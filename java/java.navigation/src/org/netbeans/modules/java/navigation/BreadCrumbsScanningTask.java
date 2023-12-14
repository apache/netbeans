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
