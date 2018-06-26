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
 *
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataLoader;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.DDWeb25DataLoader;
import org.netbeans.modules.j2ee.ddloaders.web.DDWeb30DataLoader;
import org.netbeans.modules.j2ee.ddloaders.web.DDWebFragment30DataLoader;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.ConfirmDialog;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * SecurityMultiViewElement.java
 *
 * Multiview element for creating the Security view.
 *
 * @author ptliu
 */
@MultiViewElement.Registration(
    displayName="#TTL_" + DDDataObject.MULTIVIEW_SECURITY,
    iconBase="org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif",
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=DDDataObject.DD_MULTIVIEW_PREFIX + DDDataObject.MULTIVIEW_SECURITY,
    mimeType={DDDataLoader.REQUIRED_MIME_1, DDWeb25DataLoader.REQUIRED_MIME, DDWeb30DataLoader.REQUIRED_MIME,
        DDWebFragment30DataLoader.REQUIRED_MIME, DDWeb30DataLoader.REQUIRED_MIME_31, DDWebFragment30DataLoader.REQUIRED_MIME_31},
    position=1000
)
public class SecurityMultiViewElement extends ToolBarMultiViewElement
        implements PropertyChangeListener {

    public static final int SECURITY_ELEMENT_INDEX = 12;

    private SecurityView view;
    private DDDataObject dObj;
    private ToolBarDesignEditor editor;
    private SecurityFactory factory;
    private RequestProcessor.Task repaintingTask;
    private WebApp webApp;
    private AddConstraintAction addConstraintAction;
    private RemoveConstraintAction removeConstraintAction;
    private int index;
    private boolean needInit = true;
    
    private static final String SECURITY_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_SECURITY;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_SECURITY;
    
    /** Creates a new instance of SecurityMultiViewElement */
    public SecurityMultiViewElement(Lookup context) {
        super(context.lookup(DDDataObject.class));
        this.dObj = context.lookup(DDDataObject.class);
        this.index = SECURITY_ELEMENT_INDEX;
        editor = new ToolBarDesignEditor();
        factory = new SecurityFactory(editor, dObj);
        addConstraintAction = new AddConstraintAction(dObj);
        removeConstraintAction = new RemoveConstraintAction();
        
        setVisualEditor(editor);
        
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
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("LoginConfig") > 0 || name.indexOf("Security") > 0 ) { //NOI18N
                // repaint view if the view is active and something is changed with the security view
                MultiViewPerspective perspective = dObj.getSelectedPerspective();
                if (perspective != null && SECURITY_MV_ID.equals(perspective.preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view = new SecurityView(webApp);
        editor.setContentView(view);
        
        Object lastActive = editor.getLastActive();
        if (lastActive != null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            SecurityView securityView = (SecurityView)view;
            
            Node initialNode = view.getRolesNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount() > 0)
                initialNode = ch.getNodes()[0];
            view.selectNode(initialNode);
        }
        view.checkValidity();
        dObj.checkParseable();
        
    }
    
    private class SecurityView extends SectionView {
        private SecurityRolesNode rolesNode;
        private SectionContainerNode constraintsNode;
        private SectionContainer constraintsContainer;
        private LoginConfigNode configNode;
        
        public SecurityView(WebApp webApp) {
            super(factory);
            
            configNode = new LoginConfigNode();
            addSection(new SectionPanel(this, configNode, "login_config")); //NOI18N
            
            rolesNode = new SecurityRolesNode();
            addSection(new SectionPanel(this, rolesNode, "security_roles")); //NOI18N
            
            SecurityConstraint[] constraints = webApp.getSecurityConstraint();
            Node[] nodes = new Node[constraints.length];
            Children ch = new Children.Array();
            
            for (int i=0; i < nodes.length;i++) {
                nodes[i] = new SecurityConstraintNode(constraints[i]);
            }
            
            ch.add(nodes);
            constraintsNode = new SectionContainerNode(ch);
            constraintsContainer = new SectionContainer(this, constraintsNode,
                    NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_SecurityConstraints"),false);
            
            constraintsContainer.setHeaderActions(new javax.swing.Action[]{addConstraintAction});
            
            SectionPanel[] pan = new SectionPanel[constraints.length];
            
            for (int i=0; i < nodes.length;i++) {
                pan[i] = new SectionPanel(this, nodes[i], constraints[i]);
                pan[i].setHeaderActions(new javax.swing.Action[]{removeConstraintAction});
                constraintsContainer.addSection(pan[i]);
            }
            
            addSection(constraintsContainer);
            constraintsNode.setDisplayName(NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_SecurityConstraints"));
            
            ch = new Children.Array();
            ch.add(new Node[] {configNode, rolesNode, constraintsNode});
            AbstractNode root = new AbstractNode(ch);
            setRoot(root);
        }
        
        public SecurityRolesNode getRolesNode() {
            return rolesNode;
        }
        
        public SectionContainerNode getConstraintsNode() {
            return constraintsNode;
        }
        
        public SectionContainer getConstraintsContainer() {
            return constraintsContainer;
        }
    }
    
    private static class SecurityRolesNode extends AbstractNode {
        public SecurityRolesNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(SecurityMultiViewElement.class,"TTL_SecurityRoles"));
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"securityrolesNode"); //NOI18N
        }
    }
    
    private static class SecurityConstraintNode extends AbstractNode {
        
        public SecurityConstraintNode(SecurityConstraint constraint) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(constraint.getDefaultDisplayName());
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"securityconstraintsNode"); //NOI18N
        }
    }
    
    private static class LoginConfigNode extends AbstractNode {
        public LoginConfigNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(SecurityMultiViewElement.class,"TTL_LoginConfig"));
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"loginconfigNode"); //NOI18N
        }
    }
    
    private class AddConstraintAction extends javax.swing.AbstractAction {
        
        AddConstraintAction(final DDDataObject dObj) {
            super(NbBundle.getMessage(SecurityMultiViewElement.class,"LBL_AddSecurityConstraint"));
            char mnem = NbBundle.getMessage(SecurityMultiViewElement.class,"LBL_AddSecurityConstraint_mnem").charAt(0);
            
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            dObj.modelUpdatedFromUI();
            dObj.setChangedFromUI(true);
            
            try {
                SecurityConstraint constraint = (SecurityConstraint) webApp.createBean("SecurityConstraint");  //NOI18N
                constraint.setDisplayName(getUniqueDefaultName());
                webApp.addSecurityConstraint(constraint);
                
                SecurityView view = (SecurityView) editor.getContentView();
                Node node = new SecurityConstraintNode(constraint);
                view.getConstraintsNode().getChildren().add(new Node[]{node});
                
                SectionPanel pan = new SectionPanel(view, node, constraint);
                pan.setHeaderActions(new javax.swing.Action[]{removeConstraintAction});
                view.getConstraintsContainer().addSection(pan, true);
            } catch (ClassNotFoundException ex) {
            }
        }
        
        private String getUniqueDefaultName() {
            int counter = 0;
            String defaultName = NbBundle.getMessage(SecurityMultiViewElement.class,
                    "TXT_DefaultConstraintName");
            SecurityConstraint[] constraints = webApp.getSecurityConstraint();
            
            while (true) {
                String defaultNameEx = defaultName + (++counter);
                
                boolean found = false;
                for (int i = 0; i < constraints.length; i++) {
                    if (defaultNameEx.equals(constraints[i].getDefaultDisplayName())) {
                        found = true;
                    }
                }
                
                if (!found) return defaultNameEx;
            } 
        }
    }
    
    
    private class RemoveConstraintAction extends javax.swing.AbstractAction {
        
        RemoveConstraintAction() {
            super(NbBundle.getMessage(SecurityMultiViewElement.class,"LBL_remove"));
            char mnem = NbBundle.getMessage(SecurityMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            org.openide.DialogDescriptor desc = new ConfirmDialog(
                    NbBundle.getMessage(SecurityMultiViewElement.class,"TXT_RemoveSecurityConstraintConfirm"));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
                SecurityConstraint constraint = (SecurityConstraint) sectionPanel.getKey();
                // updating data model
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                try {
                    webApp.removeSecurityConstraint(constraint);
                    
                    // removing section
                    sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
                } finally {
                    dObj.setChangedFromUI(false);
                }
            }
        }
    }
}
