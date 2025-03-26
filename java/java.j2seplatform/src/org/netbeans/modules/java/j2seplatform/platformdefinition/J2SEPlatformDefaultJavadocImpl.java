/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.spi.J2SEPlatformDefaultJavadoc;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default Javadoc for Java.
 * @author Tomas Zezula
 */
@ServiceProvider(service = J2SEPlatformDefaultJavadoc.class, position = 100, path = "org-netbeans-api-java/platform/j2seplatform/defaultJavadocProviders")
public final class J2SEPlatformDefaultJavadocImpl implements J2SEPlatformDefaultJavadoc {

    private static final Logger LOG = Logger.getLogger(J2SEPlatformDefaultJavadocImpl.class.getName());

    @Override
    public Collection<URI> getDefaultJavadoc(@NonNull final JavaPlatform platform) {
        // local
        final List<URI> result = new ArrayList<>();
        final JavadocFilter filter = new JavadocFilter();
        for (FileObject folder : platform.getInstallFolders()) {
            for (FileObject file : folder.getChildren()) {
                result.addAll(filter.accept(file));
            }
        }
        if (!result.isEmpty()) {
            return Collections.unmodifiableList(result);
        }
        // remote
        String version = platform.getSpecification().getVersion().toString();
        String location = computeJavaDocURL(version);
        if (location != null) {
            try {
                return Collections.singletonList(new URI(location));
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        return Collections.emptyList();
    }

    private static String computeJavaDocURL(String version) {
        switch (version) {
            case "1.0": // NOI18N
            case "1.1": // NOI18N
            case "1.2": // NOI18N
            case "1.3": // NOI18N
            case "1.4": return null; // NOI18N
            case "1.5": return "https://docs.oracle.com/javase/1.5.0/docs/api/"; // NOI18N
            case "1.6": return "https://docs.oracle.com/javase/6/docs/api/"; // NOI18N
            case "1.7": return "https://docs.oracle.com/javase/7/docs/api/"; // NOI18N
            case "1.8": return "https://docs.oracle.com/javase/8/docs/api/"; // NOI18N
        }
        try {
            int feature = Integer.parseInt(version);
            if (feature >= 9) {
                int latestGA = computeLatestGAVersion();
                if (feature <= latestGA) {
                    return "https://docs.oracle.com/en/java/javase/" + feature + "/docs/api/"; // NOI18N
                } else if (feature <= latestGA + 3) {
                    return "https://download.java.net/java/early_access/jdk" + feature + "/docs/api/"; // NOI18N
                }
            }
        } catch (IllegalArgumentException ignore) {}
        LOG.log(Level.WARNING, "unrecognized Java spec version: {0}", version); // NOI18N
        return null;
    }

    /**
     * Computes the feature version of the latest generally available JDK release.
     */
    private static int computeLatestGAVersion() {
        // timezone shouldn't matter since the accuracy is worse than a day
        LocalDate jdk9 = LocalDate.of(2017, Month.SEPTEMBER, 21); // start of 6 month schedule
        int latest = 9 + (int) (ChronoUnit.MONTHS.between(jdk9, LocalDate.now()) / 6);
        return Math.max(latest, SourceVersion.latest().ordinal()); // in case system time is wrong, use nb-javac version as lower bound
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
