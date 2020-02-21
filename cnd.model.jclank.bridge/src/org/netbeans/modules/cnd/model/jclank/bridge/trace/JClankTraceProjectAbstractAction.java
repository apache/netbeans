/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.model.jclank.bridge.trace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JMenuItem;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 */
public abstract class JClankTraceProjectAbstractAction extends NodeAction {

    protected final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("JClank Preprocessor", 1); // NOI18N
    private final JMenuItem presenter;

    public JClankTraceProjectAbstractAction() {
        presenter = new JMenuItem(getName());
        presenter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed();
            }
        });
    }

    @Override
    public abstract String getName();

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private JMenuItem getPresenter() {
        presenter.setEnabled(true);
        presenter.setVisible(JClankTraceProjectAbstractAction.TEST_XREF);
        return presenter;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    private void onActionPerformed() {
        performAction(getActivatedNodes());
    }

    /**
     * Actually nobody but us call this since we have a presenter.
     */
    @Override
    public final void performAction(final Node[] activatedNodes) {
        final Collection<NativeProject> projects = getNativeProjects(activatedNodes);
        if (!projects.isEmpty()) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    performActionImpl(projects);
                }

            });
        }
    }

    private void performActionImpl(Collection<NativeProject> projects) {
        String taskName = getName(); // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(taskName, false); // NOI18N
        io.select();
        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        final ProgressHandle handle = ProgressHandle.createHandle(taskName, new Cancellable() {
            @Override
            public boolean cancel() {
                cancelled.set(true);
                return true;
            }
        });

        handle.start();

        long time = System.currentTimeMillis();

        try {
            traceProjects(projects, out, err, handle, cancelled);
        } catch (Throwable e) {
            e.printStackTrace(err);
        } finally {
            handle.finish();
            if (printTiming()) {
                out.printf("%s\n", cancelled.get() ? "Cancelled" : "Done"); //NOI18N
                out.printf("%s took %d ms\n", taskName, System.currentTimeMillis() - time); // NOI18N
            }
            err.flush();
            out.flush();
            err.close();
            out.close();
        }
    }

    protected boolean printTiming() {
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Gets the collection of native projects that correspond the given nodes.
     *
     * @return in the case all nodes correspond to native projects - collection
     * of native projects; otherwise null
     */
    private Collection<NativeProject> getNativeProjects(Node[] nodes) {
        Set<NativeProject> projects = new HashSet<>();
        for (Node node : nodes) {
            NativeProject np = node.getLookup().lookup(NativeProject.class);
            if (np == null) {
                Project prj = node.getLookup().lookup(Project.class);
                if (prj == null) {
                    Object o = node.getValue("Project"); // NOI18N
                    if (o instanceof Project) {
                        prj = (Project)o;
                    }
                }
                if (prj != null) {
                    np = prj.getLookup().lookup(NativeProject.class);
                }
            }
            if (np != null) {
                projects.add(np);
            }
        }
        return projects;
    }

//    private static class OpenLink implements OutputListener {
//        private final CMSourceLocation loc;
//
//        private OpenLink(CMVisitLocation vLoc) {
//            this.loc = vLoc.getLocation();
//        }
//        
//        private OpenLink(CMSourceLocation loc) {
//            this.loc = loc;
//        }
//        
//        public static OpenLink create(CMVisitLocation vLoc) {
//            if (vLoc == null) {
//                return null;
//            }
//            return create(vLoc.getLocation());
//        }
//        
//        public static OpenLink create(CMSourceLocation loc) {
//            if (loc == null || !loc.isValid() || loc.isInSystemHeader()) {
//                return null;
//            }
//            CMFile file = loc.getFile();
//            if (file == null) {
//                return null;
//            }
////            if (!file.getFilePath().toString().contains("/home/")) {
////                return null;
////            }
//            return new OpenLink(loc);
//        }
//        
//        @Override
//        public void outputLineAction(OutputEvent ev) {
//            CMUtilities.openSource(loc);
//        }
//
//        @Override
//        public void outputLineSelected(OutputEvent ev) {
//        }
//
//        @Override
//        public void outputLineCleared(OutputEvent ev) {
//        }
//    }    
    protected abstract void traceProjects(Collection<NativeProject> projects, OutputWriter out, OutputWriter err, ProgressHandle handle, AtomicBoolean canceled);
}
