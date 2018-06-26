/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.file;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.json.JsonFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * Class representing project's <tt>package.json</tt> file.
 */
@MIMEResolver.Registration(displayName = "package.json", resource = "../resources/npm-resolver.xml", position = 127)
public final class PackageJson {

    public static final String FILE_NAME = "package.json"; // NOI18N
    public static final String PROP_NAME = "NAME"; // NOI18N
    public static final String PROP_SCRIPTS_START = "SCRIPTS_START"; // NOI18N
    public static final String PROP_DEPENDENCIES = "DEPENDENCIES"; // NOI18N
    public static final String PROP_DEV_DEPENDENCIES = "DEV_DEPENDENCIES"; // NOI18N
    public static final String PROP_PEER_DEPENDENCIES = "PEER_DEPENDENCIES"; // NOI18N
    public static final String PROP_OPTIONAL_DEPENDENCIES = "OPTIONAL_DEPENDENCIES"; // NOI18N
    // file content
    public static final String FIELD_NAME = "name"; // NOI18N
    public static final String FIELD_MAIN = "main"; // NOI18N
    public static final String FIELD_SCRIPTS = "scripts"; // NOI18N
    public static final String FIELD_START = "start"; // NOI18N
    public static final String FIELD_ENGINES = "engines"; // NOI18N
    public static final String FIELD_NODE = "node"; // NOI18N
    public static final String FIELD_DEPENDENCIES = "dependencies"; // NOI18N
    public static final String FIELD_DEV_DEPENDENCIES = "devDependencies"; // NOI18N
    public static final String FIELD_PEER_DEPENDENCIES = "peerDependencies"; // NOI18N
    public static final String FIELD_OPTIONAL_DEPENDENCIES = "optionalDependencies"; // NOI18N
    // default values
    public static final String NODE_MODULES_DIR = "node_modules"; // NOI18N

    private final JsonFile packageJson;


    public PackageJson(FileObject directory) {
        this(directory, FILE_NAME);
    }

    // for unit tests
    PackageJson(FileObject directory, String filename) {
        assert directory != null;
        packageJson = new JsonFile(filename, directory, JsonFile.WatchedFields.create()
                .add(PROP_NAME, FIELD_NAME)
                .add(PROP_SCRIPTS_START, FIELD_SCRIPTS, FIELD_START)
                .add(PROP_DEPENDENCIES, FIELD_DEPENDENCIES)
                .add(PROP_DEV_DEPENDENCIES, FIELD_DEV_DEPENDENCIES)
                .add(PROP_PEER_DEPENDENCIES, FIELD_PEER_DEPENDENCIES)
                .add(PROP_OPTIONAL_DEPENDENCIES, FIELD_OPTIONAL_DEPENDENCIES));
    }

    public boolean exists() {
        return packageJson.exists();
    }

    public String getPath() {
        return packageJson.getPath();
    }

    public File getFile() {
        return packageJson.getFile();
    }

    public File getNodeModulesDir() {
        return new File(packageJson.getFile().getParentFile(), NODE_MODULES_DIR);
    }

    @CheckForNull
    public Map<String, Object> getContent() {
        return packageJson.getContent();
    }

    @CheckForNull
    public <T> T getContentValue(Class<T> valueType, String... fieldHierarchy) {
        return packageJson.getContentValue(valueType, fieldHierarchy);
    }

    public void setContent(List<String> fieldHierarchy, Object value) throws IOException {
        packageJson.setContent(fieldHierarchy, value);
    }

    public void addPropertyChangeListener(PropertyChangeListener packageJsonListener) {
        packageJson.addPropertyChangeListener(packageJsonListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener packageJsonListener) {
        packageJson.removePropertyChangeListener(packageJsonListener);
    }

    public void cleanup() {
        packageJson.cleanup();
    }

    public void refresh() {
        packageJson.refresh();
    }

    public PackageJson.NpmDependencies getDependencies() {
        @SuppressWarnings("unchecked")
        Map<Object, Object> dependencies = getContentValue(Map.class, FIELD_DEPENDENCIES);
        @SuppressWarnings("unchecked")
        Map<Object, Object> devDependencies = getContentValue(Map.class, FIELD_DEV_DEPENDENCIES);
        @SuppressWarnings("unchecked")
        Map<Object, Object> peerDependencies = getContentValue(Map.class, FIELD_PEER_DEPENDENCIES);
        @SuppressWarnings("unchecked")
        Map<Object, Object> optionalDependencies = getContentValue(Map.class, FIELD_OPTIONAL_DEPENDENCIES);
        return new NpmDependencies(sanitizeDependencies(dependencies), sanitizeDependencies(devDependencies),
                sanitizeDependencies(peerDependencies), sanitizeDependencies(optionalDependencies));
    }

    @CheckForNull
    private Map<String, String> sanitizeDependencies(@NullAllowed Map<Object, Object> data) {
        if (data == null
                || data.isEmpty()) {
            return null;
        }
        Map<String, String> sanitized = new HashMap<>();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            sanitized.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return sanitized;
    }

    //~ Inner classes

    public static final class NpmDependencies {

        public final Map<String, String> dependencies = new ConcurrentHashMap<>();
        public final Map<String, String> devDependencies = new ConcurrentHashMap<>();
        public final Map<String, String> peerDependencies = new ConcurrentHashMap<>();
        public final Map<String, String> optionalDependencies = new ConcurrentHashMap<>();


        NpmDependencies(@NullAllowed Map<String, String> dependencies, @NullAllowed Map<String, String> devDependencies,
                @NullAllowed Map<String, String> peerDependencies, @NullAllowed Map<String, String> optionalDependencies) {
            if (dependencies != null) {
                this.dependencies.putAll(dependencies);
            }
            if (devDependencies != null) {
                this.devDependencies.putAll(devDependencies);
            }
            if (peerDependencies != null) {
                this.peerDependencies.putAll(peerDependencies);
            }
            if (optionalDependencies != null) {
                this.optionalDependencies.putAll(optionalDependencies);
            }
        }

        public boolean isEmpty() {
            return dependencies.isEmpty()
                    && devDependencies.isEmpty()
                    && peerDependencies.isEmpty()
                    && optionalDependencies.isEmpty();
        }

        public int getCount() {
            return dependencies.size() + devDependencies.size()
                    + peerDependencies.size() + optionalDependencies.size();
        }

    }

}
