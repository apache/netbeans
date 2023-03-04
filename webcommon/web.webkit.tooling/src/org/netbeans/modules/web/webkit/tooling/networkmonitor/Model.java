/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class Model extends AbstractTableModel {

    private static final int MAX_NUMBER_OF_REQUESTS = 1000;

    private final List<ModelItem> visibleRequests = Collections.synchronizedList(new ArrayList<ModelItem>());
    private volatile boolean passive = true;
    private final Project project;
    private final BrowserFamilyId browserFamilyId;

    public Model(Lookup projectContext) {
        this.project = projectContext.lookup(Project.class);
        this.browserFamilyId = projectContext.lookup(BrowserFamilyId.class);
    }

    Project getProject() {
        for (Iterator<ModelItem> iterator = visibleRequests.iterator(); iterator.hasNext();) {
            return iterator.next().getProject();
        }
        return null;
    }

    void passivate() {
        passive = true;
    }

    void activate() {
        passive = false;
    }

    public void add(Network.Request r) {
        if (passive) {
            return;
        }
        r.addPropertyChangeListener(new PropertyChangeListenerImpl(r, browserFamilyId, project));
    }

    public void add(Network.WebSocketRequest r) {
        if (passive) {
            return;
        }
        addVisibleItem(new ModelItem(null, r, browserFamilyId, project));
    }

    void addVisibleItem(final ModelItem modelItem) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                visibleRequests.add(modelItem);
                assert modelItem.canBeShownToUser() : modelItem.toString();
                int index = visibleRequests.size() - 1;
                fireTableRowsInserted(index, index);
                cleanUp();
            }
        });
    }

    void reset() {
        assert SwingUtilities.isEventDispatchThread();
        visibleRequests.clear();
        fireTableDataChanged();
    }

    void cleanUp() {
        assert SwingUtilities.isEventDispatchThread();
        int removed = 0;
        while (visibleRequests.size() > MAX_NUMBER_OF_REQUESTS) {
            visibleRequests.remove(0);
            removed++;
        }
        if (removed > 0) {
            fireTableRowsDeleted(0, removed);
        }
    }

    /**
     * Returns model item at the specified position.
     * 
     * @param index position of the requested model item.
     * @return model item at the specified position or {@code null}
     * (when there is no such item).
     */
    ModelItem getItem(int index) {
        return (index == -1) ? null : visibleRequests.get(index);
    }

    @Override
    public int getRowCount() {
        return visibleRequests.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    @NbBundle.Messages({
        "RequestTable.ColumnName.URL=URL",
        "RequestTable.ColumnName.Status=Status",
        "RequestTable.ColumnName.HTTPMethod=Method",
        "RequestTable.ColumnName.ContentType=Content Type"
    })
    public String getColumnName(int column) {
        String name;
        switch (column) {
            case 0: name = Bundle.RequestTable_ColumnName_URL(); break;
            case 1: name = Bundle.RequestTable_ColumnName_Status(); break;
            case 2: name = Bundle.RequestTable_ColumnName_HTTPMethod(); break;
            case 3: name = Bundle.RequestTable_ColumnName_ContentType(); break;
            default: throw new IllegalArgumentException();
        }
        return name;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ModelItem item = getItem(rowIndex);
        Object value;
        switch (columnIndex) {
            case 0: value = item.getURL(); break;
            case 1: value = statusHTML(item); break;
            case 2: value = item.getHTTPMethod(); break;
            case 3: 
                value = item.getContentType();
                value = (value == null) ? "-" : value; // NOI18N
                break;
            default: throw new IllegalArgumentException();
        }
        return value;
    }

    /**
     * Returns HTML representation of the status of the given model item.
     * 
     * @param item model item whose status should be returned.
     * @return HTML representation of the status of the given model item.
     */
    @NbBundle.Messages({
        "RequestTable.Status.Pending=(pending)",
        "RequestTable.Status.Failed=(failed)"
    })
    private static String statusHTML(ModelItem item) {
        String text;
        int status = item.getStatus();
        if (status > 0) {
            text = Integer.toString(status);
        } else {
            text = null;
        }
        // Hack: The comment at the beginning of the HTML ensures that
        // the statuses with different colors are sorted correctly
        if (item.isError()) {
            if (text == null) {
                text = Bundle.RequestTable_Status_Failed();
            }
            text = "<html><!--"+text+"--><font color='red'>"+text; // NOI18N
        } else {
            if (text == null) {
                text = Bundle.RequestTable_Status_Pending();
            }
            text = "<html><!--"+text+"-->"+text; // NOI18N
        }
        return text;
    }

    //~ Inner classes

    private final class PropertyChangeListenerImpl implements PropertyChangeListener {

        private final Network.Request request;
        private final BrowserFamilyId browserFamilyId;
        private final Project project;


        public PropertyChangeListenerImpl(Network.Request request, BrowserFamilyId browserFamilyId, Project project) {
            this.request = request;
            this.browserFamilyId = browserFamilyId;
            this.project = project;
        }


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource() == request : evt.getSource() + " != " + request;
            if (Network.Request.PROP_RESPONSE.equals(evt.getPropertyName())) {
                request.removePropertyChangeListener(this);
                ModelItem modelItem = new ModelItem(request, null, browserFamilyId, project);
                if (modelItem.canBeShownToUser()) {
                    addVisibleItem(modelItem);
                }
            }
        }

    }

}