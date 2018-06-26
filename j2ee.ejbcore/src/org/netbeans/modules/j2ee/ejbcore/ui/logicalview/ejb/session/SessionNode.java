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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbTransferable;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceActionGroup;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Chris Webster
 * @author Ludovic Champenois
 * @author Martin Adamek
 */
public final class SessionNode extends AbstractNode implements OpenCookie, PropertyChangeListener {
    private final EjbViewController ejbViewController;
    
    public static SessionNode create(final String ejbClass, final EjbJar ejbModule, Project project) {
        SessionNode node = null;
        try {
            node = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, SessionNode>() {
                public SessionNode run(EjbJarMetadata metadata) throws Exception {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    if (ejb != null && ejb.getEjbName() != null){
                        SessionNode node = new SessionNode(new InstanceContent(), new EjbViewController(ejbClass, ejbModule), ejb.getEjbName());
                        ejb.addPropertyChangeListener(WeakListeners.propertyChange(node, ejb));
                        return node;
                    }
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return node;
    }
    
    private SessionNode(InstanceContent instanceContent, EjbViewController controller, String ejbName) {
        super(new SessionChildren(controller), new AbstractLookup(instanceContent));
        ejbViewController = controller;
        setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/session/SessionNodeIcon.gif");
        setName(ejbName + "");
        setDisplayName();
        instanceContent.add(this);
        ElementHandle<TypeElement> beanClassHandle = ejbViewController.getBeanClass();
        if (beanClassHandle != null) {
            instanceContent.add(beanClassHandle);
        }
        DataObject dataObject = ejbViewController.getBeanDo();
        if (dataObject != null && dataObject.getPrimaryFile() != null) {
            instanceContent.add(dataObject.getPrimaryFile());
        }
        try {
            instanceContent.add(ejbViewController.createEjbReference());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void setDisplayName() {
        setDisplayName(ejbViewController.getDisplayName());
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (Session.EJB_NAME.equals(pce.getPropertyName())){
            setDisplayName();
        }
    }

    public Action[] getActions(boolean context) {
        int nodesCount = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().size();
        List<SystemAction> list = new ArrayList<SystemAction>();
        list.add(SystemAction.get(OpenAction.class));
        if (nodesCount == 1) {
            list.add(null);
            list.add(SystemAction.get(AddActionGroup.class));
            list.add(null);
            list.add(SystemAction.get(GoToSourceActionGroup.class));
        }
        return list.toArray(new SystemAction[list.size()]);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public void destroy() throws IOException {
        super.destroy();
        String deleteOptions = DeleteEJBDialog.open(ejbViewController.getDisplayName());
        if (!deleteOptions.equals(DeleteEJBDialog.DELETE_NOTHING)) {
            if (deleteOptions.equals(DeleteEJBDialog.DELETE_ONLY_DD)) {
                ejbViewController.delete(false);
            } else {
                ejbViewController.delete(true);
            }
        }
    }
    
    public boolean canCopy() {
        return true;
    }
    
    public boolean canCut() {
        return true;
    }
    
    public Transferable clipboardCopy() throws IOException {
        EjbReference ejbRef = ejbViewController.createEjbReference();
        StringBuffer ejbRefString = new StringBuffer();
        ejbRefString.append(ejbViewController.getLocalStringRepresentation("Session"));
        return new EjbTransferable(ejbRefString.toString(), ejbRef);
    }
    
    public Transferable clipboardCut() throws IOException {
        return clipboardCopy();
    }
    
    public void open() {
        FileObject fo = ejbViewController.getBeanFo();
        ElementHandle<TypeElement> beh = ejbViewController.getBeanClass();
        if (fo != null && beh != null) {
            ElementOpen.open(fo, beh);
        }
/*
        if (dataObject != null) {
            OpenCookie cookie = dataObject.getCookie(OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
 */
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    /**
     * Adds possibility to display custom delete dialog
     */
    public Object getValue(String attributeName) {
        Object retValue;
        if ("customDelete".equals(attributeName)) {
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }

}
