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
package org.netbeans.modules.web.browser.ui.picker;

import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 *
 * @author S. Aubrecht
 */
final class SelectionList {

    private final SelectionListImpl theList;

    SelectionList() {
        theList = new SelectionListImpl();
    }

    public JComponent getComponent() {
        return theList;
    }

    public void setItems( List<? extends ListItem> items ) {
        final List<? extends ListItem> listItems = Collections.unmodifiableList( items );
        setItems( new AbstractListModel<ListItem>() {

            @Override
            public int getSize() {
                return listItems.size();
            }

            @Override
            public ListItem getElementAt( int index ) {
                return listItems.get( index );
            }
        });
    }

    public void setItems( ListModel<ListItem> items ) {
        ListItem selItem = theList.getSelectedValue();
        theList.setModel( items );
        if( null != selItem ) {
            setSelectedItem( selItem );
        }
    }

    ListSelectionModel getSelectionModel() {
        return theList.getSelectionModel();
    }

    void clearSelection() {
        theList.clearSelection();
    }

    ListItem getSelectedItem() {
        return theList.getSelectedValue();
    }

    /**
     * Attempts to select the given item in this list.
     * @param item Item to select.
     * @return True if this list contains the given item, false otherwise.
     */
    boolean setSelectedItem( ListItem item ) {
        ListModel<ListItem> model = theList.getModel();
        for( int i=0; i<model.getSize(); i++ ) {
            if( item.equals( model.getElementAt( i )  ) ) {
                theList.setSelectedIndex( i );
                return true;
            }
        }
        return false;
    }
}
