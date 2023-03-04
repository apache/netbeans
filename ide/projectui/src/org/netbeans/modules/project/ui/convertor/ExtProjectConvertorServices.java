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
package org.netbeans.modules.project.ui.convertor;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.ProjectConvertorServiceFactory;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = ProjectConvertorServiceFactory.class)
public class ExtProjectConvertorServices implements ProjectConvertorServiceFactory {
    @Override
    public Collection<?> createServices(@NonNull final Project project, @NonNull final ProjectConvertor.Result result) {
        return Collections.singleton(new LogicalView(project, result));
    }

    //<editor-fold defaultstate="collapsed" desc="LogicalViewProvider implementation">
    private static final class LogicalView implements LogicalViewProvider, LookupListener {

        private final Project project;
        private final ProjectConvertor.Result result;
        private final Lookup.Result<LogicalViewProvider> eventSource;
        private final ChangeSupport support;
        private volatile LogicalViewProvider delegate;

        @SuppressWarnings("LeakingThisInConstructor")
        LogicalView(@NonNull final Project project, @NonNull final ProjectConvertor.Result result) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("result", result);   //NOI18N
            this.project = project;
            this.result = result;
            this.support = new ChangeSupport(this);
            this.eventSource = project.getLookup().lookupResult(LogicalViewProvider.class);
            this.eventSource.addLookupListener(WeakListeners.create(LookupListener.class, this, this.eventSource));
        }

        @Override
        @NonNull
        public Node createLogicalView() {
            final LogicalViewProvider lvp = delegate;
            if (lvp != null) {
                return lvp.createLogicalView();
            } else {
                Node original;
                try {
                    final FileObject fo = project.getProjectDirectory();
                    final DataObject dobj = DataObject.find(fo);
                    original = dobj.getNodeDelegate();
                } catch (DataObjectNotFoundException dnfe) {
                    original =  new AbstractNode(Children.LEAF);
                }
                return new Root(this, original);
            }
        }

        @Override
        public Node findPath(Node root, Object target) {
            final LogicalViewProvider lvp = delegate;
            if (lvp != null) {
                return lvp.findPath(root, target);
            } else {
                //Todo: Probably not needed
                return null;
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            for (LogicalViewProvider lvp : eventSource.allInstances()) {
                if (lvp != this) {
                    delegate = lvp;
                    support.fireChange();
                    break;
                }
            }
        }

        public void addChangeListener(@NonNull final ChangeListener listener) {
            support.addChangeListener(listener);
        }

        public void removeChangeListener(@NonNull final ChangeListener listener) {
            support.removeChangeListener(listener);
        }

        private static final class Root extends FilterNode implements ChangeListener {

            private final ProjectConvertor.Result result;
            private volatile Action[] actions;
            private volatile boolean hasDelegate;

            @SuppressWarnings("LeakingThisInConstructor")
            Root(
                @NonNull final LogicalView view,
                @NonNull final Node delegate) {
                super(delegate);
                this.result = view.result;
                view.addChangeListener(WeakListeners.change(this, view));
            }

            @Override
            public String getDisplayName() {
                return hasDelegate ?
                    super.getDisplayName() :
                    result.getDisplayName();
            }

            @Override
            public Image getIcon(int type) {
                return hasDelegate ?
                    super.getIcon(type):
                    ImageUtilities.icon2Image(result.getIcon());
            }

            @Override
            public Action[] getActions(boolean context) {
                return hasDelegate ?
                    super.getActions(context) :
                    transientActions();
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                final Object source = e.getSource();
                if (source instanceof LogicalView) {
                    hasDelegate = true;
                    changeOriginal(((LogicalView)source).createLogicalView(), true);
                }
            }

            @NonNull
            private Action[] transientActions() {
                Action[] res = actions;
                if (res == null) {
                    res = actions = new Action[] {
                        CommonProjectActions.closeProjectAction()
                    };
                }
                return res;
            }
        }
    }
    //</editor-fold>
}
