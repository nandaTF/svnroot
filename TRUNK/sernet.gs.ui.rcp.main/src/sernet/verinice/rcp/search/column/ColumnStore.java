/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.verinice.rcp.search.column;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.connect.PropertyType;

/**
 * This implementation of {@link IColumnStore} is based on two {@link TreeSet}s
 * for visible and invisible columns.
 *
 * A column (or {@link PropertyType}) is either in set visibleColumns or
 * invisibleColumns.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */

public class ColumnStore implements IColumnStore {

    /**
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     */
    private final class ColumnComparator implements Comparator<IColumn> {
        NumericStringComparator stringCompare = new NumericStringComparator();

        @Override
        public int compare(IColumn o1, IColumn o2) {
            if (o1 instanceof PropertyTypeColumn && o2 instanceof PropertyTypeColumn)
                return stringCompare.compare(o1.getTitle(), o2.getTitle());
            return o1.getRank() - o2.getRank();
        }
    }

    private SortedSet<IColumn> visibleColumns;
    private SortedSet<IColumn> invisibleColumns;

    public ColumnStore() {
        super();
        visibleColumns = new TreeSet<IColumn>(new ColumnComparator());
        invisibleColumns = new TreeSet<IColumn>(new ColumnComparator());
    }

    public ColumnStore(String entityTypeid) {
        this();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumnStore#getColumns()
     */
    @Override
    public SortedSet<IColumn> getColumns() {
        return visibleColumns;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.rcp.search.IColumnStore#addColumn(sernet.hui.common.connect
     * .PropertyType)
     */
    @Override
    public void addColumn(IColumn column) {
        visibleColumns.add(column);
        invisibleColumns.remove(column);
    }

    public void addInvisibleColumn(IColumn column) {
        visibleColumns.remove(column);
        invisibleColumns.add(column);
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumnStore#restoreDefault()
     */
    @Override
    public void restoreDefault() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumnStore#getNotVisible()
     */
    @Override
    public SortedSet<IColumn> getInvisible() {
        return invisibleColumns;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.rcp.search.IColumnStore#setVisible(sernet.hui.common.
     * connect.PropertyType, boolean)
     */
    @Override
    public void setVisible(IColumn column, boolean visible) {
        if (visible) {
            addColumn(column);
        } else {
            addInvisibleColumn(column);
        }
    }

    public SortedSet<IColumn> getAllColumns() {
        SortedSet<IColumn> allColumns = getColumns();
        allColumns.addAll(getInvisible());
        return allColumns;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumnStore#isColumnVisible(sernet.verinice.rcp.search.column.IColumn)
     */
    @Override
    public boolean isColumnVisible(IColumn iColumn) {
        return visibleColumns.contains(iColumn);
    }
}
