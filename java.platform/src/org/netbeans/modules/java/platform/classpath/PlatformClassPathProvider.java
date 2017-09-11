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

package org.netbeans.modules.java.platform.classpath;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.modules.SpecificationVersion;


@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.classpath.ClassPathProvider.class, position=150)
public class PlatformClassPathProvider implements ClassPathProvider {
    private static final SpecificationVersion JAVA_9 = new SpecificationVersion("9");   //NOI18N

    private static final Set<? extends String> SUPPORTED_CLASS_PATH_TYPES =
            new HashSet<String>(Arrays.asList(new String[]{
                ClassPath.SOURCE,
                ClassPath.BOOT,
                ClassPath.COMPILE,
                JavaClassPathConstants.MODULE_BOOT_PATH
            }));



    /** Creates a new instance of PlatformClassPathProvider */
    public PlatformClassPathProvider() {
    }
    
    
    public ClassPath findClassPath(FileObject fo, String type) {
        if (!SUPPORTED_CLASS_PATH_TYPES.contains(type)) {
            return null;
        }
        if (fo == null || type == null) {
            throw new IllegalArgumentException();
        }
        JavaPlatform lp = this.getLastUsedPlatform(fo);
        JavaPlatform[] platforms;
        if (lp != null) {
            platforms = new JavaPlatform[] {lp};
        }
        else {
            JavaPlatformManager manager = JavaPlatformManager.getDefault();
            platforms = manager.getInstalledPlatforms();
        }
        for (JavaPlatform jp : platforms) {
            ClassPath bootClassPath = jp.getBootstrapLibraries();
            ClassPath libraryPath = jp.getStandardLibraries();
            ClassPath sourcePath = jp.getSourceFolders();
            FileObject root = null;
            if (ClassPath.SOURCE.equals(type) && sourcePath != null &&
                (root = sourcePath.findOwnerRoot(fo))!=null) {
                this.setLastUsedPlatform (root,jp);
                return sourcePath;
            } else if (ClassPath.BOOT.equals(type) &&
                    (root = getArtefactOwner(fo, bootClassPath, libraryPath, sourcePath)) != null ) {
                this.setLastUsedPlatform (root,jp);
                return bootClassPath;
            } else if (ClassPath.COMPILE.equals(type)) {
                if (libraryPath != null && (root = libraryPath.findOwnerRoot(fo))!=null) {
                    this.setLastUsedPlatform (root,jp);
                    return libraryPath;
                }
                else if ((bootClassPath != null && (root = bootClassPath.findOwnerRoot (fo))!=null) ||
                    (sourcePath != null && (root = sourcePath.findOwnerRoot(fo)) != null)) {
                    return this.getEmptyClassPath ();
                }
            } else if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)  &&
                    JAVA_9.compareTo(jp.getSpecification().getVersion()) <= 0 &&
                    (root = getArtefactOwner(fo, bootClassPath, libraryPath, sourcePath)) != null) {
                this.setLastUsedPlatform (root,jp);
                return bootClassPath;
            }
        }
        return null;
    }

    private synchronized ClassPath getEmptyClassPath () {
        if (this.emptyCp == null ) {
            this.emptyCp = ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        }
        return this.emptyCp;
    }

    private synchronized void setLastUsedPlatform (FileObject root, JavaPlatform platform) {
        this.lastUsedRoot = root;
        this.lastUsedPlatform = platform;
    }

    private synchronized JavaPlatform getLastUsedPlatform (FileObject file) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,file)) {
            return lastUsedPlatform;
        }
        else {
            return null;
        }
    }

    @CheckForNull
    private static FileObject getArtefactOwner(
            @NonNull final FileObject file,
            @NonNull final ClassPath... cps) {
        for (ClassPath cp : cps) {
            FileObject root;
            if (cp != null && (root = cp.findOwnerRoot (file)) != null) {
                return root;
            }
        }
        return null;
    }

    private FileObject lastUsedRoot;
    private JavaPlatform lastUsedPlatform;
    private ClassPath emptyCp;
}
