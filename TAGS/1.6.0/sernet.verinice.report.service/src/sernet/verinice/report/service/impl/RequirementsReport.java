package sernet.verinice.report.service.impl;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.report.service.impl.HTMLOutputFormat;
import sernet.verinice.report.service.impl.Messages;
import sernet.verinice.report.service.impl.ODSOutputFormat;
import sernet.verinice.report.service.impl.ODTOutputFormat;

/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/

/**
 *
 */
public class RequirementsReport implements IReportType {

    private static final Logger LOG = Logger.getLogger(RequirementsReport.class);
    
    private static final String REPORT_DESIGN = "requirements.rptdesign"; //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getId()
     */
    @Override
    public String getId() {
        return "requirements";
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getLabel()
     */
    @Override
    public String getLabel() {
        return Messages.RequirementsReport_2;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getDescription()
     */
    @Override
    public String getDescription() {
        return Messages.RequirementsReport_1;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getOutputFormats()
     */
    @Override
    public IOutputFormat[] getOutputFormats() {
        return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(),  new ExcelOutputFormat(), new WordOutputFormat(), new ODTOutputFormat(), new ODSOutputFormat() };
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#createReport(sernet.verinice.interfaces.report.IReportOptions)
     */
    @Override
    public void createReport(IReportOptions reportOptions) {
        BIRTReportService brs = new BIRTReportService();
        
        URL reportDesign = RequirementsReport.class.getResource(REPORT_DESIGN); //$NON-NLS-1$
        
        if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
        {
            IRunAndRenderTask task = brs.createTask(reportDesign);
            brs.render(task, reportOptions);
        }
        else
        {
            IDataExtractionTask task = brs.createExtractionTask(reportDesign);
            brs.extract(task, reportOptions, 1);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getReportFile()
     */
    @Override
    public String getReportFile() {
        return Messages.RequirementsReport_0;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#setReportFile(java.lang.String)
     */
    @Override
    public void setReportFile(String file) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getUseCaseID()
     */
    @Override
    public String getUseCaseID() {
        return IReportType.USE_CASE_ID_GENERAL_REPORT;
    }

}
