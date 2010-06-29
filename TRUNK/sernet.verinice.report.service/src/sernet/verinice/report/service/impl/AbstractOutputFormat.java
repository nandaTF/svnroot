package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.IDataExtractionOption;
import org.eclipse.birt.report.engine.api.IRenderOption;

import sernet.verinice.interfaces.report.IOutputFormat;

public abstract class AbstractOutputFormat implements IOutputFormat {

	IRenderOption createBIRTRenderOptions()
	{
		return null;
	}
	
	IDataExtractionOption createBIRTExtractionOptions()
	{
		return null;
	}
	
	abstract boolean isRenderOutput();
}
