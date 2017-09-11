/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.platform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.spi.J2SEPlatformDefaultJavadoc;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default Javadoc for JavaFX.
 * @author Tomas Zezula
 */
@ServiceProvider(service = J2SEPlatformDefaultJavadoc.class, position = 200, path="org-netbeans-api-java/platform/j2seplatform/defaultJavadocProviders")
public final class JavaFxDefaultJavadocImpl implements J2SEPlatformDefaultJavadoc {

    private static final Map<SpecificationVersion,String> OFFICIAL_JAVADOC;
    static {
        final Map<SpecificationVersion,String> jdocs = new HashMap<>();
        jdocs.put(new SpecificationVersion("1.7"), "https://docs.oracle.com/javafx/2/api/"); //NOI18N
        jdocs.put(new SpecificationVersion("1.8"), "https://docs.oracle.com/javase/8/javafx/api/"); //NOI18N
        OFFICIAL_JAVADOC = Collections.unmodifiableMap(jdocs);
    }

    @Override
    @NonNull
    public Collection<URI> getDefaultJavadoc(@NonNull final JavaPlatform platform) {
        final List<URI> result = new ArrayList<>();
        final JavadocFilter filter = new JavadocFilter();
        for (FileObject installFolder : platform.getInstallFolders()) {
            for (FileObject file : installFolder.getChildren()) {
                final Collection<? extends URI> roots = filter.accept(file);
                result.addAll(roots);
            }
        }
        if (!result.isEmpty()) {
            return Collections.unmodifiableCollection(result);
        }
        final SpecificationVersion spec = platform.getSpecification().getVersion();
        final String uri = OFFICIAL_JAVADOC.get(spec);
        if (uri != null) {
            try {
                return Collections.singletonList(new URI(uri));
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return Collections.<URI>emptyList();
    }

    private static final class JavadocFilter {
        private static final Pattern DOCS_FILE_PATTERN = Pattern.compile(".*docs.*\\.(zip|jar)",Pattern.CASE_INSENSITIVE);   //NOI18N
        private static final Pattern JAVAFX_FILE_PATTERN = Pattern.compile(".*j(ava)?fx.*", Pattern.CASE_INSENSITIVE);  //NOI18N
        private static final Collection<String> DOCS_PATHS;
        static {
            final List<String> paths = new ArrayList<>(3);
            paths.add("api/");     //NOI18N
            DOCS_PATHS = Collections.unmodifiableList(paths);
        }

        @NonNull
        Collection<? extends URI> accept(@NonNull final FileObject fo) {
            if (fo.canRead() && fo.isData()) {
                final String nameExt = fo.getNameExt();
                if (DOCS_FILE_PATTERN.matcher(nameExt).matches() && JAVAFX_FILE_PATTERN.matcher(nameExt).matches()) {
                    final FileObject root = FileUtil.getArchiveRoot(fo);
                    if (root != null) {
                        final List<URI> roots = new ArrayList<>(DOCS_PATHS.size());
                        for (String path : DOCS_PATHS) {
                            final FileObject docRoot = root.getFileObject(path);
                            if (docRoot != null) {
                                roots.add(docRoot.toURI());
                            }
                        }
                        return Collections.unmodifiableCollection(roots);
                    }
                }
            }
            return Collections.emptySet();
        }
    }

}
