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
package sernet.verinice.report.service.impl;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.report.IReportService;
import sernet.verinice.interfaces.report.IReportType;


public class ReportService implements IReportService {
	
	private static final Logger LOG = Logger.getLogger(ReportService.class);
	
	private IReportType[] reportTypes;

	@Override
	public IReportType[] getReportTypes() {
		if (reportTypes == null)
			reportTypes = new IReportType[] { new UserReportType(), new SamtReportType(), new ComprehensiveSamtReportType() };
		
		return reportTypes;
	}
	
}
