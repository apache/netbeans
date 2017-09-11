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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.localhistory.ui.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.ui.history.History;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.localhistory.ui.view.ShowHistoryAction", category = "History")
@ActionRegistration(lazy = false, displayName = "CTL_ShowHistory")
@ActionReference(path = "OptionsDialog/Actions/History", name = "ShowHistoryAction")
public class ShowHistoryAction extends NodeAction {
    
    /** Creates a new instance of ShowHistoryAction */
    public ShowHistoryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    protected void performAction(final Node[] activatedNodes) {                        
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        final Set<VCSFileProxy> rootSet = ctx.getRootFiles();                    

        final VCSFileProxy[] files = rootSet.toArray(new VCSFileProxy[rootSet.size()]);                
        if(!files[0].isFile()) {
            return;
        }

        VCSFileProxy file = files[0];
        FileObject fo = file.toFileObject();
        if(fo != null) {
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                History.LOG.log(Level.WARNING, null, ex);
            }
            if(dataObject != null) {
                
                if(!hasHistoryElement(dataObject)) {
                    // there is no history element defined for this data object, so 
                    // lets open in a separate TopComponent
                    openLocalHistoryTC(files);
                    return;
                }
                
                // activate the History tab if there is a opened TopComponent 
                // with a History MultiView element
                Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
                for (final TopComponent tc : tcs) {
                    Lookup l = tc.getLookup();
                    final DataObject tcDataObject = l.lookup(DataObject.class);
                    if (tcDataObject != null && dataObject.equals(tcDataObject)) { 
                        final MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                        if(handler != null) {
                            if(activateHistoryTab(handler, tc)) {
                                // done, history tab found and activated.
                                return;
                            }
                        } 
                    }
                }
                
                final EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
                if(editorCookie != null) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            JEditorPane pane = NbDocument.findRecentEditorPane(editorCookie);
                            if(pane != null) {
                                // editor is oen, though we havent found a multiview => open the LH top component
                                openLocalHistoryTC(files);
                            }
                        }
                    };
                    if(SwingUtilities.isEventDispatchThread()) {
                        r.run();
                    } else {
                        SwingUtilities.invokeLater(r);
                    }
                }
               
                EditCookie editCookie = dataObject.getLookup().lookup(EditCookie.class);
                if(editCookie != null) {
                // no editor found, lets open it...
                    // editcookie might return imediately, so listen for the TC 
                    // to be opened and activate then
                    TopComponent.getRegistry().addPropertyChangeListener(new TCOpenedListener(dataObject, files));
                    editCookie.edit();
                    return;
                }
            }
        }
        openLocalHistoryTC(files);
    }

    private void openLocalHistoryTC(final VCSFileProxy[] files) {
        // fallback opening a LHTopComponent
        Runnable r = new Runnable() {
            @Override
            public void run() {
                final HistoryTopComponent tc = new HistoryTopComponent(files);
                tc.setName(NbBundle.getMessage(this.getClass(), "CTL_LocalHistoryTopComponent", files[0].getName())); // NOI18N
                tc.open();
                tc.requestActive();                                
            }
        };
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    /**
     * XXX HACK temporary solution to find out if the given dataobject provides a multiview
     * 
     * Returns EditCookie if there is a TopComponent with a history MultiView element
     * @param dataObject
     * @return EditCookie
     */
    private boolean hasHistoryElement(DataObject dataObject) {
        String mime = dataObject.getPrimaryFile().getMIMEType();
        Lookup l = MimeLookup.getLookup(MimePath.get(mime));
        Collection<? extends MultiViewDescription> descs = l.lookupAll(MultiViewDescription.class);
        for (MultiViewDescription desc : descs) {
            if (desc.preferredID().equals(HistoryTopComponent.PREFERRED_ID)) {
                return true;
            } 
        } 
        return false;
    }

    private boolean activateHistoryTab(final MultiViewHandler handler, final TopComponent tc) {
        if (handler != null) {
            MultiViewPerspective[] perspectives = handler.getPerspectives();
            for (final MultiViewPerspective p : perspectives) {
                if(p.preferredID().equals(HistoryTopComponent.PREFERRED_ID)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            tc.open();
                            tc.requestActive();
                            handler.requestActive(p);
                        }
                    });
                    return true;
                } 
            }
        }
        return false;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {     
        if(activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<VCSFileProxy> rootSet = ctx.getRootFiles();                
        if(rootSet == null || rootSet.isEmpty()) { 
            return false;
        }                        
        for (VCSFileProxy p : rootSet) {            
            if(p != null && !p.isFile()) {
                return false;
            }
        }        
        return true;           
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "CTL_ShowHistory");    // NOI8N    
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowHistoryAction.class);
    }

    private class TCOpenedListener implements PropertyChangeListener {
        private final DataObject dataObject;
        private final VCSFileProxy[] files;

        private TCOpenedListener(DataObject dataObject, VCSFileProxy[] files) {
            this.dataObject = dataObject;
            this.files = files;
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                try {
                    TopComponent tc =(TopComponent) evt.getNewValue();
                    Lookup l = tc.getLookup();
                    DataObject tcDataObject = l.lookup(DataObject.class);
                    
                    if (tcDataObject != null && dataObject.equals(tcDataObject)) {
                        final MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                        if (handler == null || !activateHistoryTab(handler, tc)) {
                            // oops, whats this? 
                            // lets fallback on LHTC
                            tc.close();
                            openLocalHistoryTC(files);
                        }
                    }
                } finally {
                    TopComponent.getRegistry().removePropertyChangeListener(this);
                }
            }
        }

    }
}
