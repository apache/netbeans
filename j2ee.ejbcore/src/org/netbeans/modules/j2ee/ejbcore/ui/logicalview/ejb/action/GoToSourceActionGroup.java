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

    private final static int EJB_CLASS = 0;
    private final static int REMOTE = 1;
    private final static int LOCAL = 2;
    private final static int HOME = 3;
    private final static int LOCAL_HOME = 4;
    
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
        
        return actions.toArray(new Action[actions.size()]);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
