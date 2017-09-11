/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.source.ui;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.ui.Icons;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @todo Resolve with TypeDescription
 *
 * @author Petr Hrebejk
 */
public class JavaTypeDescription extends TypeDescriptor {

    private static final Logger LOG = Logger.getLogger(JavaTypeDescription.class.getName());
    private static final String PATH_FROM_HANDLE = "";  //NOI18N

    private final JavaTypeProvider.CacheItem cacheItem;
    private final ElementHandle<TypeElement> handle;
    private String cachedRelPath;
    private String simpleName;
    private String outerName;
    private String packageName;
    private Icon icon;
    private volatile String cachedPath;

    JavaTypeDescription(
            @NonNull final JavaTypeProvider.CacheItem cacheItem,
            @NonNull final ElementHandle<TypeElement> handle,
            @NullAllowed final String simpleName,
            @NullAllowed final String relativePath) {
       this.cacheItem = cacheItem;
       this.handle = handle;
       this.cachedRelPath = relativePath == null ?
            PATH_FROM_HANDLE :
            relativePath;
       this.simpleName = simpleName;
       init();
    }

    JavaTypeDescription(
            @NonNull final JavaTypeProvider.CacheItem cacheItem,
            @NonNull final ElementHandle<TypeElement> handle) {
       this.cacheItem = cacheItem;
       this.handle = handle;
       init();
    }

    @Override
    public void open() {
        final FileObject root = cacheItem.getRoot();
        if (root == null) {
            final String message = NbBundle.getMessage(JavaTypeDescription.class, "LBL_JavaTypeDescription_nosource",handle.getQualifiedName());
            StatusDisplayer.getDefault().setStatusText(message);
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
        if (bootPath == null) {
            bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        final ClasspathInfo ci;
        if (cacheItem.isBinary()) {
            final ClassPath compilePath = ClassPathSupport.createClassPath(root);
            ci = ClasspathInfo.create(
                bootPath,
                compilePath,
                ClassPath.EMPTY);
        } else {
            final ClassPath sourcePath = ClassPathSupport.createClassPath(root);
            ci = ClasspathInfo.create(
                bootPath,
                ClassPath.EMPTY,
                sourcePath);
        }
        if ( cacheItem.isBinary() ) {
            final ElementHandle<TypeElement> eh = handle;
            if (!ElementOpen.open(ci, eh)) {
                final String message = NbBundle.getMessage(JavaTypeDescription.class, "LBL_JavaTypeDescription_nosource",eh.getQualifiedName());
                StatusDisplayer.getDefault().setStatusText(message);
                Toolkit.getDefaultToolkit().beep();
            }
        }
        else {
            final FileObject file = SourceUtils.getFile(handle, ci);
            boolean opened = false;
            if (file != null) {
                opened = ElementOpen.open(file, handle);
            }
            if (!opened) {
                StringBuilder name = new StringBuilder ();
                if (packageName != null) {
                    name.append(packageName);
                    name.append('.');           //NOI18N
                }
                if (outerName != null) {
                    name.append(outerName);
                }
                else {
                    name.append(simpleName);
                }
                final String message = NbBundle.getMessage(JavaTypeDescription.class, "LBL_JavaTypeDescription_nosource",name.toString());
                StatusDisplayer.getDefault().setStatusText(message);
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String getOuterName() {
        return outerName;
    }

    @Override
    public FileObject getFileObject() {
        final FileObject root = cacheItem.getRoot();
        final String relativePath = getRelativePath(
            handle.getBinaryName(),
            cacheItem.getClassIndex(),
            cacheItem.isBinary(),
            cacheItem.getRootURI());
        return root == null ?
            null :
            root.getFileObject(relativePath);
    }

    @Override
    public String getFileDisplayPath() {
        String path = cachedPath;
        if (path == null) {
            final URI uri = cacheItem.getRootURI();
            assert uri != null : "Root null for created entry";    //NOI18N
            try {
                final File rootFile = Utilities.toFile(uri);
                String relativePath = getRelativePath(
                    handle.getBinaryName(),
                    cacheItem.getClassIndex(),
                    cacheItem.isBinary(),
                    uri);
                path = new File(rootFile,relativePath).getAbsolutePath();
            } catch (IllegalArgumentException e) {
                final FileObject rootFo = cacheItem.getRoot();
                path = rootFo == null ?
                    "" : //NOI18N
                    FileUtil.getFileDisplayName(rootFo);
            }
            cachedPath = path;
        }
        return path;
    }

    @Override
    public String getTypeName() {
        StringBuilder sb = new StringBuilder( simpleName );
        if( outerName != null  ) {
            sb.append(" in ").append( outerName );
        }
        return sb.toString();
    }

    @Override
    public String getContextName() {
        StringBuilder sb = new StringBuilder();
        sb.append( " (").append( packageName == null ? "Default Package" : packageName).append(")");
        return sb.toString();


    }

    @Override
    public String getProjectName() {
        String projectName = cacheItem.getProjectName();
        return projectName == null ? "" : projectName; // NOI18N
    }

    @Override
    public Icon getProjectIcon() {
        return cacheItem.getProjectIcon();
    }

    @Override
    public synchronized Icon getIcon() {
        return icon;
    }

    @Override
    public int getOffset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( simpleName );
        if( outerName != null  ) {
            sb.append(" in ").append( outerName );
        }
        sb.append( " (").append( packageName == null ? "Default Package" : packageName).append(")");
        if (cacheItem.getProjectName() != null ) {
            sb.append( " [").append( cacheItem.getProjectName()).append("]");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hc = 17;
        hc = hc * 31 + handle.hashCode();
        hc = hc * 31 + handle.hashCode();
        return hc;
    }

    @Override
    public boolean equals (@NullAllowed final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof JavaTypeDescription)) {
            return false;
        }
        JavaTypeDescription otherJTD = (JavaTypeDescription) other;
        return handle.equals(otherJTD.handle) && cacheItem.equals(otherJTD.cacheItem);
    }

    public ElementHandle<TypeElement> getHandle() {
        return handle;
    }

    private void init() {
        final String typeName = this.handle.getBinaryName();
        int lastDot = typeName.lastIndexOf('.'); // NOI18N
        if ( lastDot == -1 ) {
            final String[] nms = parseName(typeName,0,simpleName);
            outerName = nms[0];
            simpleName = nms[1];
        } else {
            packageName = typeName.substring(0, lastDot);
            final String[] nms = parseName(typeName,lastDot+1,simpleName);
            outerName = nms[0];
            simpleName = nms[1];
        }
        icon = Icons.getElementIcon (handle.getKind(), null);
    }
    
    @NonNull
    private static String[] parseName(
            @NonNull final String binaryName,
            final int clzNameStart,
            @NullAllowed final String simpleName) {
        final String[] res = new String[2];
        if (simpleName != null) {
            res[1] = simpleName;
            int index = binaryName.length() - simpleName.length() -1;
            if (index > clzNameStart) {
                res[0] = replace(binaryName.substring(clzNameStart, index));
            }
        } else {
            final int lastDollar = binaryName.lastIndexOf('$'); // NOI18N
            if (lastDollar == -1) {
                res[1] = replace(binaryName.substring(clzNameStart));
            } else {
                res[1] = binaryName.substring(lastDollar + 1);
                res[0] = replace(binaryName.substring(clzNameStart, lastDollar));
            }
        }
        return res;
    }
    
    @NonNull
    private static String replace(@NonNull String name) {
        int i = 1;
        for (; i<name.length(); i++) {
            char c = name.charAt(i);
            if (c == '$') { //NOI18N
                break;
            }
        }
        if (i < name.length()) {
            final char[] data = name.toCharArray();
            for (; i<name.length(); i++) {
                char c = name.charAt(i);
                if (c == '$') { //NOI18N
                    c = '.';    //NOI18N
                }
                data[i] = c;
            }
            name = new String(data);
        }
        return name;
    }

    private String getRelativePath(
        @NonNull final String binaryName,
        @NullAllowed final ClassIndexImpl ci,
        final boolean isBinary,
        @NullAllowed final URI root) {
        String relativePath = cachedRelPath;
        if (relativePath == null) {
            if (ci == null) {
                LOG.log (
                    Level.WARNING,
                    "No ClassIndex for {0} in {1}", //NOI18N
                    new Object[]{
                        binaryName,
                        root});
            } else {
                try {
                    relativePath = ci.getSourceName(binaryName);
                } catch (IOException | InterruptedException ex) {
                    LOG.log (
                        Level.WARNING,
                        "Broken ClassIndex for {0} in {1}", //NOI18N
                        new Object[]{
                            binaryName,
                            root});
                }
            }
            if (relativePath == null) {
                relativePath = PATH_FROM_HANDLE;
            }
            cachedRelPath = relativePath;
        }
        if (relativePath == PATH_FROM_HANDLE) {
                relativePath = binaryName;
                int lastDot = relativePath.lastIndexOf('.');    //NOI18N
                int csIndex = relativePath.indexOf('$', lastDot);     //NOI18N
                if (csIndex > 0 && csIndex < relativePath.length()-1) {
                    relativePath = binaryName.substring(0, csIndex);
                }
                relativePath = String.format(
                    "%s.%s",    //NOI18N
                    FileObjects.convertPackage2Folder(relativePath, File.separatorChar),
                    isBinary ?
                       FileObjects.CLASS :
                       FileObjects.JAVA);
                //No need to cache fast to compute
        }
        return relativePath;
    }
}
