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
package org.netbeans.modules.web.clientproject.api.platform;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.platform.PlatformProviderAccessor;
import org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered platform providers. The path
 * for registration is "{@value #PLATFORM_PATH}" on SFS.
 * <p>
 * This class is thread safe.
 * @since 1.68
 */
public final class PlatformProviders {

    /**
     * Path on SFS for platform providers registrations.
     */
    public static final String PLATFORM_PATH = "HTML5/Platform"; // NOI18N

    private static final Lookup.Result<PlatformProviderImplementation> PLATFORM_PROVIDERS = Lookups
            .forPath(PLATFORM_PATH)
            .lookupResult(PlatformProviderImplementation.class);
    private static final PlatformProviders INSTANCE = new PlatformProviders();

    private final List<PlatformProvider> platformProviders = new CopyOnWriteArrayList<>();
    private final PlatformProvidersListener.Support listenersSupport = new PlatformProvidersListener.Support();
    private final DelegatingPlatformProviderListener delegatingPlatformProvidersListener = new DelegatingPlatformProviderListener();

    static {
        PLATFORM_PROVIDERS.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                INSTANCE.reinitProviders();
            }
        });
    }


    private PlatformProviders() {
        initProviders();
    }

    /**
     * Get PlatformProviders instance.
     * @return PlatformProviders instance
     */
    public static PlatformProviders getDefault() {
        return INSTANCE;
    }

    /**
     * Get list of all registered platform providers.
     * @return list of all registered platform providers, can be empty but never {@code null}
     */
    public List<PlatformProvider> getPlatformProviders() {
        return new ArrayList<>(platformProviders);
    }

    /**
     * Find platform provider for the given {@link PlatformProvider#getIdentifier() identifier}.
     * @param identifier identifier of platform provider
     * @return platform provider or {@code null} if not found
     */
    @CheckForNull
    public PlatformProvider findPlatformProvider(@NonNull String identifier) {
        Parameters.notNull("identifier", identifier); // NOI18N
        for (PlatformProvider platformProvider : platformProviders) {
            if (platformProvider.getIdentifier().equals(identifier)) {
                return platformProvider;
            }
        }
        return null;
    }

    /**
     * Set given platform provider for the given project.
     * @param project project to be configured
     * @param platformProvider platform provider to be set
     * @see #findPlatformProvider(String)
     */
    public void setPlatformProvider(@NonNull Project project, @NonNull PlatformProvider platformProvider) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("platformProvider", platformProvider); // NOI18N
        platformProvider.notifyPropertyChanged(project, new PropertyChangeEvent(project, PlatformProvider.PROP_ENABLED, null, true));
    }

    /**
     * Notifies providers that the given project is being opened.
     * <p>
     * Provider is notified even if it is not {@link PlatformProvider#isEnabled(Project) enabled} in the given project.
     * @param project project being opened
     */
    public void projectOpened(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        for (PlatformProvider platformProvider : platformProviders) {
            platformProvider.projectOpened(project);
        }
    }

    /**
     * Notifies providers that the given project is being closed.
     * <p>
     * Provider is notified even if it is not {@link PlatformProvider#isEnabled(Project) enabled} in the given project.
     * @param project project being closed
     */
    public void projectClosed(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        for (PlatformProvider platformProvider : platformProviders) {
            platformProvider.projectClosed(project);
        }
    }

    /**
     * Notifies provider that some property has been changed in the given project (so
     * the provider can, if necessary, adjust UI etc.).
     * <p>
     * Provider is notified even if it is not {@link PlatformProvider#isEnabled(Project) enabled} in the given project.
     * @param project the project, never {@code null}
     * @param platformProvider platform provider to be notified
     * @param event information about property change
     * @since 1.71
     */
    public void notifyPropertyChanged(@NonNull Project project, @NonNull PlatformProvider platformProvider, @NonNull PropertyChangeEvent event) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("platformProvider", platformProvider); // NOI18N
        Parameters.notNull("event", event); // NOI18N
        platformProvider.notifyPropertyChanged(project, event);
    }

    /**
     * Notifies all providers that some property has been changed in the given project (so
     * the provider can, if necessary, adjust UI etc.).
     * <p>
     * Provider is notified even if it is not {@link PlatformProvider#isEnabled(Project) enabled} in the given project.
     * @param project the project, never {@code null}
     * @param event information about property change
     * @since 1.71
     */
    public void notifyPropertyChanged(@NonNull Project project, @NonNull PropertyChangeEvent event) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("event", event); // NOI18N
        for (PlatformProvider platformProvider : platformProviders) {
            platformProvider.notifyPropertyChanged(project, event);
        }
    }

    /**
     * Attach a listener that is to be notified of changes
     * in platform providers.
     * @param listener a listener, can be {@code null}
     */
    public void addPlatformProvidersListener(@NullAllowed PlatformProvidersListener listener) {
        listenersSupport.addPlatformProvidersListener(listener);
    }

    /**
     * Removes a change listener.
     * @param listener a listener, can be {@code null}
     */
    public void removePlatformProvidersListener(@NullAllowed PlatformProvidersListener listener) {
        listenersSupport.removePlatformProvidersListener(listener);
    }

    private void initProviders() {
        assert platformProviders.isEmpty() : "Empty providers expected but: " + platformProviders;
        platformProviders.addAll(map(PLATFORM_PROVIDERS.allInstances()));
        for (PlatformProvider provider : platformProviders) {
            provider.getDelegate().addPlatformProviderImplementationListener(delegatingPlatformProvidersListener);
        }
    }

    void reinitProviders() {
        synchronized (platformProviders) {
            clearProviders();
            initProviders();
        }
        listenersSupport.firePlatformProvidersChanged();
    }

    private void clearProviders() {
        platformProviders.clear();
    }

    @CheckForNull
    PlatformProvider findPlatformProvider(PlatformProviderImplementation platformProviderImplementation) {
        assert platformProviderImplementation != null;
        for (PlatformProvider provider : platformProviders) {
            if (provider.getDelegate() == platformProviderImplementation) {
                return provider;
            }
        }
        assert false : "Cannot find platform provider for implementation: " + platformProviderImplementation.getIdentifier();
        return null;
    }

    //~ Mappers

    private Collection<PlatformProvider> map(Collection<? extends PlatformProviderImplementation> providers) {
        List<PlatformProvider> result = new ArrayList<>();
        for (PlatformProviderImplementation provider : providers) {
            result.add(PlatformProviderAccessor.getDefault().create(provider));
        }
        return result;
    }

    //~ Inner classes

    private final class DelegatingPlatformProviderListener implements PlatformProviderImplementationListener {

        @Override
        public void propertyChanged(Project project, PlatformProviderImplementation platformProvider, PropertyChangeEvent event) {
            Parameters.notNull("platformProvider", platformProvider); // NOI18N
            Parameters.notNull("event", event); // NOI18N
            PlatformProvider provider = findPlatformProvider(platformProvider);
            if (provider != null) {
                listenersSupport.firePropertyChanged(project, provider, event);
            }
        }

    }

}
