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

package org.netbeans.modules.openide.explorer;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.TabDataModel;

class TabbedContainerBridgeImpl extends TabbedContainerBridge {
    
    /** Creates a new instance of TabbedContainerBridgeImpl */
    public TabbedContainerBridgeImpl() {
    }
    
    public void attachSelectionListener(JComponent jc, ChangeListener listener) {
        TabbedContainer cont = (TabbedContainer) jc;
        cont.getSelectionModel().addChangeListener(listener);
    }
    
    public JComponent createTabbedContainer() {
        return new TabbedContainer(TabbedContainer.TYPE_TOOLBAR);
    }
    
    public void detachSelectionListener(JComponent jc, ChangeListener listener) {
        TabbedContainer cont = (TabbedContainer) jc;
        cont.getSelectionModel().removeChangeListener(listener);
    }
    
    public Object[] getItems(JComponent jc) {
        TabbedContainer cont = (TabbedContainer) jc;
        List<TabData> l = cont.getModel().getTabs();
        Object[] items = new Object[l.size()];
        for (int i=0; i < items.length; i++) {
            items[i] = l.get(i).getUserObject();
        }
        return items;
    }
    
    public Object getSelectedItem(JComponent jc) {
        Object result = null;
        TabbedContainer cont = (TabbedContainer) jc;
        int i = cont.getSelectionModel().getSelectedIndex();
        if (i != -1) {
            result = cont.getModel().getTab(i).getUserObject();
        }
        return result;
    }

    public void setSelectedItem(JComponent jc, Object selection) {
        TabbedContainer cont = (TabbedContainer) jc;
        TabDataModel mdl = cont.getModel();
        int max = mdl.size();
        for (int i=0; i < max; i++) {
            TabData td = mdl.getTab(i);
            if (td.getUserObject() == selection) {
                cont.getSelectionModel().setSelectedIndex(i);
                break;
            }
        }
    }

    public boolean setSelectionByName(JComponent jc, String tabname) {
        TabbedContainer cont = (TabbedContainer) jc;
        TabDataModel mdl = cont.getModel();
        int max = mdl.size();
        for (int i=0; i < max; i++) {
            TabData td = mdl.getTab(i);
            if (tabname.equals(td.getText())) {
                cont.getSelectionModel().setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    public String getCurrentSelectedTabName(JComponent jc) {
        TabbedContainer cont = (TabbedContainer) jc;
        int sel = cont.getSelectionModel().getSelectedIndex();
        if (sel != -1) {
            TabData td = cont.getModel().getTab(sel);
            return td.getText();
        }
        return null;
    }

    public void setInnerComponent(JComponent jc, JComponent inner) {
        TabbedContainer cont = (TabbedContainer) jc;
        ComponentConverter cc = new ComponentConverter.Fixed (inner);
        cont.setComponentConverter(cc);
    }
    
    public JComponent getInnerComponent(JComponent jc) {
        TabbedContainer cont = (TabbedContainer) jc;
        return (JComponent) cont.getComponentConverter().getComponent(null);
    }
    
    public void setItems(JComponent jc, Object[] objects, String[] titles) {
        TabbedContainer cont = (TabbedContainer) jc;
        assert objects.length == titles.length;
        TabData[] td = new TabData [objects.length];
        for (int i=0; i < objects.length; i++) {
            td[i] = new TabData (objects[i], null, titles[i], null);
        }
        cont.getModel().setTabs(td);
    }
    
}
