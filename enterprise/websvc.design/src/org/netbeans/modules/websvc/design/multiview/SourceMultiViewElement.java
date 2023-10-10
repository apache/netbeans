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

package org.netbeans.modules.websvc.design.multiview;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.design.loader.JaxWsDataLoader;
import org.netbeans.modules.websvc.design.loader.JaxWsDataObject;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * The source editor element for JaxWs node.
 *
 * @author Ajit Bhate
 * @author changed by ads
 */
@MultiViewElement.Registration(
        displayName ="#LBL_sourceView_name",// NOI18N
    iconBase=JaxWsDataObject.CLASS_GIF,
    persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
    preferredID=MultiViewSupport.SOURCE_VIEW_ID,
    mimeType=JaxWsDataLoader.JAXWS_MIME_TYPE,            
    position=1000
        )
public class SourceMultiViewElement extends CloneableEditor
        implements MultiViewElement {
    private static final long serialVersionUID = 4403502726950453345L;
    private transient JComponent toolbar;
    private transient MultiViewElementCallback multiViewCallback;
    private transient Lookup myLookup;
    
    /**
     * Constructs a new instance of SourceMultiViewElement.
     */
    public SourceMultiViewElement() {
        // Needed for deserialization, do not remove.
        super();
        initialize();
    }
    
    /**
     * Constructs a new instance of SourceMultiViewElement.
     * 
     * @param support 
     */
    public SourceMultiViewElement(Lookup context) {
        super( context.lookup(DataEditorSupport.class));
        initialize();
   }
    
    private void initialize() {
        ShowComponentCookie showCookie = new ShowComponentCookie() {            
            public void show(Object param) {
                if(!(param instanceof ElementHandle)) return;
                final ElementHandle handle = (ElementHandle)param;
                try {
                    final JavaSource targetSource = JavaSource.forFileObject(getEditorSupport().getDataObject().getPrimaryFile());
                    final CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
                       public void run(WorkingCopy workingCopy) throws java.io.IOException {
                            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TypeElement webSvc = SourceUtils.
                                getPublicTopLevelElement(workingCopy);
                            
                            Element element = handle.resolve(workingCopy);
                            if ( element == null ){
                                return;
                            }
                            SourcePositions srcPos = workingCopy.getTrees().getSourcePositions();
                            int position = -1;
                            // use visitor approach later
                            if(ElementKind.METHOD == element.getKind()) {
                                Element webServiceMethod = workingCopy.
                                getElementUtilities().getImplementationOf(
                                        (ExecutableElement)element, webSvc);
                                if ( webServiceMethod instanceof ExecutableElement ){
                                    element = (ExecutableElement)webServiceMethod;
                                }
                                MethodTree methodTree = workingCopy.getTrees().getTree((ExecutableElement)element);
                                BlockTree methodBody = methodTree.getBody();
                                Tree tree = methodBody;
                                if(!methodBody.getStatements().isEmpty())
                                    tree = methodBody.getStatements().get(0);
                                position = (int) srcPos.getStartPosition(workingCopy.getCompilationUnit(), tree);
                            }
                            if(position>0) {
                                getEditorPane().setCaretPosition(position);
                            }
                        }
                        public void cancel() {
                        }
                    };
                    targetSource.runWhenScanFinished( new Task<CompilationController>() {
                        
                        @Override
                        public void run( CompilationController arg0 ) throws Exception {
                            targetSource.runModificationTask(task).commit();                            
                        }
                    }, true);
                    
                } catch (Exception ex) {
                }
            }
        };
        myLookup = Lookups.fixed(showCookie);
        
    }
    
    public JComponent getToolbarRepresentation() {
        JEditorPane editorPane = getEditorPane();
        if (editorPane != null) {
            Document doc = editorPane.getDocument();
            if (doc instanceof NbDocument.CustomToolbar) {
                if (toolbar == null) {
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
                }
                return toolbar;
            }
        }
        return null;
    }
    
    public JComponent getVisualRepresentation() {
        return this;
    }
    
    @Override
    public void updateName() {
        super.updateName();
        //update html displayname of the main multiview component
        // fix bug 122727
        updateMultiViewHtmlDisplayName();
    }
    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewCallback = callback;
        //set html displayname of the main multiview component
        updateMultiViewHtmlDisplayName();
    }
    
    private void updateMultiViewHtmlDisplayName() {
        if(multiViewCallback!=null) {
            if (EventQueue.isDispatchThread()) {
                multiViewCallback.getTopComponent().setHtmlDisplayName(getHtmlDisplayName());
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        multiViewCallback.getTopComponent().setHtmlDisplayName(getHtmlDisplayName());
                    }
                });
            }
        }
    }
    @Override
    public void componentActivated() {
        super.componentActivated();
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        DataObject dobj = getEditorSupport().getDataObject();
        if (dobj == null || !dobj.isValid()) {
            setActivatedNodes(new Node[] {});
        } else {
            setActivatedNodes(new Node[] {getEditorSupport().getDataObject().getNodeDelegate()});
        }
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        setActivatedNodes(new Node[] {});
    }
    
    @Override
    public void open() {
        if (multiViewCallback != null) {
            multiViewCallback.requestVisible();
        } else {
            super.open();
        }
        
    }
    
    @Override
    public void requestVisible() {
        if (multiViewCallback != null)
            multiViewCallback.requestVisible();
        else
            super.requestVisible();
    }
    
    @Override
    public void requestActive() {
        if (multiViewCallback != null)
            multiViewCallback.requestActive();
        else
            super.requestActive();
    }
    
    @Override
    protected String preferredID() {
        return getClass().getName();
    }
    
    
    @Override
    public UndoRedo getUndoRedo() {
        return super.getUndoRedo();
    }
    
    @Override
    protected boolean closeLast() {
        if(MultiViewSupport.getNumberOfClones(multiViewCallback.getTopComponent()) == 0) {
            // this is the last editor component so call super.closeLast
            return super.closeLast();
        }
        return true;
    }
    
    public CloseOperationState canCloseElement() {
        // if this is not the last cloned editor component, closing is OK
        if(!getEditorSupport().isModified() ||
                MultiViewSupport.getNumberOfClones(multiViewCallback.getTopComponent()) > 1) {
            return CloseOperationState.STATE_OK;
        }
        // return a state which will save/discard changes and is called by close handler
        AbstractAction save = new AbstractAction(){
                    public void actionPerformed(ActionEvent arg0) {
                        //save changes
                        try {
                            getEditorSupport().saveDocument();
                        } catch (IOException ex) {
                        }
                    }

                };
        save.putValue(Action.LONG_DESCRIPTION, NbBundle.getMessage(DataObject.class,
                            "MSG_SaveFile", // NOI18N
                            getEditorSupport().getDataObject().getPrimaryFile().getNameExt()));     
        return MultiViewFactory.createUnsafeCloseState(
                "ID_JAXWS_CLOSING", // NOI18N
                save,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    private DataEditorSupport getEditorSupport() {
        return (DataEditorSupport) cloneableEditorSupport();
    }

    @Override
    public Lookup getLookup() {
        return new ProxyLookup(super.getLookup(), myLookup, getEditorSupport().getDataObject().getLookup());
     }

}
