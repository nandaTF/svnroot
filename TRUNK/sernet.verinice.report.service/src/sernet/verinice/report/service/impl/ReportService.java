package sernet.verinice.report.service.impl;

import java.io.File;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportService;
import sernet.verinice.interfaces.report.IReportType;


public class ReportService implements IReportService {
	
	private static final Logger LOG = Logger.getLogger(ReportService.class);

	public ReportService()
	{
	}
	
	@Override
	public void runTestReportGeneration() {
		IReportType rt = new TestReportType();
		
		IReportOptions ro = new IReportOptions() {
			public boolean isToBeEncrypted() { return false; }
			public boolean isToBeCompressed() { return false; }
			public IOutputFormat getOutputFormat() { return new PDFOutputFormat(); } 
			public File getOutputFile() { return new File("/tmp/test-report.pdf"); }
		};
		
		rt.createReport(ro);
	}

	@Override
	public void runSamtReportGeneration(IReportOptions reportOptions) {
		IReportType rt = new SamtReportType();
		
		rt.createReport(reportOptions);
	}

	@Override
	public IReportType[] getReportTypes() {
		return new IReportType[] { new SamtReportType() };
	}
	
}
