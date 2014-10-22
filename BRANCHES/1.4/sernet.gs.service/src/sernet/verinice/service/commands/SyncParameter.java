/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class SyncParameter implements Serializable {

    private boolean insert;
    private boolean update;
    private boolean delete;
    private boolean integrate; 
   
    public static final Integer EXPORT_FORMAT_VERINICE_ARCHIV = 0; 
    public static final Integer EXPORT_FORMAT_XML_PURE = 1;  
    public static final Integer EXPORT_FORMAT_DEFAULT = EXPORT_FORMAT_VERINICE_ARCHIV;
    
    private Integer format = EXPORT_FORMAT_DEFAULT;

    public SyncParameter(boolean insert, boolean update, boolean delete, boolean integrate, Integer format) throws SyncParameterException {
        super();
        this.insert = insert;
        this.update = update;
        this.delete = delete;
        this.integrate = integrate;
        if(format!=null) {
            this.format = format;
        }
        
        validateParameter();
    }

    /**
     * @param insertState
     * @param updateState
     * @param deleteState
     * @throws SyncParameterException 
     */
    public SyncParameter(boolean insertState, boolean updateState, boolean deleteState) throws SyncParameterException {
        this(insertState, updateState, deleteState, true, SyncParameter.EXPORT_FORMAT_DEFAULT);
    }

    public boolean isInsert() {
        return insert;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isIntegrate() {
        return integrate;
    }

    public void setIntegrate(boolean integrate) {
        this.integrate = integrate;
    }

    public Integer getFormat() {
        return format;
    }

    public void setFormat(Integer format) {
        this.format = format;
    }
    
    private void validateParameter() throws SyncParameterException {
        if ((this.insert || this.update || this.delete || this.integrate) == false)
            throw new SyncParameterException();
    }
}
