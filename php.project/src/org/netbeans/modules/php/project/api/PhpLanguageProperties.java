/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.PhpLanguagePropertiesAccessor;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileObject;

/**
 * Helper class to get PHP language properties like ASP tags supported etc.
 * @author Tomas Mysik
 * @since 2.44
 */
public final class PhpLanguageProperties {
    /**
     * The default value for short tags (&lt?) (<code>{@value #SHORT_TAGS_ENABLED}</code>).
     */
    public static final boolean SHORT_TAGS_ENABLED = false;
    /**
     * The default value for ASP tags (&lt% and %&gt;) (boolean value of system property <code>netbeans.php.aspTags.enabled</code>).
     */
    public static final boolean ASP_TAGS_ENABLED = Boolean.getBoolean("netbeans.php.aspTags.enabled"); // NOI18N
    /**
    * Property which is fired when {@link #areShortTagsEnabled()} changes.
    */
    public static final String PROP_SHORT_TAGS = PhpLanguageProperties.class.getName() + ".shortTags"; // NOI18N
    /**
    * Property which is fired when {@link #areAspTagsEnabled()} changes.
    */
    public static final String PROP_ASP_TAGS = PhpLanguageProperties.class.getName() + ".aspTags"; // NOI18N
    /**
    * Property which is fired when {@link #getPhpVersion()} changes.
    */
    public static final String PROP_PHP_VERSION = PhpLanguageProperties.class.getName() + ".phpVersion"; // NOI18N


    private static final PhpLanguageProperties INSTANCE = new PhpLanguageProperties();

    private final PhpLanguageOptionsImpl impl;


    static {
        PhpLanguagePropertiesAccessor.setDefault(new PhpLanguagePropertiesAccessor() {
            @Override
            public PhpLanguageProperties createForProject(PhpProject project) {
                return new PhpLanguageProperties(project);
            }
        });
    }

    PhpLanguageProperties(PhpProject project) {
        impl = ProjectOptions.forProject(project);
    }

    private PhpLanguageProperties() {
        impl = new DefaultOptions();
    }

    /**
     * Get the default instance of {@link PhpLanguageProperties} class.
     * <p>
     * This instance does not fire any property changes since there is no way to change them.
     * @return the default instance of {@link PhpLanguageProperties} class.
     */
    public static PhpLanguageProperties getDefault() {
        return INSTANCE;
    }

    /**
     * Get the instance of {@link PhpLanguageProperties} class for the given file object.
     * <p>
     * Return {@link PhpLanguageProperties} of the owning PHP project (if any) or
     * the {@link #getDefault() default} {@link PhpLanguageProperties} if the
     * file object does not belong to any project.
     * @param fileObject file object to get {@link PhpLanguageProperties} for, can be {@code null}
     * @return the instance of {@link PhpLanguageProperties} class for the given file object.
     */
    public static PhpLanguageProperties forFileObject(FileObject fileObject) {
        if (fileObject != null) {
            PhpProject phpProject = org.netbeans.modules.php.project.util.PhpProjectUtils.getPhpProject(fileObject);
            if (phpProject != null) {
                return phpProject.getLookup().lookup(PhpLanguageProperties.class);
            }
        }
        return INSTANCE;
    }

    /**
        * Find out whether short tags (&lt;?) are supported or not. This option is project specific.
        * If no project is found for the file, then {@link #SHORT_TAGS_ENABLED the default value} is returned.
        * @return <code>true</code> if short tags are supported, <code>false</code> otherwise.
        * @see #SHORT_TAGS_ENABLED
        */
    public boolean areShortTagsEnabled() {
        return impl.areShortTagsEnabled();
    }

    /**
        * Find out whether ASP tags (&lt% and %&gt;) are supported or not. This option is project specific.
        * If no project is found for the file, then {@link #ASP_TAGS_ENABLED the default value} is returned.
        * @return <code>true</code> if ASP tags are supported, <code>false</code> otherwise.
        * @see #ASP_TAGS_ENABLED
        */
    public boolean areAspTagsEnabled() {
        return impl.areAspTagsEnabled();
    }

    /**
        * Get the {@link PhpVersion PHP version} of the project.
        * If not specified, {@link PhpVersion#getDefault() default PHP version} is returned.
        * @return the {@link PhpVersion PHP version} of the project, or {@link PhpVersion#getDefault() default PHP version} if not known
        */
    public PhpVersion getPhpVersion() {
        return impl.getPhpVersion();
    }

    /**
    * Add listener that is notified when any "important" PHP language property changes.
    * @param listener a listener to add
    * @see #removeProjectPropertyChangeListener(PropertyChangeListener)
    */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }

    /**
    * Remove listener.
    * @param listener a listener to remove
    * @see #addProjectPropertyChangeListener(PropertyChangeListener)
    */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getClass().getName());
        sb.append(" [shorTagsEnabled: "); // NOI18N
        sb.append(impl.areShortTagsEnabled());
        sb.append(", aspTagsEnabled: "); // NOI18N
        sb.append(impl.areAspTagsEnabled());
        sb.append(", PHP version: "); // NOI18N
        sb.append(impl.getPhpVersion());
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    //~ Inner classes

    private interface PhpLanguageOptionsImpl {
        boolean areShortTagsEnabled();
        boolean areAspTagsEnabled();
        PhpVersion getPhpVersion();
        void addPropertyChangeListener(PropertyChangeListener listener);
        void removePropertyChangeListener(PropertyChangeListener listener);
    }

    private static final class DefaultOptions implements PhpLanguageOptionsImpl {

        @Override
        public boolean areShortTagsEnabled() {
            // #238257
            return true;
        }
        @Override
        public boolean areAspTagsEnabled() {
            // #238257
            return true;
        }

        @Override
        public PhpVersion getPhpVersion() {
            return PhpVersion.getDefault();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

    }

    private static final class ProjectOptions implements PhpLanguageOptionsImpl, PropertyChangeListener {

        private final PhpProject project;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        volatile Boolean shortTagsEnabled;
        volatile Boolean aspTagsEnabled;
        volatile PhpVersion phpVersion;


        private ProjectOptions(PhpProject project) {
            this.project = project;
        }

        public static ProjectOptions forProject(PhpProject project) {
            ProjectOptions projectOptions = new ProjectOptions(project);
            ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, projectOptions);
            return projectOptions;
        }

        @Override
        public boolean areShortTagsEnabled() {
            if (shortTagsEnabled == null) {
                // ok to read it more times
                shortTagsEnabled = ProjectPropertiesSupport.areShortTagsEnabled(project);
            }
            return shortTagsEnabled;
        }

        @Override
        public boolean areAspTagsEnabled() {
            if (aspTagsEnabled == null) {
                // ok to read it more times
                aspTagsEnabled = ProjectPropertiesSupport.areAspTagsEnabled(project);
            }
            return aspTagsEnabled;
        }

        @Override
        public PhpVersion getPhpVersion() {
            if (phpVersion == null) {
                // ok to read it more times
                phpVersion = ProjectPropertiesSupport.getPhpVersion(project);
            }
            return phpVersion;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // ensure that each listener is added just once
            propertyChangeSupport.removePropertyChangeListener(listener);
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (PhpProjectProperties.SHORT_TAGS.equals(propertyName)) {
                shortTagsEnabled = null;
                firePropertyChange(PROP_SHORT_TAGS, evt.getOldValue(), evt.getNewValue());
            } else if (PhpProjectProperties.ASP_TAGS.equals(propertyName)) {
                aspTagsEnabled = null;
                firePropertyChange(PROP_ASP_TAGS, evt.getOldValue(), evt.getNewValue());
            } else if (PhpProjectProperties.PHP_VERSION.equals(propertyName)) {
                phpVersion = null;
                firePropertyChange(PROP_PHP_VERSION, evt.getOldValue(), evt.getNewValue());
            }
        }

    }

}
