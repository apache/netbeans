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
/*
 * CustomizationWSEditor.java
 *
 * Created on March 9, 2006, 4:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.websvc.customization.light;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.api.support.RefreshCookie;
import org.netbeans.modules.websvc.api.wseditor.InvalidDataException;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.customization.multiview.SaveableSectionInnerPanel;
import org.netbeans.modules.websvc.customization.multiview.WSCustomizationTopComponent;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Roderico Cruz
 */
public class CustomizationWSEditor implements WSEditor {

    private WSCustomizationTopComponent wsTopComponent;
    private boolean wsdlIsDirty;
    private Definitions primaryDefinitions;
    private UndoManager undoManager;
    private JAXWSLightSupport jaxWsSupport;
    private JaxWsService service;

    /**
     * Creates a new instance of CustomizationWSEditor
     */
    public CustomizationWSEditor(JAXWSLightSupport jaxWsSupport, JaxWsService service) {
        this.jaxWsSupport = jaxWsSupport;
        this.service = service;
    }
    
    public CustomizationWSEditor() {
    }

    private void saveAndRefresh(final Node node) {
        Collection<SaveableSectionInnerPanel> panels = wsTopComponent.getPanels();
        for (SaveableSectionInnerPanel panel : panels) {
            panel.save();
            if (!wsdlIsDirty) {
                wsdlIsDirty = panel.wsdlIsDirty();
            }
        }

        try {
            if (wsdlIsDirty) {
                Set<WSDLModel> modelSet = wsdlModels.keySet();
                for (WSDLModel wsdlModel : modelSet) {
                    ModelSource ms = wsdlModel.getModelSource();
                    FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                    DataObject wsdlDO = DataObject.find(fo);
                    SaveCookie wsdlSaveCookie = (SaveCookie) wsdlDO.getCookie(SaveCookie.class);
                    if (wsdlSaveCookie != null) {
                        wsdlSaveCookie.save();
                    }
                }
            }
            final ProgressHandle handle = ProgressHandleFactory.createHandle
                    (NbBundle.getMessage(CustomizationWSEditor.class, "TXT_Refreshing")); //NOI18N
            handle.start(100);
            handle.switchToIndeterminate();
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        if (wsdlIsDirty) {
                            RefreshCookie refreshCookie =
                                    (RefreshCookie) node.getCookie(RefreshCookie.class);
                            if (refreshCookie != null) { 
                                refreshCookie.refreshService(false);
                            }
                        }
                    } finally {
                        handle.finish();
                    }
                }
            };
            RequestProcessor.getDefault().post(r);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public void save(final Node node) {
        saveAndRefresh(node);
        removeListeners();
    }

    public JComponent createWSEditorComponent(Node node) throws InvalidDataException {
        try {
            initializeModels(node);
        }
        catch ( InvalidDataException ex){
            throw ex;
        } 
        catch (Exception e) {
            Logger.getLogger(CustomizationWSEditor.class.getName()).log(Level.FINE, 
                    "Cannot create WSEditor Component", e); //NOI1*N
        }

        wsTopComponent = new WSCustomizationTopComponent(node, getWSDLModels(), primaryDefinitions, true);
        wsTopComponent.setName(getTitle());
        return wsTopComponent;
    }

    public String getTitle() {
        return NbBundle.getMessage(CustomizationWSEditor.class, "TITLE_WSDL_CUSTOMIZATION");
    }

    public Set<WSDLModel> getWSDLModels() {
        return wsdlModels.keySet();
    }
    private Map<WSDLModel, Boolean> wsdlModels = new HashMap<WSDLModel, Boolean>();

    private void initializeModels(Node node) throws IOException, 
        CatalogModelException, InvalidDataException {
        if (wsdlModels.isEmpty()) {
            undoManager = new UndoManager();
            WSDLModel primaryModel = getPrimaryModel(node);
            populateAllModels(primaryModel);
            Set<WSDLModel> modelSet = wsdlModels.keySet();
            for (WSDLModel wsdlModel : modelSet) {
                wsdlModel.addUndoableEditListener(undoManager);
            }
        }
    }

    private void removeListeners() {
        Set<WSDLModel> modelSet = wsdlModels.keySet();
        for (WSDLModel wsdlModel : modelSet) {
            wsdlModel.removeUndoableEditListener(undoManager);
        }
    }

    private DataObject getDataObjectOfModel(WSDLModel wsdlModel) {
        ModelSource ms = wsdlModel.getModelSource();
        return (DataObject) ms.getLookup().lookup(DataObject.class);
    }

    private boolean modelExists(final WSDLModel wsdlModel) {
        if (wsdlModels.size() == 0) {
            return false;
        }
        DataObject modelDobj = getDataObjectOfModel(wsdlModel);
        if (!modelDobj.isValid()) {
            return true;
        }
        Set<WSDLModel> wsdls = wsdlModels.keySet();
        for (WSDLModel wsdl : wsdls) {
            DataObject dobj = getDataObjectOfModel(wsdl);
            if (!dobj.isValid()) {
                continue;
            }
            if (modelDobj.equals(dobj)) {
                return true;
            }
        }
        return false;
    }

    private void populateAllModels(WSDLModel wsdlModel) throws CatalogModelException {
        if (modelExists(wsdlModel)) {
            return;
        }
        DataObject dobj = getDataObjectOfModel(wsdlModel);
        if (!dobj.isValid()) {
            return;
        }
        Definitions definitions = wsdlModel.getDefinitions();
        if (definitions.getImports().size() == 0) {
            wsdlModels.put(wsdlModel, Boolean.valueOf(dobj.isModified()));
            return;
        } else {
            wsdlModels.put(wsdlModel, Boolean.valueOf(dobj.isModified()));
            Set<WSDLModel> modelSet = getImportedModels(definitions);
            for (WSDLModel wModel : modelSet) {
                populateAllModels(wModel);
            }
        }
    }

    private Set<WSDLModel> getImportedModels(Definitions definitions) throws CatalogModelException {
        Set<WSDLModel> importedModels = new HashSet<WSDLModel>();
        Collection<Import> importedWsdls = definitions.getImports();
        for (Import importedWsdl : importedWsdls) {
            WSDLModel wsdlModel = importedWsdl.getImportedWSDLModel();
            importedModels.add(wsdlModel);
        }
        return importedModels;
    }

    private WSDLModel getPrimaryModel(Node node)
            throws MalformedURLException, IOException, InvalidDataException {
        WSDLModel model = null;
        FileObject wsdlFO = null;
        if ( jaxWsSupport == null ){
            return null;
        }
        FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);

        if (wsdlFolder != null) {
            wsdlFO = wsdlFolder.getFileObject(service.getLocalWsdl());
            if (wsdlFO != null) { //found the wsdl
                ModelSource ms = Utilities.getModelSource(wsdlFO, true);
                model = WSDLModelFactory.getDefault().getModel(ms);
            } else { //wsdl not found, throw an exception
                notifyWsdlAbsence();
            }
        } else {
            notifyWsdlAbsence();
        }
        primaryDefinitions = model.getDefinitions();
        return model;
    }
    
    private void notifyWsdlAbsence() throws InvalidDataException {
        throw new InvalidDataException (  "WSDL not found" , NbBundle.
                getMessage(CustomizationWSEditor.class, "TXT_WsdlNotFound"));    // NOI18N 
    }

    public void cancel(Node node) {
        if (undoManager != null) {
            while (undoManager.canUndo()) {
                undoManager.undo();
            }
        }

        try {
            Set<WSDLModel> modelSet = wsdlModels.keySet();
            for (WSDLModel wsdlModel : modelSet) {
                ModelSource ms = wsdlModel.getModelSource();
                FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                DataObject wsdlDobj = DataObject.find(fo);
                wsdlDobj.setModified(wsdlModels.get(wsdlModel));
            }
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        }
        removeListeners();
    }

    public String getDescription() {
        return NbBundle.getMessage(CustomizationWSEditor.class, "WSDL_CUSTOMIZE_DESC");
    }
}
