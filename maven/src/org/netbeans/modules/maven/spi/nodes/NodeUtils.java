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

package org.netbeans.modules.maven.spi.nodes;

import java.awt.Image;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mkleint
 */
public class NodeUtils {
    
    private static final Logger LOG = Logger.getLogger(NodeUtils.class.getName());

    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final @StaticResource String ICON_PATH = "org/netbeans/modules/maven/defaultFolder.gif"; // NOI18N
    private static final @StaticResource String OPENED_ICON_PATH = "org/netbeans/modules/maven/defaultFolderOpen.gif"; // NOI18N

    // XXX could alternately register a URLMapper so that FileUtil.toFileObject works on repo files (which would be useful for making deserialization of r/o TCs work)
    /**
     * Produces a variant of a file in the local repository that the IDE will consider read-only.
     * You can then use {@link OpenCookie} or {@link EditCookie} to open in a read-only text editor window.
     * @param file a file possibly in the local repository
     * @return the same file but from a transient r/o filesystem; or the original file, if a folder or not in the local repository
     * @since 2.10
     */
    public static synchronized FileObject readOnlyLocalRepositoryFile(FileObject file) {
        File f = FileUtil.toFile(file);
        if (f == null || !file.isData()) {
            return file;
        }
        LocalFileSystem fs = repoFS != null ? repoFS.get() : null;
        if (fs == null) {
            fs = new LocalFileSystem();
            fs.setReadOnly(true);
            try {
                fs.setRootDirectory(EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile());
            } catch (Exception x) {
                throw new AssertionError(x);
            }
            repoFS = new WeakReference<LocalFileSystem>(fs);
        }
        File root = fs.getRootDirectory();
        String path = FileUtilities.getRelativePath(root, f);
        if (path == null) {
            return file;
        }
        FileObject ro = fs.findResource(path);
        if (ro == null) {
            fs.refresh(false);
            ro = fs.findResource(path);
        }
        if (ro == null) {
            Logger.getLogger(NodeUtils.class.getName()).log(Level.WARNING, "Cannot find r/o equivalent of {0} as {1} in {2}", new Object[] {f, path, root});
            return file;
        }
        return ro;
    }
    private static Reference<LocalFileSystem> repoFS;

    
    /**
     * open pom file for given FileObject, for items from local repository creates a read-only FO.
     * @param fo 
     * @since 2.67
     */
    public static void openPomFile(FileObject fo) {
        DataObject dobj;
        try {
            dobj = DataObject.find(NodeUtils.readOnlyLocalRepositoryFile(fo));
            EditCookie edit = dobj.getLookup().lookup(EditCookie.class);
            if (edit != null) {
                edit.edit();
            }
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.FINE, "Cannot find dataobject", ex);
        }
    }

    /**
     * Icon for a dependency JAR file.
     * @since 2.37
     * @deprecated  since 2.93, please use the version from <code>IconResources</code>
     */
    @Deprecated
    public static final String ICON_DEPENDENCY_JAR = IconResources.ICON_DEPENDENCY_JAR;

    private NodeUtils() {}
    
}
