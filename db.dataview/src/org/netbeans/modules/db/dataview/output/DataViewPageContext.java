/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.output;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Holds data view page pointers and the current page data set
 *
 * @author Ahimanikya Satapathy
 */
class DataViewPageContext {
    public static final String PROP_pageSize = "pageSize";
    public static final String PROP_currentPos = "currentPos";
    public static final String PROP_tableMetaData = "tableMetaData";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private int pageSize = 10;
    private int currentPos = 1;
    private DataViewDBTable tableMetaData = null;
    private final DataViewTableUIModel model = new DataViewTableUIModel(new DBColumn[0]);

    DataViewPageContext(int pageSize) {
        this.pageSize = pageSize;
    }

    public DataViewDBTable getTableMetaData() {
        return tableMetaData;
    }

    public void setTableMetaData(DataViewDBTable tableMetaData) {
        DataViewDBTable old = this.tableMetaData;
        this.tableMetaData = tableMetaData;
        firePropertyChange(PROP_tableMetaData, old, tableMetaData);
        resetEditableState();
    }

    void resetEditableState() {
        model.setEditable(tableMetaData == null ? false : tableMetaData.hasOneTable());
    }

    int getPageSize() {
        return pageSize;
    }

    synchronized void setPageSize(int pageSize) {
        int oldPageSize = this.pageSize;
        this.pageSize = pageSize;
        firePropertyChange(PROP_pageSize, oldPageSize, pageSize);
    }

    int getCurrentPos() {
        return currentPos;
    }

    synchronized private void setCurrentPos(int currentPos) {
        int oldPos = this.currentPos;
        this.currentPos = currentPos;
        firePropertyChange(PROP_currentPos, oldPos, currentPos);
    }

    synchronized void first() {
        setCurrentPos(1);
    }

    synchronized void previous() {
        setCurrentPos(getCurrentPos() - pageSize);
    }

    synchronized void next() {
        setCurrentPos(getCurrentPos() + pageSize);
    }

    DataViewTableUIModel getModel() {
        return model;
    }

    boolean hasRows() {
        return model.getRowCount() > 0;
    }

    boolean hasNext() {
        if (pageSize == 0) {
            return false;
        }
        return getModel().getRowCount() >= pageSize;
    }

    boolean hasOnePageOnly() {
        return pageSize == 0 || getModel().getRowCount() < pageSize;
    }

    boolean hasPrevious() {
        return pageSize != 0 && ((currentPos - pageSize) > 0) && hasRows();
    }

    boolean isLastPage() {
        return pageSize == 0 || getModel().getRowCount() < pageSize;
    }

    boolean refreshRequiredOnInsert() {
        return pageSize == 0 || (isLastPage() && model.getRowCount() <= pageSize);
    }

    /**
     * Ensure the property change event is dispatched into the EDT
     *
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                pcs.firePropertyChange(propertyName, oldValue, newValue);
            }
        });

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
