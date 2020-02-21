/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
