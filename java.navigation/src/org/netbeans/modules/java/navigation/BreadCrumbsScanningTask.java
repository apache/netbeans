/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011-2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation;

import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.nodes.Node;

/**
 *
 * @author lahvac
 */
public final class BreadCrumbsScanningTask extends JavaParserResultTask {

    private static final String COLOR = "#707070";
    private final AtomicBoolean cancel = new AtomicBoolean();

    private BreadCrumbsScanningTask() {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        cancel.set(false);

        CompilationInfo info = CompilationInfo.get(result);
        if (info == null) {
            return;
        }

        Document doc = info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return;
        }

        if (!BreadcrumbsController.areBreadCrumsEnabled(doc)) return ;
        
        int caretPosition = event instanceof CursorMovedSchedulerEvent
                ? ((CursorMovedSchedulerEvent) event).getCaretOffset()
                : CaretAwareJavaSourceTaskFactory.getLastPosition(result.getSnapshot().getSource().getFileObject()); //XXX

        if (cancel.get()) {
            return;
        }

        BreadcrumbsElement[] rootAndSelection = rootAndSelection(info, caretPosition, cancel);
        
        if (cancel.get() || rootAndSelection == null) {
            return ;
        }

        BreadcrumbsController.setBreadcrumbs(doc, rootAndSelection[1]);
        
        info.putCachedValue(BreadCrumbsScanningTask.class, rootAndSelection[0], CacheClearPolicy.ON_CHANGE);
    }

    static BreadcrumbsElement[] rootAndSelection(CompilationInfo info, int caretPosition, AtomicBoolean cancel) {
        BreadcrumbsElement root;
        BreadcrumbsElement lastNode;

        root = (BreadCrumbsNodeImpl) info.getCachedValue(BreadCrumbsScanningTask.class);
        
        if (root == null) {
            root = BreadCrumbsNodeImpl.createBreadcrumbs(null, info, new TreePath(info.getCompilationUnit()), false);
        }
        
        lastNode = root;
            
        boolean cont = true;

        while (cont) {
            if (cancel.get()) {
                return null;
            }

            cont = false;

            List<BreadcrumbsElement> children = lastNode.getChildren();

            for (BreadcrumbsElement child : children) {
                if (cancel.get()) {
                    return null;
                }

                int[] pos = child.getLookup().lookup(int[].class);

                if (pos[0] <= caretPosition && caretPosition <= pos[1]) {
                    lastNode = child;
                    cont = true;
                    break;
                }
            }
        }
        
        return new BreadcrumbsElement[] {root, lastNode};
    }
    
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return BreadcrumbsController.BREADCRUMBS_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancel.set(true);
    }

    @MimeRegistration(mimeType="text/x-java", service=TaskFactory.class)
    public static final class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new BreadCrumbsScanningTask());
        }
    }
}
