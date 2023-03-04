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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/** ResourcesMultiViewElement.java - Multi View Element for Resources :
 * - env-entries
 * - resource-refs
 * - resource-env-refs
 * - ejb-refs
 * - message-destination-refs
 * 
 * Created on April 11, 2005
 * @author mkuchtiak
 */
@MultiViewElement.Registration(
    displayName="#TTL_" + DDDataObject.MULTIVIEW_REFERENCES,
    iconBase="org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif",
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=DDDataObject.DD_MULTIVIEW_PREFIX + DDDataObject.MULTIVIEW_REFERENCES,
    mimeType={DDDataLoader.REQUIRED_MIME_1, DDWeb25DataLoader.REQUIRED_MIME, 
        DDWeb30DataLoader.REQUIRED_MIME, DDWebFragment30DataLoader.REQUIRED_MIME, 
        DDWeb30DataLoader.REQUIRED_MIME_31, DDWebFragment30DataLoader.REQUIRED_MIME_31,
        DDWeb40DataLoader.REQUIRED_MIME_40, DDWebFragment40DataLoader.REQUIRED_MIME_40, 
        DDWeb50DataLoader.REQUIRED_MIME_50, DDWebFragment50DataLoader.REQUIRED_MIME_50, 
        DDWeb60DataLoader.REQUIRED_MIME_60, DDWebFragment60DataLoader.REQUIRED_MIME_60},
    position=900
)
public class ReferencesMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {

    public static final int REFERENCES_ELEMENT_INDEX = 10;

    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private ReferencesFactory factory;
    private javax.swing.Action addAction, removeAction;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String REFERENCES_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_REFERENCES;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_REFERENCES;
    
    /** Creates a new instance of DDMultiViewElement */
    public ReferencesMultiViewElement(Lookup context) {
        super(context.lookup(DDDataObject.class));
        this.dObj=context.lookup(DDDataObject.class);
        this.index=REFERENCES_ELEMENT_INDEX;
        comp = new ToolBarDesignEditor();
        factory = new ReferencesFactory(comp, dObj);
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
    }
    
    public SectionView getSectionView() {
        return view;
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView(index);
        if (needInit || !dObj.isDocumentParseable()) {
            repaintView();
            needInit=false;
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view = new ReferencesView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ReferencesView referencesView = (ReferencesView)view;
            Node initialNode = referencesView.getResRefsNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) 
                initialNode = ch.getNodes()[0];
            referencesView.selectNode(initialNode);
        }
        view.checkValidity();
        dObj.checkParseable();
        
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        dObj.getWebApp().addPropertyChangeListener(this);
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        dObj.getWebApp().removePropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("Ref")>0 || name.indexOf("EnvEntry")>0) { //NOI18
                // repaint view if the wiew is active and something is changed with references
                if (REFERENCES_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }

    class ReferencesView extends SectionView {
        private Node envEntriesNode, resRefsNode, resEnvRefsNode, ejbRefsNode, messageDestRefsNode;
        
        ReferencesView (WebApp webApp) {
            super(factory);
            envEntriesNode = new EnvEntriesNode();
            addSection(new SectionPanel(this,envEntriesNode,"env_entries")); //NOI18N
            
            resRefsNode = new ResRefsNode();
            addSection(new SectionPanel(this,resRefsNode,"res_refs")); //NOI18N

            resEnvRefsNode = new ResEnvRefsNode();
            addSection(new SectionPanel(this,resEnvRefsNode,"res_env_refs")); //NOI18N
            
            ejbRefsNode = new EjbRefsNode();
            addSection(new SectionPanel(this,ejbRefsNode,"ejb_refs")); //NOI18N
            
            messageDestRefsNode = new MessageDestRefsNode();
            addSection(new SectionPanel(this,messageDestRefsNode,"message_dest_refs")); //NOI18N
            
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{envEntriesNode,resRefsNode,resEnvRefsNode,ejbRefsNode,messageDestRefsNode}); 
            AbstractNode root = new AbstractNode(rootChildren);
            setRoot(root);
        }
        
        Node getEnvEntriesNode() {
            return envEntriesNode;
        }
        
        Node getResRefsNode() {
            return resRefsNode;
        }
        
        Node getResEnvRefsNode(){
            return resEnvRefsNode;
        }
        
        Node getEjbRefsNode() {
            return ejbRefsNode;
        }
        
        Node getMessageDestRefsNode() {
            return messageDestRefsNode;
        }
    }
    
    private static class EnvEntriesNode extends org.openide.nodes.AbstractNode {
        EnvEntriesNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_EnvEntries"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode.gif"); //NOI18N
        }    
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"envEntriesNode"); //NOI18N
        }
    }
    
    private static class ResRefsNode extends org.openide.nodes.AbstractNode {
        ResRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_ResRefs"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"resRefsNode"); //NOI18N
        }
    }
    
    private static class ResEnvRefsNode extends org.openide.nodes.AbstractNode {
        ResEnvRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_ResEnvRefs"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"resEnvRefsNode"); //NOI18N
        }
    }
    
    private static class EjbRefsNode extends org.openide.nodes.AbstractNode {
        EjbRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_EjbRefs"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"ejbRefsNode"); //NOI18N
        }
    }
    
    private static class MessageDestRefsNode extends org.openide.nodes.AbstractNode {
        MessageDestRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_MessageDestRefs"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"messageDestRefsNode"); //NOI18N
        }
    }
}
