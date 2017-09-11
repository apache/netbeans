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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import static org.netbeans.api.project.ProjectInformation.PROP_DISPLAY_NAME;
import static org.netbeans.api.project.ProjectInformation.PROP_ICON;
import static org.netbeans.api.project.ProjectInformation.PROP_NAME;
import org.netbeans.modules.project.uiapi.ProjectConvertorServiceFactory;
import org.netbeans.spi.project.ui.ProjectConvertor;
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
public class DefaultProjectConvertorServices implements ProjectConvertorServiceFactory {

    @Override
    public Collection<?> createServices(
            @NonNull final Project project,
            @NonNull final ProjectConvertor.Result result) {
        return Collections.singleton(new ProjectInfo(project, result));
    }

    //<editor-fold defaultstate="collapsed" desc="ProjectInformation implementation">
    private static final class ProjectInfo implements ProjectInformation, LookupListener {

        private final Project project;
        private final ProjectConvertor.Result result;
        private final PropertyChangeSupport pcs;
        private final Lookup.Result<ProjectInformation> eventSource;
        private volatile ProjectInformation delegate;

        ProjectInfo(
                @NonNull final Project project,
                @NonNull final ProjectConvertor.Result result) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("result", result);   //NOI18N
            this.project = project;
            this.result = result;
            this.pcs = new PropertyChangeSupport(this);
            this.eventSource = project.getLookup().lookupResult(ProjectInformation.class);
            this.eventSource.addLookupListener(WeakListeners.create(LookupListener.class, this, eventSource));
        }

        @Override
        @NonNull
        public String getName() {
            final ProjectInformation d = delegate;
            return d != null ?
                d.getName() :
                project.getProjectDirectory().getName();
        }

        @Override
        @NonNull
        public String getDisplayName() {
            final ProjectInformation d = delegate;
            if (d != null) {
                return d.getDisplayName();
            } else {
                String res = result.getDisplayName();
                if (res == null) {
                    res = getName();
                }
                return res;
            }
        }

        @Override
        @NonNull
        public Icon getIcon() {
            final ProjectInformation d = delegate;
            if (d != null) {
                return d.getIcon();
            } else {
                Icon res = result.getIcon();
                //Todo: Handle null res
                return res;
            }
        }

        @Override
        @NonNull
        public Project getProject() {
            final ProjectInformation d = delegate;
            if (d != null) {
                return d.getProject();
            } else {
                return project;
            }
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            //In case someone holds this transient ProjectInfo
            //keep it alive and delegate to real one.
            final Collection<? extends ProjectInformation> instances = eventSource.allInstances();
            if (!instances.isEmpty()) {
                final ProjectInformation instance = instances.iterator().next();
                if (instance != this) {
                    delegate = instance;
                    pcs.firePropertyChange(PROP_NAME, null, null);
                    pcs.firePropertyChange(PROP_DISPLAY_NAME, null, null);
                    pcs.firePropertyChange(PROP_ICON, null, null);
                }
            }
        }
    }
    //</editor-fold>
}
