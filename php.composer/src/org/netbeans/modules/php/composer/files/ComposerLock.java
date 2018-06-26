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
package org.netbeans.modules.php.composer.files;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.json.JsonFile;
import org.openide.filesystems.FileObject;

public final class ComposerLock {

    private static final Logger LOGGER = Logger.getLogger(ComposerLock.class.getName());

    public static final String FILE_NAME = "composer.lock"; // NOI18N
    public static final String PROP_PACKAGES = "PACKAGES"; // NOI18N
    public static final String PROP_PACKAGES_DEV = "PACKAGES_DEV"; // NOI18N
    // file content
    public static final String FIELD_PACKAGES = "packages"; // NOI18N
    public static final String FIELD_PACKAGES_DEV = "packages-dev"; // NOI18N
    public static final String FIELD_NAME = "name"; // NOI18N
    public static final String FIELD_VERSION = "version"; // NOI18N

    private final JsonFile composerLock;


    public ComposerLock(FileObject directory) {
        this(directory, FILE_NAME);
    }

    // for unit tests
    ComposerLock(FileObject directory, String filename) {
        assert directory != null;
        assert filename != null;
        composerLock = new JsonFile(filename, directory, JsonFile.WatchedFields.create()
                .add(PROP_PACKAGES, FIELD_PACKAGES)
                .add(PROP_PACKAGES_DEV, FIELD_PACKAGES_DEV));
    }

    public File getFile() {
        return composerLock.getFile();
    }

    public boolean exists() {
        return composerLock.exists();
    }

    public void addPropertyChangeListener(PropertyChangeListener composerLockListener) {
        composerLock.addPropertyChangeListener(composerLockListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener composerLockListener) {
        composerLock.removePropertyChangeListener(composerLockListener);
    }

    public ComposerPackages getPackages() {
        List<Map<String, Object>> packages = composerLock.getContentValue(List.class, FIELD_PACKAGES);
        List<Map<String, Object>> packagesDev = composerLock.getContentValue(List.class, FIELD_PACKAGES_DEV);
        return new ComposerPackages(getPackages(packages), getPackages(packagesDev));
    }

    @CheckForNull
    private Map<String, String> getPackages(@NullAllowed List<Map<String, Object>> data) {
        if (data == null
                || data.isEmpty()) {
            return null;
        }
        Map<String, String> result = new HashMap<>(data.size() * 2);
        for (Map<String, Object> pckg : data) {
            // be defensive
            result.put(String.valueOf(pckg.get(FIELD_NAME)), String.valueOf(pckg.get(FIELD_VERSION)));
        }
        return result;
    }

    //~ Inner classes

    public static final class ComposerPackages {

        public final Map<String, String> packages = new ConcurrentHashMap<>();
        public final Map<String, String> packagesDev = new ConcurrentHashMap<>();


        ComposerPackages(@NullAllowed Map<String, String> packages, @NullAllowed Map<String, String> packagesDev) {
            if (packages != null) {
                this.packages.putAll(packages);
            }
            if (packagesDev != null) {
                this.packagesDev.putAll(packagesDev);
            }
        }

        public boolean isEmpty() {
            return packages.isEmpty()
                    && packagesDev.isEmpty();
        }

        public int getCount() {
            return packages.size() + packagesDev.size();
        }

        @Override
        public String toString() {
            return "ComposerPackages{" + "packages=" + packages + ", packagesDev=" + packagesDev + '}'; // NOI18N
        }

    }

}
