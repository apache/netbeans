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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author mkuchtiak
 */
@MultiViewElement.Registration(
    displayName="#TTL_" + DDDataObject.MULTIVIEW_SERVLETS,
    iconBase="org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif",
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=DDDataObject.DD_MULTIVIEW_PREFIX + DDDataObject.MULTIVIEW_SERVLETS,
    mimeType={DDDataLoader.REQUIRED_MIME_1, DDWeb25DataLoader.REQUIRED_MIME, DDWeb30DataLoader.REQUIRED_MIME,
        DDWebFragment30DataLoader.REQUIRED_MIME, DDWeb30DataLoader.REQUIRED_MIME_31, DDWebFragment30DataLoader.REQUIRED_MIME_31},
    position=600
)
public class ServletsMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {

    public static final int SERVLETS_ELEMENT_INDEX = 4;

    private static final Logger LOG = Logger.getLogger(ServletsMultiViewElement.class.getName());
    
    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private ServletPanelFactory factory;
    private javax.swing.Action addAction, removeAction;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String SERVLET_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_SERVLETS;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_SERVLETS;
    
    /** Creates a new instance of DDMultiViewElement */
    public ServletsMultiViewElement(Lookup context) {
        super(context.lookup(DDDataObject.class));
        this.dObj=context.lookup(DDDataObject.class);
        this.index=SERVLETS_ELEMENT_INDEX;
        comp = new ToolBarDesignEditor();
        factory = new ServletPanelFactory(comp, dObj);
        addAction = new AddAction(dObj, NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_addServlet"));
        removeAction = new RemoveAction(NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_remove"));
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
            if ( name.indexOf("Servlet")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with servlets
                MultiViewPerspective selected = dObj.getSelectedPerspective();
                if (selected != null && SERVLET_MV_ID.equals(selected.preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view = new ServletsView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ServletsView servletsView = (ServletsView)view;
            Node initialNode = servletsView.getServletsNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) 
                initialNode = ch.getNodes()[0];
            servletsView.selectNode(initialNode);
        }
        view.checkValidity();
        dObj.checkParseable();
    }

    class ServletsView extends SectionView {
        private SectionContainer servletsCont;
        private Node servletsNode;
        
        ServletsView (WebApp webApp) {
            super(factory);
            Servlet[] servlets = webApp.getServlet();
            Node[] nodes = new Node[servlets.length];
            Children ch = new Children.Array();
            for (int i=0;i<nodes.length;i++) {
                nodes[i] = new ServletNode(this,webApp,servlets[i]);
            }
            ch.add(nodes);
            servletsNode = new SectionContainerNode(ch);
            servletsCont = new SectionContainer(this,servletsNode,
                NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_servlets"),false);
            servletsCont.setHeaderActions(new javax.swing.Action[]{addAction});
            Children servletsChildren = new Children.Array();
            servletsChildren.add(new Node[]{servletsNode});
            AbstractNode root = new AbstractNode(servletsChildren);
            SectionPanel[] pan = new SectionPanel[servlets.length];
            for (int i=0;i<nodes.length;i++) {
                pan[i] = new SectionPanel(this, nodes[i], getServletTitle(servlets[i]),servlets[i]);
                pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                servletsCont.addSection(pan[i]);
            }
            addSection(servletsCont);
            //root.setDisplayName("<Servlets>");
            servletsNode.setDisplayName(NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_servlets"));
            servletsNode.setName(HELP_ID_PREFIX+"servletsNode"); //NOI18N
            setRoot(root);
        }
        
        Node getServletsNode() {
            return servletsNode;
        }
        
        SectionContainer getServletsContainer() {
            return servletsCont;
        }
        
        String getServletTitle(Servlet servlet) {
            String servletName=servlet.getServletName();
            if (servletName==null) servletName="";
            String mappings = DDUtils.urlPatternList(DDUtils.getUrlPatterns(webApp,servlet));
            return NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_servletPanel",servletName,mappings);
        }
        
        @Override
        public Error validateView() {
            Error error = getSectionView().getErrorPanel().getError();
            if (error != null){
                return error;
            }
            return SectionValidator.validateServlets(webApp);
        }
    }
    
    private static class ServletNode extends org.openide.nodes.AbstractNode {
        ServletNode(SectionView view, WebApp webApp, Servlet servlet) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(servlet.getServletName());
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"servletNode"); //NOI18N
        }
    }

    private class AddAction extends javax.swing.AbstractAction {
        
        AddAction(final DDDataObject dObj, String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_addServlet_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {

            String[] labels = new String[]{
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_servletName"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_servletClass"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_jspFile"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_description"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_urlPatterns")
            };
            String[] a11y_desc = new String[]{
                NbBundle.getMessage(ServletsMultiViewElement.class,"ACSD_servletName"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"ACSD_servletClass"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"ACSD_jspFile"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"ACSD_description"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"ACSD_urlPatterns")
            };
            boolean[] buttons = new boolean[]{false,true,true,false,false};
            SimpleDialogPanel.DialogDescriptor descriptor =
                    new SimpleDialogPanel.DialogDescriptor(labels, true);
            descriptor.setButtons(buttons);
            descriptor.setA11yDesc(a11y_desc);
            descriptor.setTextField(new boolean[]{true,true,true,false,true});
            
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            dialogPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ServletsMultiViewElement.class,"ACS_add_servlet"));
            dialogPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServletsMultiViewElement.class,"ACSD_add_servlet"));
            
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getJavaSourceGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String className = DDUtils.getResourcePath(groups,fo);
                            dialogPanel.getTextComponents()[1].setText(className);
                        }
                    } catch (java.io.IOException ex) {
                        LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                    }
                }
            });
            dialogPanel.getCustomizerButtons()[1].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String res = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                            dialogPanel.getTextComponents()[2].setText(res);
                        }
                    } catch (java.io.IOException ex) {
                        LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                    }
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_Servlet"),true) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String servletName = values[0].trim();
                    String servletClass = values[1].trim();
                    String jspFile = values[2].trim();
                    String urlPatterns = values[4];
                    return SectionValidator.validateNewServlet(dObj.getWebApp(), servletName, servletClass, jspFile, urlPatterns);
                }
            };
            dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[4].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[4].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                String[] values = dialogPanel.getValues();
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                try {
                    Servlet servlet = (Servlet)webApp.createBean("Servlet"); //NOI18N
                    servlet.setServletName(values[0].trim());
                    String servletClass = values[1].trim();
                    if (servletClass.length()>0) servlet.setServletClass(servletClass);
                    else servlet.setJspFile(values[2].trim());
                    String desc = values[3].trim();
                    if (desc.length()>0) servlet.setDescription(desc);
                    webApp.addServlet(servlet);
                    
                    String[] patterns = DDUtils.getStringArray(values[4]);
                    DDUtils.addServletMappings(webApp,servlet,patterns);

                    ServletsView view = (ServletsView)comp.getContentView();
                    Node node = new ServletNode(view, webApp, servlet);
                    view.getServletsNode().getChildren().add(new Node[]{node});

                    SectionPanel pan = new SectionPanel(view, node, view.getServletTitle(servlet), servlet);
                    pan.setHeaderActions(new javax.swing.Action[]{removeAction});
                    view.getServletsContainer().addSection(pan, true);
                } catch (ClassNotFoundException ex) {
                    LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                }
                finally {
                    dObj.setChangedFromUI(false);
                }
            }
        }
    }
    private class RemoveAction extends javax.swing.AbstractAction {
        
        RemoveAction(String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            org.openide.DialogDescriptor desc = new ConfirmDialog(
                NbBundle.getMessage(ServletsMultiViewElement.class,"TXT_removeServletConfirm"));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
                Servlet servlet = (Servlet)sectionPanel.getKey();
                // updating data model
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                try {
                    DDUtils.removeServletMappings(webApp,servlet.getServletName());
                    DDUtils.removeFilterMappingsForServlet(webApp,servlet.getServletName());
                    webApp.removeServlet(servlet);

                    // removing section
                    sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
                } finally {
                    dObj.setChangedFromUI(false);
                }
            }
        }
    }
}
