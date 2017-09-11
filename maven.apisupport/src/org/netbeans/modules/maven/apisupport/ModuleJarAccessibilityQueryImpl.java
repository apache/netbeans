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

package org.netbeans.modules.maven.apisupport;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.spi.nodes.DependencyTypeIconBadge;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import static org.netbeans.modules.maven.apisupport.Bundle.*;

/**
 *
 * @author mkleint
 */
@ServiceProviders({
    @ServiceProvider(service = AccessibilityQueryImplementation.class), 
    @ServiceProvider(service = DependencyTypeIconBadge.class)
})
public class ModuleJarAccessibilityQueryImpl implements AccessibilityQueryImplementation, DependencyTypeIconBadge {
    private static final Logger LOG = Logger.getLogger(ModuleJarAccessibilityQueryImpl.class.getName());
    private static final @StaticResource String BADGE = "org/netbeans/modules/maven/apisupport/module-badge.png";
    private static final String toolTip = "<img src=\"" + ModuleJarAccessibilityQueryImpl.class.getClassLoader().getResource(BADGE) + "\">&nbsp;" //NOI18N
            + Tooltip_manifest();//NOI18N
    

    private static final WeakHashMap<FileObject, List<Pattern>> cache = new WeakHashMap<FileObject, List<Pattern>>();
    private static final List<Pattern> NOT_MODULE = new ArrayList<Pattern>();
    
    @Override
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject jarFile = FileUtil.getArchiveFile(pkg);
        if (jarFile != null) {
            FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
            synchronized (cache) {
                List<Pattern> patt = cache.get(jarRoot);
                if (patt != null) {
                    if (patt != NOT_MODULE) {
                        return AccessQueryImpl.check(patt, FileUtil.getRelativePath(jarRoot, pkg).replace("/", "."));
                    } else {
                        return null;
                    }
                }
            }
            FileObject manifest = jarRoot.getFileObject("META-INF/MANIFEST.MF");
            if (manifest != null) {
                try {
                    Manifest mf = new Manifest(manifest.getInputStream());
                    String publicPack = mf.getMainAttributes().getValue(AccessQueryImpl.ATTR_PUBLIC_PACKAGE);
                    if (publicPack != null) {
                        List<Pattern> patt = AccessQueryImpl.prepareManifestPublicPackagesPatterns(publicPack);
                        synchronized (cache) {
                            cache.put(jarRoot, patt);
                        }
                        return AccessQueryImpl.check(patt, FileUtil.getRelativePath(jarRoot, pkg).replace("/", "."));
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "cannot read manifest", ex);
                }
            }
            synchronized (cache) {
                cache.put(jarRoot, NOT_MODULE);
            }
        }
        return null;
        
    }

    @Override
    public Image getBadgeIcon(FileObject jarFile, Artifact art) {
        if ("nbm-file".equals(art.getType())) {
            return getIcon();
        }
        if (jarFile != null && FileUtil.isArchiveFile(jarFile)) {
            FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
            synchronized (cache) {
                List<Pattern> patt = cache.get(jarRoot);
                if (patt != null) {
                    if (patt != NOT_MODULE) {
                        return getIcon();
                    } else {
                        return null;
                    }
                }
            }
            FileObject manifest = jarRoot.getFileObject("META-INF/MANIFEST.MF");
            if (manifest != null) {
                try {
                    Manifest mf = new Manifest(manifest.getInputStream());
                    String publicPack = mf.getMainAttributes().getValue(AccessQueryImpl.ATTR_PUBLIC_PACKAGE);
                    if (publicPack != null) {
                        //TODO do we create the patterns here, or just ignore it if ignoring is the path, then we should check a different attribute?
                        List<Pattern> patt = AccessQueryImpl.prepareManifestPublicPackagesPatterns(publicPack);
                        synchronized (cache) {
                            cache.put(jarRoot, patt);
                        }
                        return getIcon();
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "cannot read manifest", ex);
                }
            }
            synchronized (cache) {
                cache.put(jarRoot, NOT_MODULE);
            }
        }
        return null;
        
    }

    @NbBundle.Messages("Tooltip_manifest=Contains NetBeans module manifest headers")
    private Image getIcon() {
        return ImageUtilities.addToolTipToImage(ImageUtilities.loadImage(BADGE), toolTip);
    }
    
    

}
