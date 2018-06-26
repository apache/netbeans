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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.nodes.Node;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class EJBActionGroup extends NodeAction implements Presenter.Popup {
    
    Lookup actionContext;
    
    public String getName() {
        return NbBundle.getMessage(EJBActionGroup.class, "LBL_EJBActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    protected Action[] grouped() {
        return new Action[] {
            new AddBusinessMethodAction(),
            new AddCreateMethodAction(),
            new AddFinderMethodAction(),
            new AddHomeMethodAction(),
            new AddSelectMethodAction()
        };
    }
    
    public JMenuItem getPopupPresenter() {
        if (isEnabled() && isEjbProject(getActivatedNodes())) {
            return getMenu();
        }
        JMenuItem jMenuItem = super.getPopupPresenter();
        jMenuItem.setEnabled(false);
        return jMenuItem;
    }
    
    protected JMenu getMenu() {
        return new LazyMenu(actionContext);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
    protected boolean enable(final org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        final FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            return false;
        }
        // The following code atomically checks that the scan is not running and posts a JavaSource task 
        // which is expected to run synchronously. If the task does not run synchronously,
        // then we cancel it and return false from this method.
        final AtomicBoolean enabled = new AtomicBoolean(false);
        try {
            Future<Void> future = javaSource.runWhenScanFinished(new Task<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    String className = null;
                    ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
                    if (elementHandle != null) {
                        className = elementHandle.getQualifiedName();
                    }
                    EjbMethodController ejbMethodController = null;
                    if (className != null) {
                         ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
                    }
                    enabled.set(ejbMethodController != null);
                }
            }, true);
            // Cancel the task if it has not run yet (it will run asynchronously at a later point in time
            // which is too late for us -- we need the result now). If it has already run, the cancel() call is a no-op.
            future.cancel(true);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return enabled.get();
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        // do nothing -- should never be called
    }
    
    public boolean isEjbProject(Node[] activatedNodes) { 
        return activatedNodes.length == 1 &&
               isContainingProjectEjb(activatedNodes[0].getLookup().lookup(FileObject.class));
    }
    
    private static boolean isContainingProjectEjb(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return false;
        }
        return EjbJar.getEjbJars(project).length > 0;
    }
    
    /** Implements <code>ContextAwareAction</code> interface method. */
    public Action createContextAwareInstance(Lookup actionContext) {
        this.actionContext = actionContext;
        return super.createContextAwareInstance(actionContext);
    }

    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        private final Lookup lookup;
        
        public LazyMenu(Lookup lookup) {
            super(EJBActionGroup.this.getName());
            this.lookup = lookup;
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                Action[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    Action action = grouped[i];
                    if (action == null && getItemCount() != 0) {
                        addSeparator();
                    } else {
                        if (action instanceof ContextAwareAction) {
                            action = ((ContextAwareAction)action).createContextAwareInstance(lookup);
                        }
                        if (action instanceof Presenter.Popup) {
                            add(((Presenter.Popup)action).getPopupPresenter());
                        }
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
    
}
