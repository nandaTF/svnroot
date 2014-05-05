/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.bpm;

import org.eclipse.osgi.util.NLS;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.bpm.messages"; //$NON-NLS-1$
    public static String PersonTypeSelectDialog_0;
    public static String PersonTypeSelectDialog_1;
    public static String PersonTypeSelectDialog_2;
    public static String PersonTypeSelectDialog_3;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
