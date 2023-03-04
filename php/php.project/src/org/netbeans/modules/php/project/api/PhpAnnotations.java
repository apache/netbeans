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
package org.netbeans.modules.php.project.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.annotations.ProjectUserAnnotationsProvider;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Helper class to get PHP annotations.
 * @since 2.46
 */
public final class PhpAnnotations implements PropertyChangeListener {

    /**
     * @since 2.112
     */
    public static final String PROP_RESOLVE_DEPRECATED_ELEMENTS = "PROP_RESOLVE_DEPRECATED_ELEMENTS"; // NOI18N
    /**
     * @since 2.112
     */
    public static final String PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS = "PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS"; // NOI18N

    private static final PhpAnnotations INSTANCE = new PhpAnnotations();

    // @GuardedBy(this)
    private final Map<FileObject, List<AnnotationCompletionTagProvider>> cache = new WeakHashMap<>();

    private final PropertyChangeSupport propertyChangeSupport;


    private PhpAnnotations() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        getPhpOptions().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                String key = evt.getKey();
                String newValue = evt.getNewValue();
                if (org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_ANNOTATIONS_RESOLVE_DEPRECATED_ELEMENTS.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_RESOLVE_DEPRECATED_ELEMENTS, null, Boolean.parseBoolean(newValue));
                } else if (org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_ANNOTATIONS_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS, null, Boolean.parseBoolean(newValue));
                }
            }
        });
    }

    /**
     * Get PHP annotations.
     * @return {@link PhpAnnotations} instance
     */
    public static PhpAnnotations getDefault() {
        return INSTANCE;
    }

    /**
     * Add listener.
     * @param listener listener to be added, can be {@code null}
     * @since 2.112
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove listener.
     * @param listener listener to be removed, can be {@code null}
     * @since 2.112
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Get PHP annotations providers for the given file.
     * @param fileObject file to get annotations for
     * @return PHP annotations providers
     */
    public synchronized List<AnnotationCompletionTagProvider> getCompletionTagProviders(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject);

        List<AnnotationCompletionTagProvider> providers = cache.get(fileObject);
        if (providers != null) {
            return providers;
        }
        cache.clear();
        providers = computeProviders(fileObject);
        cache.put(fileObject, providers);
        return providers;
    }

    /**
     * Check whether the deprecated elements should be resolved.
     * @return {@code true} if the deprecated elements should be resolved, {@code false} otherwise
     * @since 2.93
     */
    public boolean isResolveDeprecatedElements() {
        return getPhpOptions().isAnnotationsResolveDeprecatedElements();
    }

    /**
     * Check whether the unknown annotations should be threated as type annotations.
     * @return {@code true} if the unknown annotations should be threated as type annotations, {@code false} otherwise
     * @since 2.112
     */
    public boolean isUnknownAnnotationsAsTypeAnnotations() {
        return getPhpOptions().isAnnotationsUnknownAnnotationsAsTypeAnnotations();
    }

    private List<AnnotationCompletionTagProvider> computeProviders(FileObject fileObject) {
        assert Thread.holdsLock(this);

        List<AnnotationCompletionTagProvider> result = new ArrayList<>();
        // first, add global providers
        result.addAll(org.netbeans.modules.php.api.annotation.PhpAnnotations.getCompletionTagProviders());
        // next, add providers from php frameworks and project itself
        PhpProject phpProject = org.netbeans.modules.php.project.util.PhpProjectUtils.getPhpProject(fileObject);
        if (phpProject != null) {
            ProjectPropertiesSupport.addWeakProjectPropertyChangeListener(phpProject, this);
            final PhpModule phpModule = phpProject.getPhpModule();
            // frameworks
            for (PhpFrameworkProvider provider : phpProject.getFrameworks()) {
                result.addAll(provider.getAnnotationsCompletionTagProviders(phpModule));
            }
            // project itself
            result.add(phpProject.getLookup().lookup(ProjectUserAnnotationsProvider.class));
        }
        return result;
    }

    private org.netbeans.modules.php.project.ui.options.PhpOptions getPhpOptions() {
        return org.netbeans.modules.php.project.ui.options.PhpOptions.getInstance();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PhpProject.PROP_FRAMEWORKS.equals(evt.getPropertyName())) {
            synchronized (this) {
                cache.clear();
            }
        }
    }

}
