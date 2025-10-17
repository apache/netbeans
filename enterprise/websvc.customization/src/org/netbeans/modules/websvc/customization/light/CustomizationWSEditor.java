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
            final ProgressHandle handle = ProgressHandle.createHandle
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
            wsdlModels.put(wsdlModel, dobj.isModified());
            return;
        } else {
            wsdlModels.put(wsdlModel, dobj.isModified());
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
