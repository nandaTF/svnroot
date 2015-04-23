/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.column;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class IconColumn extends AbstractColumn {

    public IconColumn(ColumnStore columnStore) {
        super(columnStore);
    }

    private final static String ICON_COLUMN_TITLE = "icon";

    public final static String ICON_PROPERTY_NAME = "icon-path";

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isMultiselect()
     */
    @Override
    public boolean isMultiselect() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isSingleSelect()
     */
    @Override
    public boolean isSingleSelect() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isNumericSelect()
     */
    @Override
    public boolean isNumericSelect() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isBooleanSelect()
     */
    @Override
    public boolean isBooleanSelect() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isEnum()
     */
    @Override
    public boolean isEnum() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isLine()
     */
    @Override
    public boolean isLine() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isReference()
     */
    @Override
    public boolean isReference() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isCnaLinkReference()
     */
    @Override
    public boolean isCnaLinkReference() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isText()
     */
    @Override
    public boolean isText() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#isDate()
     */
    @Override
    public boolean isDate() {
        // TODO Auto-generated method stub
        return false;
    }



    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#getTitle()
     */
    @Override
    public String getTitle() {
        return ICON_COLUMN_TITLE;
    }

    @Override
    public int getRank(){
        return -4;
    }
}
