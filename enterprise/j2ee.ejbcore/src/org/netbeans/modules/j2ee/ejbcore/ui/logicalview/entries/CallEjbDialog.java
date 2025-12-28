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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.CallEjbGenerator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Handling of Call EJB dialog; used from CallEjbAction
 *
 * @author Martin Adamek
 */
public class CallEjbDialog {
    
    public boolean open(final FileObject referencingFO, final String referencingClassName, String title) throws IOException {
        Project enterpriseProject = FileOwnerQuery.getOwner(referencingFO);
        
        Project[] allProjects = Utils.getCallableEjbProjects(enterpriseProject);
        List<Node> ejbProjectNodes = new LinkedList<Node>();
        
        for (int i = 0; i < allProjects.length; i++) {
            Node projectView = new EjbsNode(allProjects[i]);
            ejbProjectNodes.add(new FilterNode(projectView, new EjbChildren(projectView)) {
                @Override
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            });
        }
        
        Children.Array children = new Children.Array();
        children.add(ejbProjectNodes.toArray(new Node[0]));
        Node root = new AbstractNode(children);
        root.setDisplayName(NbBundle.getMessage(CallEjbDialog.class, "LBL_EJBModules"));
        EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        boolean isJavaEE5orHigher = ProjectUtil.isJavaEE5orHigher(enterpriseProject);
        final CallEjbPanel panel = new CallEjbPanel(referencingFO, root, isJavaEE5orHigher ? null : erc.getServiceLocatorName(), referencingClassName);
        if (isJavaEE5orHigher) {
            panel.disableServiceLocator();
        }
        
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                title,
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(CallEjbPanel.class),
                null
                );
        NotificationLineSupport statusLine = dialogDescriptor.createNotificationLineSupport();
        panel.setNotificationLine(statusLine);
        
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(CallEjbPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if (newvalue instanceof Boolean) {
                        dialogDescriptor.setValid(((Boolean)newvalue));
                    }
                }
            }
        });
        
        panel.validateReferences();
        
        Object button = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (button != NotifyDescriptor.OK_OPTION) {
            return false;
        }
        Node ejbNode = panel.getEjb();
        final boolean throwExceptions = !panel.convertToRuntime();
        EjbReference ref = ejbNode.getLookup().lookup(EjbReference.class);
        String referenceNameFromPanel = panel.getReferenceName();
        if (referenceNameFromPanel != null && referenceNameFromPanel.trim().equals("")) {
            referenceNameFromPanel = null;
        }
        final FileObject fileObject = ejbNode.getLookup().lookup(FileObject.class);
        final Project nodeProject = FileOwnerQuery.getOwner(fileObject);
        
        boolean isDefaultRefName = panel.isDefaultRefName();
        final String referencedClassName = _RetoucheUtil.getJavaClassFromNode(ejbNode).getQualifiedName();

        final CallEjbGenerator generator = CallEjbGenerator.create(ref, referenceNameFromPanel, isDefaultRefName);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    final ElementHandle<? extends Element> elementHandle = generator.addReference(
                            referencingFO,
                            referencingClassName,
                            fileObject,
                            referencedClassName,
                            panel.getServiceLocator(),
                            panel.getSelectedInterface(),
                            throwExceptions,
                            nodeProject
                            );
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ElementOpen.open(referencingFO, elementHandle);
                        }
                    });
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        });

        
        return true;
    }

    private class EjbsNode extends AbstractNode {
        public EjbsNode(Project project) {
            super(new EJBListViewChildren(project), Lookups.singleton(project));
            J2eeModuleProvider module = project.getLookup().lookup(J2eeModuleProvider.class);
            if (module != null && module.getJ2eeModule().getType().equals(J2eeModule.Type.WAR)){
                setIconBaseWithExtension( "org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif" ); // NOI18N
            } else {
                setIconBaseWithExtension( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif" ); // NOI18N
            }
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
        }
    }
    
}
