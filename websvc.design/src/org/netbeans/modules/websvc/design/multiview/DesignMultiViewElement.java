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
