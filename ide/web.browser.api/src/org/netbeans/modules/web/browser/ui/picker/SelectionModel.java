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

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author S. Aubrecht
 */
final class SelectionModel {

    private final ListSelectionListener selectionListener;
    private final ArrayList<SelectionList> lists = new ArrayList<>( 10 );
    private final ChangeSupport changeSupport = new ChangeSupport( this );

    SelectionModel() {
        selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                onSelectionChange( e );
            }
        };
    }

    void add( SelectionList sl ) {
        synchronized( lists ) {
            if( lists.contains( sl ) )
                return;
            boolean hasSelection = null != getSelectedItem();
            lists.add( sl );
            if( hasSelection )
                sl.clearSelection();
            sl.getSelectionModel().addListSelectionListener( selectionListener );
        }
    }

    public ListItem getSelectedItem() {
        synchronized( lists ) {
            for( SelectionList sl : lists ) {
                ListItem sel = sl.getSelectedItem();
                if( null != sel )
                    return sel;
            }
            return null;
        }
    }

    public void setSelectedItem( ListItem item ) {
        assert SwingUtilities.isEventDispatchThread();
        synchronized( lists ) {
            if( null == item ) {
                for( SelectionList sl : lists ) {
                    sl.clearSelection();
                }
            } else {
                for( SelectionList sl : lists ) {
                    if( sl.setSelectedItem( item ) ) {
                        return;
                    }
                }
            }
        }
        //or just ignore silently?
        throw new IllegalArgumentException();
    }

    public void addChangeListener( ChangeListener cl ) {
        changeSupport.addChangeListener( cl );
    }

    public void removeChangeListener( ChangeListener cl ) {
        changeSupport.removeChangeListener( cl );
    }

    private boolean ignoreSelectionEvents = false;
    private void onSelectionChange( ListSelectionEvent e ) {
        if( e.getValueIsAdjusting() )
            return;
        if( ignoreSelectionEvents ) {
            return;
        }
        ignoreSelectionEvents = true;
        synchronized( lists ) {
            for( SelectionList sl : lists ) {
                if( sl.getSelectionModel() == e.getSource() )
                    continue;
                sl.clearSelection();
            }
        }
        ignoreSelectionEvents = false;
        changeSupport.fireChange();
    }
}
