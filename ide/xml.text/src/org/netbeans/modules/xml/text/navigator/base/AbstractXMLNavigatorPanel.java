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

package org.netbeans.modules.xml.text.navigator.base;

import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * A base implementation of NavigatorPanel for all XML navigators.
 *
 * @author Samaresh
 * @version 1.0
 */
public abstract class AbstractXMLNavigatorPanel implements NavigatorPanel {

    protected AbstractXMLNavigatorContent navigator;
    protected Lookup.Result selection;
    protected final LookupListener selectionListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            // #230574: threading model of AbstractXMLNavigatorPanel unspecified,
            // but the called code assumes AWT thread, replan into EDT.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(selection == null)
                        return;
                    navigate(selection.allInstances());
                }
            });
        }
    };
    
    public abstract String getDisplayHint();
    
    public abstract String getDisplayName();
    
    /**
     * 
     * @return AbstractXMLNavigatorContent
     */
    protected abstract AbstractXMLNavigatorContent getNavigatorUI();
    
    public JComponent getComponent() {
        return getNavigatorUI();
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void panelActivated(Lookup context) {
        getNavigatorUI().showWaitNode();
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        getNavigatorUI().showWaitNode();
        if(selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        if(navigator != null)
            navigator.release(); //hide the UI
    }
    
    /**
     * 
     * @param selectedFiles 
     */
    public void navigate(Collection/*<DataObject>*/ selectedFiles) {
        if(selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            navigator.navigate(d);           
        }
    }
    
   
}
