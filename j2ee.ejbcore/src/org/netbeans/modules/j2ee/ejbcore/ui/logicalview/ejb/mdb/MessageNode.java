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


package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.mdb;

import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Chris Webster
 * @author Ludovic Champenois
 * @author Martin Adamek
 */
public class MessageNode extends AbstractNode implements OpenCookie {

    private final PropertyChangeListener nameChangeListener;
    private final EjbViewController controller;

    public static MessageNode create(final String ejbClass, EjbJar ejbModule, Project project) {
        String ejbName = null;
        try {
            ejbName = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws Exception {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    return ejb == null ? null : ejb.getEjbName();
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        if (ejbName == null) {
            return null;
        } else {
            return new MessageNode(new InstanceContent(), new EjbViewController(ejbClass, ejbModule), ejbName, project);
        }
    }

    private MessageNode(InstanceContent content, EjbViewController controller, String ejbName, Project project) {
        super(Children.LEAF, new AbstractLookup(content));
        this.controller = controller;
        setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/mdb/MessageNodeIcon.gif");
        setName(ejbName + "");
        setDisplayName();
        nameChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setDisplayName();
            }
        };
        //TODO: RETOUCHE listening on model for logical view
//        model.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeListener,model));
        content.add(this);
        ElementHandle<TypeElement> beanClassHandle = controller.getBeanClass();
        if (beanClassHandle != null) {
            content.add(beanClassHandle);
        }
        if (controller.getBeanDo() != null) {
            content.add(controller.getBeanDo().getPrimaryFile());
        }
    }

    private void setDisplayName() {
        setDisplayName(controller.getDisplayName());
    }

    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
        };
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // TODO
        // return new HelpCtx(SessionNode.class);
    }

    public boolean canDestroy() {
        return false;
    }

    public void destroy() throws java.io.IOException {
        String deleteOptions = DeleteEJBDialog.open(controller.getDisplayName());
        if (!deleteOptions.equals(DeleteEJBDialog.DELETE_NOTHING)) {
            if (deleteOptions.equals(DeleteEJBDialog.DELETE_ONLY_DD)) {
                controller.delete(false);
            } else {
                controller.delete(true);
            }
        }
    }

    public boolean canCopy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }

    public void open() {
        FileObject fo = controller.getBeanFo();
        ElementHandle<TypeElement> beh = controller.getBeanClass();
        if (fo != null && beh != null) {
            ElementOpen.open(fo, beh);
        }

/*
        DataObject dataObject = controller.getBeanDo();
        if (dataObject != null) {
            OpenCookie cookie = (OpenCookie) dataObject.getCookie(OpenCookie.class);
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
        if (attributeName.equals("customDelete")) {
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }

}
