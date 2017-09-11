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

package org.netbeans.modules.maven.osgi;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.eclipse.osgi.util.ManifestElement;
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
import org.osgi.framework.BundleException;
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
    private static final String toolTip = "<img src=\"" + OSGiJarAccessibilityQueryImpl.class.getClassLoader().getResource(BADGE) + "\">&nbsp;" //NOI18N
            + Tooltip_manifest();//NOI18N

    private final WeakHashMap<FileObject, List<ManifestElement>> publicCache = new WeakHashMap<FileObject, List<ManifestElement>>();
    private final List<ManifestElement> NOT_OSGIJAR = new ArrayList<ManifestElement>();
    
    @Override
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject jarFile = FileUtil.getArchiveFile(pkg);
        boolean notOSGi = true;
        if (jarFile != null) {
            FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
            synchronized (publicCache) {
                List<ManifestElement> pub = publicCache.get(jarRoot);
                
                if (pub != null) {
                    if (pub == NOT_OSGIJAR) {
                        return null;
                    }
                    String relPath = FileUtil.getRelativePath(jarRoot, pkg);
                    assert relPath != null : "null path for : " + jarRoot + ", " + pkg;
                    if(relPath == null) {
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
                    List<ManifestElement> pub = null;
                    String exportPack = mf.getMainAttributes().getValue(OSGiConstants.EXPORT_PACKAGE);
                    if (exportPack != null) {
                        try {
                            ManifestElement[] mans = ManifestElement.parseHeader(OSGiConstants.EXPORT_PACKAGE, exportPack);
                            pub = Arrays.asList(mans);
                        } catch (BundleException ex) {
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
                publicCache.put(jarRoot, notOSGi ? NOT_OSGIJAR : Collections.<ManifestElement>emptyList());
            }
        }
        return notOSGi ? null : Boolean.FALSE;
        
    }

    private Boolean check(List<ManifestElement> pub, String packageName) {
        for (ManifestElement p : pub) {
            if (packageName.equals(p.getValue())) {
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
                List<ManifestElement> pub = publicCache.get(jarRoot);
                
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
                    List<ManifestElement> pub = null;
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
        return ImageUtilities.addToolTipToImage(ImageUtilities.loadImage(BADGE), toolTip);
    }

}
