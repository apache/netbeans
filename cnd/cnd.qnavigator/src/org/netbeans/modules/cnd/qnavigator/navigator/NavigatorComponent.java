/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.qnavigator.navigator;

import javax.swing.JComponent;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 */
public final class NavigatorComponent implements NavigatorPanel, LookupListener {

    /** Lookup template to search for java data objects. shared with InheritanceTreePanel */
    private Lookup.Result<DataObject> doContext;
    /** UI of this navigator panel */
    private NavigatorPanelUI panelUI;
    private boolean activated = false;
    private static NavigatorComponent INSTANCE = new NavigatorComponent();
    /** actual data */
    private DataObject curData;
    private final static class Lock{};
    private final Lock lock = new Lock();
    private final Lock uiLock = new Lock();
        
    private NavigatorComponent() {
//        INSTANCE = this;
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NavigatorComponent.class, "LBL_members"); //NOI18N
    }
    
    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(NavigatorComponent.class, "HINT_NavigatorTopComponen"); //NOI18N
    }
    
    @Override
    public JComponent getComponent() {
        return getPanelUI();
    }

    private String getMime(DataObject dobj) {
        FileObject fo = (dobj == null) ? null : dobj.getPrimaryFile();
        String mime = (fo == null) ? "" : fo.getMIMEType();
        return mime;
    }
    
    /** Called when this panel's component is about to being displayed.
     * Right place to attach listeners to current navigation data context.
     *
     * @param context Lookup instance representing current context
     */
    @Override
    public void panelActivated(Lookup context) {
        synchronized(lock) {
            activated = true;
            doContext = context.lookupResult(DataObject.class);
            doContext.addLookupListener(this);
            resultChanged(null);
        }
    }
    
    
    /** Called when this panel's component is about to being hidden.
     * Right place to detach, remove listeners from data context.
     */
    @Override
    public void panelDeactivated() {
        synchronized(lock) {
            activated = false;
            doContext.removeLookupListener(this);
            doContext = null;
            curData = null;
        }
    }
    
    /** Impl of LookupListener, reacts to changes of context */
    @Override
    public void resultChanged(LookupEvent ev) {
        synchronized (lock) {
            for (DataObject dob : doContext.allInstances()) {
                if (MIMENames.isFortranOrHeaderOrCppOrC(getMime(dob))) {
                    if (!dob.equals(curData)) {
                        curData = dob;
                        setNewContent(dob);
                    }
                    break;
                }
            }
        }
    }

    boolean isNavigatorEnabled() {
        synchronized (uiLock) {
            return activated && panelUI != null;
        }
    }
    
    @NavigatorPanel.Registrations({
        @NavigatorPanel.Registration(mimeType = MIMENames.HEADER_MIME_TYPE, displayName = "#LBL_members"),
        @NavigatorPanel.Registration(mimeType = MIMENames.C_MIME_TYPE, displayName = "#LBL_members"),
        @NavigatorPanel.Registration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, displayName = "#LBL_members"),
        @NavigatorPanel.Registration(mimeType = MIMENames.FORTRAN_MIME_TYPE, displayName = "#LBL_members")
    })    
    public static NavigatorComponent getInstance() {
        return INSTANCE;
    }

    @Override
    public Lookup getLookup() {
        return getPanelUI().getLookup();
    }

    /********** non public stuff **********/
    
    private void setNewContent(final DataObject cdo) {
        final NavigatorPanelUI ui = getPanelUI();
        DataObject old = ui.getDataObject();
        if (old == null || !old.equals(cdo)) {
            ui.showWaitNode();
        }
    }
    
    NavigatorPanelUI getPanelUI() {
        synchronized(uiLock) {
            if (panelUI == null) {
                panelUI = new NavigatorPanelUI(getContent());
            }
            return panelUI;
        }
    }
    private static final NavigatorContent content = new NavigatorContent();
    static NavigatorContent getContent() {
        return content;
    }
}
