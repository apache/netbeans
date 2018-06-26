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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session.SessionChildren;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EntityChildren extends Children.Keys<EntityChildren.KEY> implements PropertyChangeListener {
    
    // indexes into fields with results for model query
    private final static int REMOTE = 0;
    private final static int LOCAL = 1;
    private final static int CMP = 2;
    
    protected enum KEY { REMOTE, LOCAL, CMP_FIELDS }

    private final ClasspathInfo cpInfo;
    private final String ejbClass;
    private final EjbJar ejbModule;
    private final EntityMethodController controller;
    private final Entity model; // EJB 2.1

    private final static RequestProcessor RP = new RequestProcessor(SessionChildren.class.getName(), 5);

    public EntityChildren(EjbViewController ejbViewController) throws IOException {
        this.cpInfo = ejbViewController.getClasspathInfo();
        this.ejbClass = ejbViewController.getEjbClass();
        this.ejbModule = ejbViewController.getEjbModule();
        this.controller = new EntityMethodController(ejbClass, ejbModule.getMetadataModel());
        this.model = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Entity>() {
            public Entity run(EjbJarMetadata metadata) throws Exception {
                return (Entity) metadata.findByEjbClass(ejbClass);
            }
        });
    }
    
    protected void addNotify() {
        super.addNotify();
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void updateKeys() throws IOException {
        RP.submit(new Runnable() {
            @Override
            public void run() {
                final boolean[] results = new boolean[]{false, false, true};
                try {
                    ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                        @Override
                        public Void run(EjbJarMetadata metadata) throws Exception {
                            Entity entity = (Entity) metadata.findByEjbClass(ejbClass);
                            if (entity != null) {
                                results[REMOTE] = entity.getRemote() != null;
                                results[LOCAL] = entity.getLocal() != null;
                                results[CMP] = Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType());
                            }
                            return null;
                        }
                    });
                    controller.refresh();
                } catch (MetadataModelException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        List<KEY> keys = new ArrayList<KEY>();
                        if (results[REMOTE]) { keys.add(KEY.REMOTE); }
                        if (results[LOCAL]) { keys.add(KEY.LOCAL); }
                        if (results[CMP]) { keys.add(KEY.CMP_FIELDS); }
                        setKeys(keys);
                    }
                });
            }
        });
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<KEY>emptySet());
        super.removeNotify();
    }
     
    @Override
    protected Node[] createNodes(KEY key) {
        if (key == KEY.LOCAL) {
            Children children = new MethodChildren(this, cpInfo, ejbModule, controller, model, true, ejbModule.getDeploymentDescriptor());
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.LOCAL);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (key == KEY.REMOTE) {
            Children children = new MethodChildren(this, cpInfo, ejbModule, controller, model, false, ejbModule.getDeploymentDescriptor());
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.REMOTE);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        if (key == KEY.CMP_FIELDS) {
            try {
                CMPFieldsNode n = new CMPFieldsNode(controller, model, ejbModule.getDeploymentDescriptor());
                n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/CMFieldContainerIcon.gif");
                n.setDisplayName(NbBundle.getMessage(EntityChildren.class, "LBL_CMPFields"));
                return new Node[] { n };
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return null;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
}
