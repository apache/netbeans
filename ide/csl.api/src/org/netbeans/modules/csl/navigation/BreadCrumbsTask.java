/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.csl.navigation;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.Icon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.core.AbstractTaskFactory;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class BreadCrumbsTask extends ElementScanningTask {

    public BreadCrumbsTask() {
    }

    private static final RequestProcessor WORKER = new RequestProcessor(BreadCrumbsTask.class.getName(), 1, false, false);
    
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return BreadcrumbsController.BREADCRUMBS_SCHEDULER;
    }

    private final AtomicLong requestId = new AtomicLong();
    
    @Override
    public void run(final ParserResult result, final SchedulerEvent event) {
        runWithCancelService(new Runnable() {
            @Override
            public void run() {
                resume();
                final long id = requestId.incrementAndGet();

                final Document doc = result.getSnapshot().getSource().getDocument(false);

                if (doc == null || !BreadcrumbsController.areBreadCrumsEnabled(doc)) return ;

                final int caret;

                if (event instanceof CursorMovedSchedulerEvent) {
                    caret = ((CursorMovedSchedulerEvent) event).getCaretOffset();
                } else {
                    //XXX: outside AWT!
                    JTextComponent c = EditorRegistry.focusedComponent();

                    if (c != null && c.getDocument() == doc)
                        caret = c.getCaretPosition();
                    else
                        caret = (-1);
                }

                if (caret == (-1)) return ;

                final StructureItem structureRoot = computeStructureRoot(result.getSnapshot().getSource());

                if (structureRoot == null) return ;

                WORKER.post(new Runnable() {
                    @Override public void run() {
                        selectNode(doc, structureRoot, id, caret);
                    }
                });
            }
        });
    }
    
    private void selectNode(Document doc, StructureItem structureRoot, long id, int caret) {
        StructureItemNode root = new StructureItemNode(structureRoot);
        StructureItemNode toSelect = root;
        
        OUTER: while (requestId.get() == id) {
            for (Node n : toSelect.getChildren().getNodes(true)) {
                StructureItemNode sin = (StructureItemNode) n;
                if (StructureItem.isInherited(sin.item)) {
                    continue;
                }
                if (sin.item.getPosition() <= caret && caret <= sin.item.getEndPosition()) {
                    toSelect = sin;
                    // see #223480, mimetype nodes look ugly in the breadcrumb bar
                    if (toSelect.item instanceof ElementScanningTask.MimetypeRootNode) {
                        root = sin;
                    }
                    continue OUTER;
                }
            }
            
            break;
        }
        
        if (requestId.get() == id) {
            BreadcrumbsController.setBreadcrumbs(doc, root, toSelect);
        }
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        requestId.incrementAndGet();
    }
    
    private static final class StructureItemNode extends AbstractNode {
        private final StructureItem item;

        public StructureItemNode(final StructureItem item) {
            super(Children.create(new ChildFactory<StructureItem>() {
                @Override protected boolean createKeys(List<StructureItem> toPopulate) {
                    toPopulate.addAll(item.getNestedItems());
                    return true;
                }

                @Override
                protected Node createNodeForKey(StructureItem key) {
                    return new StructureItemNode(key);
                }
                
            }, false), Lookups.fixed(new OpenCookie() {
                @Override public void open() {
                    ElementHandle elementHandle = item.getElementHandle();
                    FileObject file = elementHandle != null ? elementHandle.getFileObject() : null;
                    if (file != null) {
                        UiUtils.open(file, (int) item.getPosition());
                    }
                }
            }));
            this.item = item;
            setDisplayName(item.getName());
        }

        @Override
        public Image getIcon(int type) {
            if (item.getCustomIcon() != null) {
                return ImageUtilities.icon2Image(item.getCustomIcon());
            }
            Icon icon = Icons.getElementIcon(item.getKind(), item.getModifiers());
            if (icon != null) {
                return ImageUtilities.icon2Image(icon);
            } else {
                return super.getIcon(type);
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }
    
    @MimeRegistration(service=TaskFactory.class, mimeType="")
    public static final class TaskFactoryImpl extends AbstractTaskFactory {

        public TaskFactoryImpl() {
            super(true);
        }
        
        @Override
        protected Collection<? extends SchedulerTask> createTasks(Language language, Snapshot snapshot) {
            return Collections.singletonList(new BreadCrumbsTask());
        }
        
    }
}
