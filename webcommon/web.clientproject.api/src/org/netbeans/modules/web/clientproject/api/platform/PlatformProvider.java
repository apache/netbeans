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
package org.netbeans.modules.web.clientproject.api.platform;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.CustomizerPanelAccessor;
import org.netbeans.modules.web.clientproject.api.BadgeIcon;
import org.netbeans.modules.web.clientproject.api.CustomizerPanel;
import org.netbeans.modules.web.clientproject.platform.PlatformProviderAccessor;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Parameters;

/**
 * The API representation of a single provider for platform.
 * @since 1.68
 */
public final class PlatformProvider {

    /**
     * Property name for changes in enabled state.
     */
    public static final String PROP_ENABLED = PlatformProviderImplementation.PROP_ENABLED;
    /**
     * Property name for changes in source roots.
     */
    public static final String PROP_SOURCE_ROOTS = PlatformProviderImplementation.PROP_SOURCE_ROOTS;
    /**
     * Property name for changes in project name.
     * @since 1.70
     */
    public static final String PROP_PROJECT_NAME = PlatformProviderImplementation.PROP_PROJECT_NAME;
    /**
     * Property name for changes in run configuration.
     * @since 1.72
     */
    public static final String PROP_RUN_CONFIGURATION = PlatformProviderImplementation.PROP_RUN_CONFIGURATION;


    private final PlatformProviderImplementation delegate;

    static {
        PlatformProviderAccessor.setDefault(new PlatformProviderAccessor() {

            @Override
            public PlatformProvider create(PlatformProviderImplementation platformProviderImplementation) {
                return new PlatformProvider(platformProviderImplementation);
            }
        });
    }

    private PlatformProvider(PlatformProviderImplementation delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    /**
     * Returns the display name of this provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Returns badge icon of this provider.
     * @return badge icon of this provider, can be {@code null}
     */
    @CheckForNull
    public BadgeIcon getBadgeIcon() {
        return delegate.getBadgeIcon();
    }

    /**
     * Checks whether this provider is enabled in the given project.
     * @param project project to be checked
     * @return {@code true} if this provider is enabled in the given project, {@code false} otherwise
     */
    public boolean isEnabled(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.isEnabled(project);
    }

    /**
     * Gets list of source roots.
     * @param project project to be used
     * @return list of source roots, can be empty but never {@code null}
     */
    public List<URL> getSourceRoots(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.getSourceRoots(project);
    }

    /**
     * Get action provider of this provider.
     * @param project project to be source of the action
     * @return action provider of this provider, can be {@code null} if not supported
     */
    @CheckForNull
    public ActionProvider getActionProvider(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.getActionProvider(project);
    }

    /**
     * Get list of panels for run customization.
     * <p>
     * These panels can be used to configure properties needed for running this platform provider,
     * like e.g. debugger port, default/index file etc.
     * @param project project to be source of the customization
     * @return list of panels for run customization, can be empty but never {@code null}
     * @since 1.71
     */
    public List<CustomizerPanel> getRunCustomizerPanels(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        List<CustomizerPanelImplementation> delegatePanels = delegate.getRunCustomizerPanels(project);
        if (delegatePanels.isEmpty()) {
            return Collections.emptyList();
        }
        List<CustomizerPanel> panels = new ArrayList<>(delegatePanels.size());
        for (CustomizerPanelImplementation delegatePanel : delegatePanels) {
            if (delegatePanel == null) {
                throw new IllegalStateException("Run customizer panel cannot be null for " + delegate.getClass().getName());
            }
            panels.add(CustomizerPanelAccessor.getDefault().create(delegatePanel));
        }
        return panels;
    }

    void projectOpened(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        delegate.projectOpened(project);
    }

    void projectClosed(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        delegate.projectClosed(project);
    }

    PlatformProviderImplementation getDelegate() {
        return delegate;
    }

    void notifyPropertyChanged(@NonNull Project project, @NonNull PropertyChangeEvent event) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("event", event); // NOI18N
        delegate.notifyPropertyChanged(project, event);
    }

}
