/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
