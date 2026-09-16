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

package org.netbeans.modules.maven.osgi;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.spi.nodes.DependencyTypeIconBadge;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

import static org.netbeans.modules.maven.osgi.Bundle.*;

/**
 *
 * @author mkleint
 */
@ServiceProviders({
    @ServiceProvider(service = AccessibilityQueryImplementation.class),
    @ServiceProvider(service = DependencyTypeIconBadge.class)
})
public class OSGiJarAccessibilityQueryImpl implements AccessibilityQueryImplementation, DependencyTypeIconBadge {
    private static final Logger LOG = Logger.getLogger(OSGiJarAccessibilityQueryImpl.class.getName());
    private static final @StaticResource String BADGE = "org/netbeans/modules/maven/osgi/maven_osgi_badge.png";
    private static final String TOOLTIP = "<img src=\"" + OSGiJarAccessibilityQueryImpl.class.getClassLoader().getResource(BADGE) + "\">&nbsp;" //NOI18N
            + Tooltip_manifest();//NOI18N

    private final WeakHashMap<FileObject, List<String>> publicCache = new WeakHashMap<>();
    private final List<String> NOT_OSGIJAR = new ArrayList<>();

    @Override
    @SuppressWarnings("null") // relPath is runtime null checked in addition to assert
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject jarFile = FileUtil.getArchiveFile(pkg);
        boolean notOSGi = true;
        if (jarFile != null) {
            FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
            synchronized (publicCache) {
                List<String> pub = publicCache.get(jarRoot);

                if (pub != null) {
                    if (pub == NOT_OSGIJAR) {
                        return null;
                    }
                    String relPath = FileUtil.getRelativePath(jarRoot, pkg);
                    assert relPath != null : "null path for : " + jarRoot + ", " + pkg;
                    if (relPath == null) {
                        LOG.log(Level.WARNING, "null path for : {0}, {1}", new Object[]{jarRoot, pkg});
                        return Boolean.FALSE;
                    }
                    return check(pub, relPath.replace("/", "."));
                }
            }
            FileObject manifest = jarRoot.getFileObject("META-INF/MANIFEST.MF");
            if (manifest != null) {
                try {
                    Manifest mf = new Manifest(manifest.getInputStream());
                    List<String> pub = null;
                    String exportPack = mf.getMainAttributes().getValue(OSGiConstants.EXPORT_PACKAGE);
                    if (exportPack != null) {
                        try {
                            pub = splitExportPackages(exportPack);
                        } catch (RuntimeException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (pub != null) {
                        synchronized (publicCache) {
                            publicCache.put(jarRoot, pub);
                        }
                        String relPath = FileUtil.getRelativePath(jarRoot, pkg);
                        assert relPath != null : "null path for : " + jarRoot + ", " + pkg;
                        if(relPath == null) {
                            LOG.log(Level.WARNING, "null path for : {0}, {1}", new Object[]{jarRoot, pkg});
                            return Boolean.FALSE;
                        }
                        return check(pub, relPath.replace("/", "."));
                    } else {
                        notOSGi = mf.getMainAttributes().getValue(OSGiConstants.BUNDLE_SYMBOLIC_NAME) == null;
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "cannot read manifest", ex);
                }
            }
            synchronized (publicCache) {
                publicCache.put(jarRoot, notOSGi ? NOT_OSGIJAR : List.of());
            }
        }
        return notOSGi ? null : Boolean.FALSE;

    }

    private Boolean check(List<String> pub, String packageName) {
        for (String p : pub) {
            if (packageName.equals(p)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Image getBadgeIcon(FileObject jarFile, Artifact art) {
        if (jarFile != null && FileUtil.isArchiveFile(jarFile)) {
            FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
            synchronized (publicCache) {
                List<String> pub = publicCache.get(jarRoot);

                if (pub != null) {
                    if (pub == NOT_OSGIJAR) {
                        return null;
                    }
                    return getIcon();
                }
            }
            FileObject manifest = jarRoot.getFileObject("META-INF/MANIFEST.MF");
            if (manifest != null) {
                try {
                    Manifest mf = new Manifest(manifest.getInputStream());
                    String name = mf.getMainAttributes().getValue(OSGiConstants.BUNDLE_SYMBOLIC_NAME);
                    if (name != null) {
                        return getIcon();
                    }
                } catch (IOException ex) {
                     LOG.log(Level.FINE, "cannot read manifest", ex);
                }
            }
            synchronized (publicCache) {
                publicCache.put(jarRoot, NOT_OSGIJAR);
            }
        }
        return null;
    }

    @NbBundle.Messages("Tooltip_manifest=Contains OSGi manifest headers")
    private Image getIcon() {
        return ImageUtilities.addToolTipToImage(ImageUtilities.loadImage(BADGE), TOOLTIP);
    }

    /**
     * OSGI-Specification declares:
     *
     * <pre>
     * Export-Package  ::= export ( ',' export)*
     * export          ::= package-names ( ';' parameter )*
     * package-names   ::= package-name                // See 1.3.2
     *                   ( ';' package-name )*
     * </pre>
     *
     * @param exportPkg String formatted according to Export-Package
     * @return list of package-name entries
     */
    // Same implementation is used in org.netbeans.core.netigso.Netigso and
    // org.netbeans.modules.maven.osgi.OSGiJarAccessibilityQueryImpl copied to
    // not introduce module dependencies. Tests are in Netigso, when updated,
    // update both places!
    static List<String> splitExportPackages(String exportPkg) {
        List<String> exports = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean inQuotedString = false;
        for (int i = 0; i < exportPkg.length(); i++) {
            char nextChar = exportPkg.charAt(i);
            switch(nextChar) {
                case '"' -> {
                    inQuotedString = ! inQuotedString;
                    buffer.append(nextChar);
                }
                case ',' -> {
                    if (inQuotedString) {
                        buffer.append(nextChar);
                    } else {
                        exports.add(buffer.toString());
                        buffer.setLength(0);
                    }
                }
                case '\\' -> {
                    buffer.append(nextChar);
                    if (inQuotedString) {
                        if((i + 1) == exportPkg.length()) {
                            throw new IllegalStateException("Invalid escape sequence");
                        }
                        nextChar = exportPkg.charAt(i + 1);
                        i++;
                        if (nextChar == '"' || nextChar == '\\') {
                            buffer.append(nextChar);
                        } else {
                            throw new IllegalStateException("Invalid escape sequence");
                        }
                    }
                }
                default -> buffer.append(nextChar);
            }
        }
        if(! buffer.isEmpty()){
            exports.add(buffer.toString());
        }
        List<String> packageNames = new ArrayList<>();
        // Each export statement can hold multiple package names separated by
        // semicolon, so split on semicolon. package-name and parameter are
        // discerned based on the presence of a equals character. A package-name
        // must not contain it, a parameter must.
        for(String export: exports) {
            for(String packageCandiate: export.split(";")) {
                // package-name list ends with first parameter
                if(packageCandiate.contains("=")) {
                    break;
                }
                packageNames.add(packageCandiate);
            }
        }
        return packageNames;
    }
}
