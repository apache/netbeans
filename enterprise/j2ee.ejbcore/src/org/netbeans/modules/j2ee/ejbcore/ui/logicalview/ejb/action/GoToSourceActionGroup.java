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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Group holding source navigation actions for Session and Entity EJBs
 *
 * @author Martin Adamek
 */
public class GoToSourceActionGroup extends EJBActionGroup {

    private static final int EJB_CLASS = 0;
    private static final int REMOTE = 1;
    private static final int LOCAL = 2;
    private static final int HOME = 3;
    private static final int LOCAL_HOME = 4;
    
    public String getName() {
        return NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoToSourceGroup");
    }

    protected Action[] grouped() {

        final DataObject[] results = new DataObject[5];
        
        Node node = getActivatedNodes()[0];
        if (node == null) {
            return new Action[0];
        }
        
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        final String[] ejbClass = new String[1];
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar model = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        MetadataModel<EjbJarMetadata> metadata = model.getMetadataModel();
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement publicTopLevelElement = SourceUtils.getPublicTopLevelElement(controller);
                    if (publicTopLevelElement != null) {
                        ejbClass[0] = publicTopLevelElement.getQualifiedName().toString();
                    }
                }
            }, true);

            if (ejbClass[0] == null) {
                return new Action[0];
            }

            final EjbViewController controller = new EjbViewController(ejbClass[0], model);
            metadata.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) {
                    EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass[0]);
                    if (ejb == null){
                        return null;
                    }
                    results[EJB_CLASS] = ejb.getEjbClass() == null ? null : controller.getDataObject(ejb.getEjbClass());
                    results[REMOTE] = ejb.getRemote() ==null ? null : controller.getDataObject(ejb.getRemote());
                    results[LOCAL] = ejb.getLocal() ==null ? null : controller.getDataObject(ejb.getLocal());
                    results[HOME] = ejb.getHome() ==null ? null : controller.getDataObject(ejb.getHome());
                    results[LOCAL_HOME] = ejb.getLocalHome() ==null ? null : controller.getDataObject(ejb.getLocalHome());
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        List<Action> actions = new ArrayList<Action>();
        actions.add(new GoToSourceAction(results[EJB_CLASS], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_BeanImplementation")));
        if (results[REMOTE] != null) {
            actions.add(new GoToSourceAction(results[REMOTE], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_RemoteInterface")));
        }
        if (results[LOCAL] != null) {
            actions.add(new GoToSourceAction(results[LOCAL], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_LocalInterface")));
        }
        if (results[HOME] != null) {
            actions.add(new GoToSourceAction(results[HOME], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_RemoteHomeInterface")));
        }
        if (results[LOCAL_HOME] != null) {
            actions.add(new GoToSourceAction(results[LOCAL_HOME], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_LocalHomeInterface")));
        }
        
        return actions.toArray(new Action[0]);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
