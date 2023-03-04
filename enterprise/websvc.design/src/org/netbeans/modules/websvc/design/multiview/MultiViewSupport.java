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
package org.netbeans.modules.websvc.design.multiview;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.Action;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.ProjectService;
import org.netbeans.modules.websvc.design.loader.JaxWsDataLoader;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.util.Lookup;

/**
 * Class for creating the Multiview
 * @author Ajit Bhate
 */
public class MultiViewSupport implements OpenCookie, EditCookie {

    static final long serialVersionUID = 1L;
    private DataObject dataObject;
    private ProjectService service;
    //private DataObject wsdlDo;
    public static String SOURCE_UNSAFE_CLOSE = "SOURCE_UNSAFE_CLOSE";
    //private static String DESIGN_UNSAFE_CLOSE = "DESIGN_UNSAFE_CLOSE";
    
    public static final String DESIGN_VIEW_ID = "webservice-designview";  // NOI18N
    public static final String SOURCE_VIEW_ID = "webservice-sourceview";

    /**
     * MultiView enum
     */
    public enum View {

        /**
         * Source multiview
         */
        SOURCE,
        /**
         * Design multiview
         */
        DESIGN,
        /**
         * WSDL Preview multiview
         */
        PREVIEW,
    }

    /**
     * Constructor for deserialization
     */
    public MultiViewSupport() {
    }
    static Logger l = Logger.getLogger(MultiViewSupport.class.getName());

    /**
     * Constructor
     * @param displayName
     * @param dataObject
     */
    public MultiViewSupport(ProjectService service, DataObject dataObject) {
        this.dataObject = dataObject;
        this.service = service;
        //initWsdlDO();
    }

    public void open() {
        view(View.DESIGN);
    }

    public void edit() {
        view(View.SOURCE);
    }

    public void preview() {
        view(View.PREVIEW);
    }

    DataObject getDataObject() {
        return dataObject;
    }

    private DataEditorSupport getEditorSupport() {
        return dataObject.getLookup().lookup(DataEditorSupport.class);
    }

    ProjectService getService() {
        return service;
    }

    FileObject getImplementationBean() {
        return getDataObject().getPrimaryFile();
    }

    /**
     * Create the Multiview, doc into the editor window and open it.
     * @return CloneableTopComponent new multiview.
     */
    public CloneableTopComponent createMultiView() {
        /*MultiViewDescription views[];
        if (service != null && service.getLocalWsdlFile() != null) {
            views = new MultiViewDescription[2];

            // Put the source element first so that client code can find its
            // CloneableEditorSupport.Pane implementation.
            views[0] = new SourceMultiViewDesc(getDataObject());
            views[1] = new DesignMultiViewDesc(getDataObject());
            //views[2] = new PreviewMultiViewDesc(wsdlDo);
        } else {
             views = new MultiViewDescription[2];

            // Put the source element first so that client code can find its
            // CloneableEditorSupport.Pane implementation.
            views[0] = new SourceMultiViewDesc(getDataObject());
            views[1] = new DesignMultiViewDesc(getDataObject());
            //views[2] = new PreviewMultiViewDesc(getDataObject(), service);
        }
        
        // Make the column view the default element.
        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                views[0], new CloseHandler(getDataObject()));*/
        CloneableTopComponent multiview = MultiViews.createCloneableMultiView(
                JaxWsDataLoader.JAXWS_MIME_TYPE, dataObject);

        String displayName = getDataObject().getNodeDelegate().getDisplayName();
        multiview.setDisplayName(displayName);
        multiview.setName(displayName);

        return multiview;
    }

    /**
     *
     * @param view
     * @param param
     */
    public void view(final View view, final Object... param) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    viewInSwingThread(view, param);
                }
            });
        } else {
            viewInSwingThread(view, param);
        }
    }

    private void viewInSwingThread(View view, Object... parameters) {
        getEditorSupport().open();
        switch (view) {
            case SOURCE:
                requestMultiviewActive(SOURCE_VIEW_ID);
                break;
            case DESIGN:
                requestMultiviewActive(DESIGN_VIEW_ID);
                break;
            case PREVIEW:
                requestMultiviewActive(PreviewMultiViewDesc.PREFERRED_ID);
                break;
        }
        if (parameters != null && parameters.length > 0) {
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ShowComponentCookie cake = activeTC.getLookup().lookup(ShowComponentCookie.class);
            if (cake != null) {
                cake.show(parameters[0]);
            }
        }
    }

    /**
     * Shows the desired multiview element. Must be called after the editor
     * has been opened (i.e. SchemaEditorSupport.open()) so the TopComponent
     * will be the active one in the registry.
     *
     * @param  id      identifier of the multiview element.
     */
    private static void requestMultiviewActive(String id) {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(activeTC);
        if (handler != null) {
            MultiViewPerspective[] perspectives = handler.getPerspectives();
            for (MultiViewPerspective perspective : perspectives) {
                if (perspective.preferredID().equals(id)) {
                    handler.requestActive(perspective);
                }
            }
        }
    }

    /**
     * Returns true if the given TopComponent is the last one in the
     * set of cloneable windows.
     *
     * @param  tc  TopComponent.
     * @return  -1 if not a cloneabletopcomponent
     *          otherwise number of clones including self
     */
    public static int getNumberOfClones(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent)) {
            return -1;
        }
        return Collections.list(((CloneableTopComponent) tc).getReference().getComponents()).size();
    }

    /**
     *  Method, preparing DataObject for processing by WSDL Preview element
     * @param service - web service object, initialized by class constructor
     */
    /*private void initWsdlDO() {

        if (service == null) {
            return;
        }
        DataObject dataObj = null;  // DataObject created from FileObject of WSDL file - null if WSDL don't exist
        FileObject wsdlFile = null;        // FileObject of WSDL file
//        String tempdir = System.getProperty("java.io.tmpdir");      // Tempdir
        FileObject primaryFile = getImplementationBean();
        String localWSDLFilePath = service.getLocalWsdlFile();      // Local path to wsdl file,only part of path for URL wsdl
//        String serviceName = service.getName();                     // Web service name

        // Detection if this is WSDL or Java case - later sets this propery null
        if (!(localWSDLFilePath == null)) {
            // Process of obtaining proper path to wsdl through JAXWSSupport and its methods,
            // which leads to desired FileObject
            JAXWSSupport jAXWSSupport = JAXWSSupport.getJAXWSSupport(primaryFile);
            FileObject foj = jAXWSSupport.getLocalWsdlFolderForService(service.getName(), false);
            wsdlFile = foj.getFileObject(localWSDLFilePath);
            // If obtaining of WSDL file fails, empty page with error label is displayed
            if (wsdlFile != null) {
                try {
                    dataObj = DataObject.find(wsdlFile);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }           
        //wsdlDo = dataObj;
    }*/

    /**
     * Implementation of CloseOperationHandler for multiview. Ensures the
     * editors correctly closed, data object is saved, etc. Holds a
     * reference to DataObject only - to be serializable with the multiview
     * TopComponent without problems.
     */
/*    @MimeRegistration(mimeType=JaxWsDataLoader.JAXWS_MIME_TYPE, 
            service=CloseOperationHandler.class)
    public static class CloseHandler implements CloseOperationHandler, Serializable {

        private static final long serialVersionUID = -3838395157610633251L;
        private DataObject sourceDataObject;

        private CloseHandler() {
            int a =0;
        }

        public CloseHandler(DataObject dataObject) {
            this.sourceDataObject = dataObject;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            StringBuilder message = new StringBuilder();
            for (CloseOperationState state : elements) {
                if (state.getCloseWarningID().equals(SOURCE_UNSAFE_CLOSE)) {
                    message.append(NbBundle.getMessage(DataObject.class,
                            "MSG_SaveFile", // NOI18N
                            sourceDataObject.getPrimaryFile().getNameExt()));
                    message.append("\n");
                }
            }
            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message.toString().trim());
            Object retVal = DialogDisplayer.getDefault().notify(desc);
            for (CloseOperationState state : elements) {
                Action act = null;
                if (retVal == NotifyDescriptor.YES_OPTION) {
                    act = state.getProceedAction();
                } else if (retVal == NotifyDescriptor.NO_OPTION) {
                    act = state.getDiscardAction();
                } else {
                    return false;
                }
                if (act != null) {
                    act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                }
            }
            return true;
        }
    }
*/
}
