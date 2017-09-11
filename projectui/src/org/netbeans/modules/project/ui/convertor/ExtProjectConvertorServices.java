/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
