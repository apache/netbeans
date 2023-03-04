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

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

/**
 * A separate class only for historical reasons. Could be inlined into PSheet if desired.
 */
public abstract class TabbedContainerBridge {
    
    protected TabbedContainerBridge(){};
    
    public static TabbedContainerBridge getDefault() {
        return new TabbedContainerBridgeImpl();
    }

    public abstract JComponent createTabbedContainer();

    public abstract void setInnerComponent (JComponent container, JComponent inner);

    public abstract JComponent getInnerComponent(JComponent jc);

    public abstract Object[] getItems(JComponent jc);

    public abstract void setItems (JComponent jc, Object[] objects, String[] titles);

    public abstract void attachSelectionListener (JComponent jc, ChangeListener listener);

    public abstract void detachSelectionListener (JComponent jc, ChangeListener listener);

    public abstract Object getSelectedItem(JComponent jc);

    public abstract void setSelectedItem (JComponent jc, Object selection);

    public abstract boolean setSelectionByName(JComponent jc, String tabname);

    public abstract String getCurrentSelectedTabName(JComponent jc);

}
