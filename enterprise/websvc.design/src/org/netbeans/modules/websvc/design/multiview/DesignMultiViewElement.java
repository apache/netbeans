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

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.websvc.design.loader.JaxWsDataLoader;
import org.netbeans.modules.websvc.design.loader.JaxWsDataObject;
import org.netbeans.modules.websvc.design.navigator.WSDesignNavigatorHint;
import org.netbeans.modules.websvc.design.view.DesignView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Ajit Bhate
 * @author changed by ads
 */
@MultiViewElement.Registration(
    displayName ="#LBL_designView_name",// NOI18N
    iconBase=JaxWsDataObject.CLASS_GIF,
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=MultiViewSupport.DESIGN_VIEW_ID,
    mimeType=JaxWsDataLoader.JAXWS_MIME_TYPE,            
    position=2000
)
public class DesignMultiViewElement extends TopComponent
        implements MultiViewElement {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private transient MultiViewElementCallback multiViewCallback;
    private transient DesignView designView;
    private transient DataObject dataObject;
    private transient Lookup myLookup;
    
    public DesignMultiViewElement() {
        super();
        initialize();
    }

    /**
     * 
     * @param mvSupport 
     */
    public DesignMultiViewElement(Lookup context) {
        this.dataObject = context.lookup(JaxWsDataObject.class);
        initialize();
    }
    
    private void initialize() {
        myLookup = Lookups.fixed(new WSDesignNavigatorHint());
    }
    
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
    
    /**
     * Initializes the UI. Here it checks for the state of the underlying
     * schema model. If valid, draws the UI, else empties the UI with proper
     * error message.
     */
    private void initUI() {
        removeAll();
        setLayout(new BorderLayout());
        MultiViewSupport mvSupport = dataObject.getCookie(MultiViewSupport.class);
        if (mvSupport!=null && mvSupport.getService()!=null) {
            designView = new DesignView(mvSupport.getService(),mvSupport.getImplementationBean());
            add(designView);
        } else {
            JLabel emptyLabel = new JLabel("The design view can not be rendered. Please switch to source view.");
            add(emptyLabel,BorderLayout.CENTER);
        }
    }
    
    
    @Override
    public void componentActivated() {
        super.componentActivated();
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        if ( designView != null ){
            designView.flushContent();
        }
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        // create UI, this will be moved to componentShowing for refresh/sync
        initUI();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        if ( designView != null ){
            designView.flushContent();
        }
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        setActivatedNodes(dataObject.isValid() ? new Node[]{dataObject.getNodeDelegate()} : new Node[]{});
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        setActivatedNodes(new Node[] {});
        if ( designView != null ){
            designView.flushContent();
        }
    }
    
    public JComponent getToolbarRepresentation() {
        if(designView!=null) {
            return designView.getToolbarRepresentation();
        }
        else {
            return new JPanel();
        }
    }
    
    @Override
    public UndoRedo getUndoRedo() {
        return super.getUndoRedo();
    }
    
    public JComponent getVisualRepresentation() {
        return this;
    }
    
    @Override
    public Lookup getLookup() {
        return new ProxyLookup(super.getLookup(), myLookup);
     }

    
}
