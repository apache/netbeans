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
