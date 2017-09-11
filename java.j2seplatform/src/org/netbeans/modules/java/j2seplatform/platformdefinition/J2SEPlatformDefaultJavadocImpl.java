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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.spi.J2SEPlatformDefaultJavadoc;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default Javadoc for J2SE.
 * @author Tomas Zezula
 */
@ServiceProvider(service =J2SEPlatformDefaultJavadoc.class, position = 100, path = "org-netbeans-api-java/platform/j2seplatform/defaultJavadocProviders")
public final class J2SEPlatformDefaultJavadocImpl implements J2SEPlatformDefaultJavadoc {

    private static final Logger LOG = Logger.getLogger(J2SEPlatformDefaultJavadocImpl.class.getName());
    private static final Map<String,String> OFFICIAL_JAVADOC = new HashMap<String,String>();
    static {
        OFFICIAL_JAVADOC.put("1.0", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.1", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.2", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.3", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.4", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.5", "https://docs.oracle.com/javase/1.5.0/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.6", "https://docs.oracle.com/javase/6/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.7", "https://docs.oracle.com/javase/7/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.8", "https://docs.oracle.com/javase/8/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("9", null); //Does not exist for now
    }

    @Override
    public Collection<URI> getDefaultJavadoc(@NonNull final JavaPlatform platform) {
        final List<URI> result = new ArrayList<>();
        final JavadocFilter filter = new JavadocFilter();
        for (FileObject folder : platform.getInstallFolders()) {
            for (FileObject file : folder.getChildren()) {
                final Collection<? extends URI> roots = filter.accept(file);
                result.addAll(roots);
            }
        }
        if (!result.isEmpty()) {
            return Collections.unmodifiableList(result);
        }
        String version = platform.getSpecification().getVersion().toString();
        if (!OFFICIAL_JAVADOC.containsKey(version)) {
            LOG.log(Level.WARNING, "unrecognized Java spec version: {0}", version);
        }
        String location = OFFICIAL_JAVADOC.get(version);
        if (location != null) {
            try {
                return Collections.singletonList(new URI(location));
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        return Collections.emptyList();
    }

    private static final class JavadocFilter {
        private static final Pattern DOCS_FILE_PATTERN = Pattern.compile(".*docs.*\\.(zip|jar)",Pattern.CASE_INSENSITIVE);   //NOI18N
        private static final Pattern JAVAFX_FILE_PATTERN = Pattern.compile(".*j(ava)?fx.*", Pattern.CASE_INSENSITIVE);  //NOI18N
        private static final Collection<String> DOCS_PATHS;
        static {
            final List<String> paths = new ArrayList<>(3);
            paths.add("docs/api/");     //NOI18N
            paths.add("docs/jdk/api/"); //NOI18N
            paths.add("docs/jre/api/"); //NOI18N
            DOCS_PATHS = Collections.unmodifiableList(paths);
        }
        private static final Map<String,String> VENDOR_DOCS;
        static {
            Map<String,String> docs = new HashMap<>();
            docs.put("appledocs.jar", "appledoc/api/");     //NOI18N
            VENDOR_DOCS = Collections.unmodifiableMap(docs);
        }

        @NonNull
        Collection<? extends URI> accept(@NonNull FileObject fo) {
            if (fo.canRead()) {
                if (fo.isFolder()) {
                    if ("docs".equals(fo.getName())) {  //NOI18N
                        return Collections.singleton(fo.toURI());
                    }
                } else if (fo.isData()) {
                    final String nameExt = fo.getNameExt();
                    final String vendorPath = VENDOR_DOCS.get(nameExt);
                    if (vendorPath != null) {
                        if (FileUtil.isArchiveFile(fo)) {
                            try {
                                return Collections.singleton(
                                    new URL (FileUtil.getArchiveRoot(fo.toURL()).toExternalForm() + vendorPath).toURI());
                            } catch (MalformedURLException | URISyntaxException e) {
                                LOG.log(
                                    Level.INFO,
                                    "Invalid Javadoc URI for file : {0}, reason: {1}",
                                    new Object[]{
                                        FileUtil.getFileDisplayName(fo),
                                        e.getMessage()
                                });
                                //pass
                            }
                        }
                    } else if (DOCS_FILE_PATTERN.matcher(nameExt).matches() && !JAVAFX_FILE_PATTERN.matcher(nameExt).matches()) {
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
            }
            return Collections.emptySet();
        }
    }
}
