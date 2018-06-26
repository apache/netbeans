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
        children.add(ejbProjectNodes.toArray(new Node[ejbProjectNodes.size()]));
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
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dialogDescriptor.setValid(((Boolean)newvalue).booleanValue());
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
