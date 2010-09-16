/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/

package sernet.verinice.oda.driver.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.oda.driver.Activator;
import bsh.EvalError;
import bsh.Interpreter;

public class Query implements IQuery
{
	private Logger log = Logger.getLogger(Query.class);
	
	private int maxRows;
    private String queryText;
    
    
    public static Integer vnRrootObject;
    
    private Interpreter setupInterpreter, interpreter;
    
    private HashMap<String, String> properties = new HashMap<String, String>();
    
    private HashMap<String, Object> inParameterValues = new HashMap<String, Object>();
    
    private Object result;
    
    private String[] columns, inParameters;
    
    public static final String PROP_SETUP_QUERY_TEXT = "setupQueryText";
    
    Query()
    {
    	IVeriniceOdaDriver odaDriver = Activator.getDefault().getOdaDriver();

    	try {
    	    // "Setup" BSH environment:
    		setupInterpreter = new Interpreter();
    		setupInterpreter.setClassLoader(Query.class.getClassLoader());
    		
    		setupInterpreter.set("__columns", null);
    		setupInterpreter.eval("columns(c) { __columns = c; }");
			
    		setupInterpreter.set("__inParameters", null);
    		setupInterpreter.eval("inParameters(ip) { __inParameters = ip; }");
    		setupInterpreter.set("helper", new Helper());

    		// BSH environment:
    		interpreter = new Interpreter();
    		interpreter.setClassLoader(Query.class.getClassLoader());
    		
    		
			interpreter.set("_inpv", inParameterValues);
			interpreter.eval(
					"inpv(s) {" +
					" v = _inpv.get(s);" +
					" return (v == null) ? \"input parameter value \" + s + \" does not exist.\" : v;" +
					"}");

			interpreter.set("_vars", odaDriver.getScriptVariables());
			interpreter.eval(
					"vars(s) {" +
					" v = _vars.get(s);" +
					" return (v == null) ? s + \" does not exist.\" : v;" +
					"}");
			
    		interpreter.set("helper", new Helper());
    		interpreter.eval("gpt(entityType) { return helper.getAllPropertyTypes(entityType); }");
    		interpreter.set("properties", properties);

    		
    		
		} catch (EvalError e) {
			new RuntimeException("Unable to set BSH variable 'properties'.", e);
		}
    }
    
    /**
     * A class with utility methods which are supposed to be used by report scripts.
     * 
     * @author Robert Schuster <r.schuster@tarent.de>
     *
     */
    public class Helper {
        
    	public ICommand execute(ICommand c)
    	{
    		try
    		{
    			return Activator.getDefault().getCommandService().executeCommand(c);
    		} catch (CommandException e)
    		{
    			throw new IllegalStateException("Running the command failed.");
    		}
    	}
    	
        /**
         * A variant of 'retrieveEntityValues' which does not specify the type of the properties. (Defaults
         * to 'getSimpleValue'.)
         * 
         * @param typeId
         * @param propertyNames
         * @return
         */
        public List<List<String>> retrieveEntityValues(String typeId, String[] propertyNames)
        {
        	return retrieveEntityValues(typeId, propertyNames, new Class[0]);
        }
        
        /**
         * Retrieves a list containing the values of the propertytypes of the specified entitytype.
         * 
         * The data is returned in a way that it can directly be used for BIRT tables (list of property value lists) 
         * 
         * By specifying the class of the result the retrieval code will use {@link Entity#getInt(String)} (for
         * <code>Integer.class</code>) or {@link Entity#getSimpleValue(String)} (for <code>String.class</code>).
         * 
         * @param typeId
         * @param propertyNames
         * @param classes
         * @return
         */
        public List<List<String>> retrieveEntityValues(String typeId, String[] propertyNames, Class<?>[] classes)
        {
    		LoadEntityValues command = new LoadEntityValues(typeId, propertyNames, classes );

    			try {
    				command = Activator.getDefault().getCommandService().executeCommand(command);
    			} catch (CommandException e) {
    				return Collections.emptyList();
    			}
    		
    		return command.getResult();
        }
        
      public String[] getAllPropertyTypes(String entityTypeId) {
          HUITypeFactory htf = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
          return htf.getEntityType(entityTypeId).getAllPropertyTypeIDsIncludingGroups();
      }

        public List<List<String>> map(List<CnATreeElement> input, String[] props)
        {
        	return map(input, props, new Class<?>[0]);
        }
        
        /**
         * Takes an existing list of {@link CnATreeElement} instances and converts them into a list
         * of string values (this can be used as the input for BIRT tables).
         * 
         * @param input
         * @param props
         * @param classes
         * @return
         */
        public List<List<String>> map(List<CnATreeElement> input, String[] props, Class<?>[] classes)
        {
        	List<List<String>> result = new ArrayList<List<String>>();
        	
        	for (CnATreeElement e : input)
        	{
        		result.add(LoadEntityValues.retrievePropertyValues(e.getEntity(), props, classes));
        	}
        	
        	return result;
        }
        
        public Integer getRoot() {
            return Query.vnRrootObject;
        }

        /**
         * Takes a {@link BufferedImage} instance and turns it into a byte array which can be used
         * by BIRT's dynamic images.
         * 
         * <p>Note: If a dataset should contain only a single image it *MUST* be wrapped
         * using {@link #wrapeSingleImageResult}.</p>
         * 
         * @param im
         * @return
         * @throws IOException
         */
        public byte[] createImageResult(BufferedImage im) throws IOException
        {
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();

        	ImageIO.write(im, "png", bos);

        	return bos.toByteArray();
        }
        
    }
	
	public void prepare( String queryText ) throws OdaException
	{
        this.queryText = queryText;
	}
	
	public void setAppContext( Object context ) throws OdaException
	{
	    // do nothing; assumes no support for pass-through context
	}

	public void close() throws OdaException
	{
        queryText = null;
        result = null;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException
	{
		return new ResultSetMetaData(runQuery(), columns);
	}
	
	private void runSetupQuery() throws OdaException
	{
		try {
			String setupQueryText = properties.get(PROP_SETUP_QUERY_TEXT);
			if (setupQueryText == null)
				return;
			
			setupInterpreter.eval(setupQueryText);
			Object cols = setupInterpreter.get("__columns");
			if (cols instanceof String[])
				columns = (String[]) cols;
			else
				columns = null;
			
			Object inp = setupInterpreter.get("__inParameters");
			if (inp instanceof String[])
				inParameters = (String[]) inp;
			else
				inParameters = null;
		} catch (EvalError e) {
			log.warn("Error evaluating the setup query: " + e);
			
			throw new IllegalStateException("Unable to execute setup query: " + e.getErrorText());
		}
	}
	
	private Object runQuery() throws OdaException
	{
		runSetupQuery();
		
		try {
			result = interpreter.eval(queryText);
		} catch (EvalError e) {
			log.warn("Error evaluating the query: ", e);
			result = new String("Unable to execute query: " + e.getErrorText());
		}
		
		return result;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException
	{
		ResultSet resultSet = new ResultSet(runQuery(), columns);
		resultSet.setMaxRows( getMaxRows() );
		return resultSet;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty( String name, String value ) throws OdaException
	{
		properties.put(name, value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	public void setMaxRows( int max ) throws OdaException
	{
	    maxRows = max;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	public int getMaxRows() throws OdaException
	{
		return maxRows;
	}
	
	private void setValue(int parameterId, Object value) throws OdaException
	{
    	runSetupQuery();
    	if (inParameters != null
    			&& inParameters.length >= parameterId)
    	{
    		inParameterValues.put(inParameters[parameterId-1], value);
    	}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
	 */
	public void clearInParameters() throws OdaException
	{
		inParameterValues.clear();
	}

	public void setInt( String parameterName, int value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setInt( int parameterId, int value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	public void setDouble( String parameterName, double value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setDouble( int parameterId, double value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	public void setBigDecimal( String parameterName, BigDecimal value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setBigDecimal( int parameterId, BigDecimal value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	public void setString( String parameterName, String value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setString( int parameterId, String value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	public void setDate( String parameterName, Date value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setDate( int parameterId, Date value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	public void setTime( String parameterName, Time value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setTime( int parameterId, Time value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	public void setTimestamp( String parameterName, Timestamp value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	public void setTimestamp( int parameterId, Timestamp value ) throws OdaException
	{
		setValue(parameterId, value);
	}

    public void setBoolean( String parameterName, boolean value )
            throws OdaException
    {
		inParameterValues.put(parameterName, value);
    }

    public void setBoolean( int parameterId, boolean value )
            throws OdaException
    {
		setValue(parameterId, value);
    }

    public void setObject( String parameterName, Object value )
            throws OdaException
    {
		inParameterValues.put(parameterName, value);
    }
    
    public void setObject( int parameterId, Object value ) throws OdaException
    {
		setValue(parameterId, value);
    }
    
    public void setNull( String parameterName ) throws OdaException
    {
		inParameterValues.put(parameterName, null);
    }

    public void setNull( int parameterId ) throws OdaException
    {
		setValue(parameterId, null);
    }

	public int findInParameter( String parameterName ) throws OdaException
	{
		for (int i = 0; i < inParameters.length; i++)
		{
			if (inParameters[i].equals(parameterName))
				return i;
		}
		
		return -1;
	}

	public IParameterMetaData getParameterMetaData() throws OdaException
	{
		runSetupQuery();
		return new ParameterMetaData(inParameters);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
	 */
	public void setSortSpec( SortSpec sortBy ) throws OdaException
	{
		// only applies to sorting, assumes not supported
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	public SortSpec getSortSpec() throws OdaException
	{
		// only applies to sorting
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setSpecification(org.eclipse.datatools.connectivity.oda.spec.QuerySpecification)
     */
    @SuppressWarnings("restriction")
    public void setSpecification( QuerySpecification querySpec )
            throws OdaException, UnsupportedOperationException
    {
        // assumes no support
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getSpecification()
     */
    @SuppressWarnings("restriction")
    public QuerySpecification getSpecification()
    {
        // assumes no support
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getEffectiveQueryText()
     */
    public String getEffectiveQueryText()
    {
        return queryText;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#cancel()
     */
    public void cancel() throws OdaException, UnsupportedOperationException
    {
    	result = null;
    }
    
}
