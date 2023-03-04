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

/*
 * EditWSAttributesCookieImpl.java
 *
 * Created on April 12, 2006, 10:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.wseditor.support;

import org.netbeans.modules.websvc.api.support.EditWSAttributesCookie;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.wseditor.InvalidDataException;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.netbeans.modules.websvc.api.wseditor.WSEditorProviderRegistry;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author rico
 */
public class EditWSAttributesCookieImpl implements EditWSAttributesCookie {

    /** Creates a new instance of EditWSAttributesCookieImpl */
    public EditWSAttributesCookieImpl(Node node, JaxWsModel jaxWsModel) {
        this.node = node;
        this.jaxWsModel = jaxWsModel;
    }

    @Override
    public void openWSAttributesEditor() {
        if (SwingUtilities.isEventDispatchThread()) {  //Ensure it is in AWT thread
            openEditor();
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    openEditor();
                }
            });
        }
    }
    
    private void openEditor() {
        try {
            doOpenEditor();
        }
        catch( InvalidDataException ex ){
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                    ex.getLocalizedMessage(), 
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( descriptor );
        }
    }
    
    private void doOpenEditor() throws InvalidDataException {
        final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
        final Cursor origCursor = mainWin.getGlassPane().getCursor();
        mainWin.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainWin.getGlassPane().setVisible(true);

        tc = cachedTopComponents.get(this);
        if (tc == null) {
            //populate the editor registry if needed
            populateWSEditorProviderRegistry();
            //get all providers
            providers = WSEditorProviderRegistry.getDefault().getEditorProviders();
            tc = new EditWSAttributesPanel();
            cachedTopComponents.put(this, tc);
        }
        populatePanels();
        tc.addTabs(editors, node);
        DialogDescriptor dialogDesc = new DialogDescriptor(tc, node.getName());
        dialogDesc.setHelpCtx(new HelpCtx(EditWSAttributesCookieImpl.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
        dialog.setVisible(true);

        mainWin.getGlassPane().setCursor(origCursor);
        mainWin.getGlassPane().setVisible(false);


        if (dialogDesc.getValue() == NotifyDescriptor.OK_OPTION) {
            for (WSEditor editor : editors) {
                editor.save(node);
            }
        } else {
            for (WSEditor editor : editors) {
                editor.cancel(node);
            }
        }
    }

    class DialogWindowListener extends WindowAdapter {

        Set<WSEditor> editors;

        public DialogWindowListener(Set<WSEditor> editors) {
            this.editors = editors;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            for (WSEditor editor : editors) {
                editor.cancel(node);
            }
        }
    }

    private Set getWSEditorProviders() {
        return providers;
    }

    private void populatePanels() {
        editors = new HashSet<WSEditor>();
        for (WSEditorProvider provider : providers) {
            if (provider.enable(node)) {
                //for each provider, create a WSAttributesEditor
                WSEditor editor = provider.createWSEditor(node.getLookup());
                if (editor != null) {
                    editors.add(editor);
                }
            }
        }
    }

    private void populateWSEditorProviderRegistry() {
        WSEditorProviderRegistry registry = WSEditorProviderRegistry.getDefault();
        if (registry.getEditorProviders().isEmpty()) {
            Lookup.Result<WSEditorProvider> results = Lookup.getDefault().lookup(new Lookup.Template<WSEditorProvider>(WSEditorProvider.class));
            Collection<? extends WSEditorProvider> services = results.allInstances();
            //System.out.println("###number of editors: " + services.size());
            for (WSEditorProvider provider : services) {
                registry.register(provider);
            }
        }
    }
    private Set<WSEditorProvider> providers;
    private Set<WSEditor> editors;
    private static Map<EditWSAttributesCookie, EditWSAttributesPanel> cachedTopComponents = new WeakHashMap<EditWSAttributesCookie, EditWSAttributesPanel>();
    private EditWSAttributesPanel tc;
    private Node node;
    private JaxWsModel jaxWsModel;
    private DialogWindowListener windowListener;
}
