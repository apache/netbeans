/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
