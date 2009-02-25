package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class GetBausteinText extends GenericCommand {

	private String url;
	private String stand;
	private String bausteinText;

	public GetBausteinText(String url, String stand) {
		this.url = url;
		this.stand = stand;
	}

	public void execute() {
		try {
			InputStream in = BSIMassnahmenModel.getBaustein(url, stand);
			bausteinText = InputUtil.streamToString(in,  "iso-8859-1");
		} catch (GSServiceException e) {
			throw new RuntimeCommandException(e);
		} catch (IOException e) {
			throw new RuntimeCommandException(e);
		}
	}

	public String getBausteinText() {
		return bausteinText;
	}

}
