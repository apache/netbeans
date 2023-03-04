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

import java.math.BigDecimal;
import org.netbeans.core.api.multiview.MultiViewPerspective;
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

/**
 * @author mkuchtiak
 */
@MultiViewElement.Registration(
    displayName="#TTL_" + DDDataObject.MULTIVIEW_OVERVIEW,
    iconBase="org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif",
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=DDDataObject.DD_MULTIVIEW_PREFIX + DDDataObject.MULTIVIEW_OVERVIEW,
    mimeType={DDDataLoader.REQUIRED_MIME_1, DDWeb25DataLoader.REQUIRED_MIME, 
        DDWeb30DataLoader.REQUIRED_MIME, DDWebFragment30DataLoader.REQUIRED_MIME, 
        DDWeb30DataLoader.REQUIRED_MIME_31, DDWebFragment30DataLoader.REQUIRED_MIME_31, 
        DDWeb40DataLoader.REQUIRED_MIME_40, DDWebFragment40DataLoader.REQUIRED_MIME_40, 
        DDWeb50DataLoader.REQUIRED_MIME_50, DDWebFragment50DataLoader.REQUIRED_MIME_50, 
        DDWeb60DataLoader.REQUIRED_MIME_60, DDWebFragment60DataLoader.REQUIRED_MIME_60},
    position=500
)
public class OverviewMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {

    public static final int OVERVIEW_ELEMENT_INDEX = 2;

    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private OverviewFactory factory;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String OVERVIEW_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_OVERVIEW; 
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_OVERVIEW;
    
    /** Creates a new instance of DDMultiViewElement */
    public OverviewMultiViewElement(Lookup context) {
        super(context.lookup(DDDataObject.class));
        this.dObj=context.lookup(DDDataObject.class);
        this.index=OVERVIEW_ELEMENT_INDEX;
        comp = new ToolBarDesignEditor();
        factory = new OverviewFactory(comp, dObj);
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
        view =new OverView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ((SectionView)view).openPanel("overview"); //NOI18N
        }
        view.checkValidity();
        dObj.checkParseable();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        if (dObj != null && dObj.getWebApp() != null) {
            dObj.getWebApp().addPropertyChangeListener(this);
        }
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        if (dObj != null && dObj.getWebApp() != null) {
            dObj.getWebApp().removePropertyChangeListener(this);
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("/WebApp/DisplayName")>=0 || //NOI18N
                 name.indexOf("/WebApp/Description")>=0 || //NOI18N
                 name.indexOf("/WebApp/Name") >=0 || //NOI18N
                 name.indexOf("Distributable")>0 || //NOI18N
                 name.indexOf("ContextParam")>0 || //NOI18N
                 name.indexOf("Listener")>0 || //NOI18N
                 name.indexOf("SessionConfig")>0 ) { //NOI18N
                // repaint view if the wiew is active and something is changed with elements listed above
                MultiViewPerspective selectedPerspective = dObj.getSelectedPerspective();
                if (selectedPerspective != null && OVERVIEW_MV_ID.equals(selectedPerspective.preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }

    class OverView extends SectionView {
        private Node overviewNode, absoluteOrderingNode, relativeOrderingNode;
        private Node contextParamsNode, listenersNode;
        OverView(WebApp webApp) {
            super(factory);
            overviewNode = new OverviewNode();
            addSection(new SectionPanel(this,overviewNode,"overview")); //NOI18N

            String ver = webApp.getVersion();
            boolean jee6 = (ver == null) ? false :
                new BigDecimal(ver).compareTo(new BigDecimal(3.0)) >= 0;
            boolean fragment = webApp instanceof WebFragment;
            if (jee6) {
                if (fragment) {
                    relativeOrderingNode = new RelativeOrderingNode();
                    addSection(new SectionPanel(this, relativeOrderingNode, "relativeOrdering")); //NOI18N
                }
                else {
                    absoluteOrderingNode = new AbsoluteOrderingNode();
                    addSection(new SectionPanel(this, absoluteOrderingNode, "absoluteOrdering")); //NOI18N
                }
            }

            contextParamsNode = new ContextParamsNode();
            addSection(new SectionPanel(this,contextParamsNode,"context_params")); //NOI18N

            listenersNode = new ListenersNode();
            addSection(new SectionPanel(this,listenersNode,"listeners")); //NOI18N

            Children rootChildren = new Children.Array();
            if (jee6) {
                if (fragment)
                    rootChildren.add(new Node[]{overviewNode,relativeOrderingNode,contextParamsNode,listenersNode});
                else
                    rootChildren.add(new Node[]{overviewNode,absoluteOrderingNode,contextParamsNode,listenersNode});
            }
            else
                rootChildren.add(new Node[]{overviewNode,contextParamsNode,listenersNode});
            AbstractNode root = new AbstractNode(rootChildren);
            setRoot(root);
        }
    }
    
    private static class OverviewNode extends org.openide.nodes.AbstractNode {
        OverviewNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_Overview"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class.gif"); //NOI18N
        }    
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"overviewNode"); //NOI18N
        }
    }

    private static class AbsoluteOrderingNode extends org.openide.nodes.AbstractNode {
        AbsoluteOrderingNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_Ordering"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramsNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"absoluteOrderingNode"); //NOI18N
        }
    }

    private static class RelativeOrderingNode extends org.openide.nodes.AbstractNode {
        RelativeOrderingNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_Ordering"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramsNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"relativeOrderingNode"); //NOI18N
        }
    }
    
    private static class ContextParamsNode extends org.openide.nodes.AbstractNode {
        ContextParamsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_ContextParams"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramsNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"contextParamsNode"); //NOI18N
        }
    }
    
    private static class ListenersNode extends org.openide.nodes.AbstractNode {
        ListenersNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_Listeners"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"listenersNode"); //NOI18N
        }
    }
}
