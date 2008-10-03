/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * @author ahanekop@sernet.de
 *
 */
public class RisikoMassnahmenUmsetzungTransfer extends ByteArrayTransfer {
	
	private static final String TYPE_NAME = "RisikoMassnahmenTransfer";

    private static final int TYPE_ID = registerType(TYPE_NAME);

    private static RisikoMassnahmenUmsetzungTransfer INSTANCE = new RisikoMassnahmenUmsetzungTransfer();

    public static RisikoMassnahmenUmsetzungTransfer getInstance() {
      return INSTANCE;
    }

    /**
     *  returns the type id this TransferAgent is able
     *  to process.
     *  
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPE_ID };
	}

	/**
	 *  returns the name of the type this Transferagent
	 *  is able to process.
	 *  
	 *  @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	/**
	 * converts a Java representation of data into a
	 * platform-specific one.
	 */
	public void javaToNative(Object data, TransferData transferData) {
		if (!(data instanceof RisikoMassnahmenUmsetzung))
			return;
		RisikoMassnahmenUmsetzung[] items = (RisikoMassnahmenUmsetzung[]) data;

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.writeInt(items.length);
			for (int i = 0; i < items.length; i++) {
				dataOut.writeUTF(items[i].getText());
				//dataOut.writeUTF(items[i].getDecscription());
			}
			dataOut.close();
			out.close();
			super.javaToNative(out.toByteArray(), transferData);
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
		}
	}
	
	
	public Object nativeToJava(TransferData transferData) {
		// no native transfer provided
		return null;
	}
}
