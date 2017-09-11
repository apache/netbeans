/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.ui;

import java.io.File;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
abstract class JavaSymbolDescriptorBase extends SymbolDescriptor {

    private final ElementHandle<TypeElement> owner;
    private final ProjectInformation projectInformation;
    private final FileObject root;
    private final ClassIndexImpl ci;
    private volatile FileObject cachedFo;
    private volatile String cachedPath;
    private volatile String ownerName;

    JavaSymbolDescriptorBase(
        @NonNull final ElementHandle<TypeElement> owner,
        @NullAllowed final ProjectInformation projectInformation,
        @NonNull final FileObject root,
        @NonNull final ClassIndexImpl ci) {
        assert owner != null;
        assert root != null;
        assert ci != null;
        this.owner = owner;
        this.projectInformation = projectInformation;
        this.root = root;
        this.ci = ci;
    }

    JavaSymbolDescriptorBase(
            @NonNull final JavaSymbolDescriptorBase other,
            @NullAllowed final String ownerName) {
        this.owner = other.owner;
        this.projectInformation = other.projectInformation;
        this.root = other.root;
        this.ci = other.ci;
        this.cachedFo = other.cachedFo;
        this.cachedPath = other.cachedPath;
        this.ownerName = ownerName != null ?
                ownerName :
                other.ownerName;
    }

    @Override
    @NonNull
    public final String getOwnerName() {
        String on = ownerName;
        if (on == null) {
            ownerName = on = replace(owner.getBinaryName(), getSimpleName());
        }
        return on;
    }

    @Override
    @CheckForNull
    public final FileObject getFileObject() {
        FileObject res = cachedFo;
        if (res == null) {
            final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPath.EMPTY,
                    ClassPath.EMPTY, ClassPathSupport.createClassPath(root));
            res = cachedFo = SourceUtils.getFile(owner, cpInfo);
        }
        return res;
    }

    @Override
    @NonNull
    public final String getFileDisplayPath() {
        String res = cachedPath;
        if (res == null) {
            final File rootFile = FileUtil.toFile(root);
            if (rootFile != null) {
                try {
                    final String binaryName = owner.getBinaryName();
                    String relativePath = ci.getSourceName(binaryName);
                    if (relativePath == null) {
                        relativePath = binaryName;
                        int lastDot = relativePath.lastIndexOf('.');    //NOI18N
                        int csIndex = relativePath.indexOf('$', lastDot);     //NOI18N
                        if (csIndex > 0 && csIndex < relativePath.length()-1) {
                            relativePath = binaryName.substring(0, csIndex);
                        }
                        relativePath = String.format(
                            "%s.%s",    //NOI18N
                            FileObjects.convertPackage2Folder(relativePath, File.separatorChar),
                            FileObjects.JAVA);
                    }
                    res = new File (rootFile, relativePath).getAbsolutePath();
                } catch (IOException | InterruptedException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (res == null) {
                final FileObject fo = getFileObject();
                res = fo == null ?
                    "" :    //NOI18N
                    FileUtil.getFileDisplayName(fo);
            }
            cachedPath = res;
        }
        return res;
    }

    @Override
    @NonNull
    public final String getProjectName() {
        return projectInformation == null ?
            "" :    //NOI18N
            projectInformation.getDisplayName();
    }

    @Override
    @CheckForNull
    public final Icon getProjectIcon() {
        return projectInformation == null ?
            null :
            projectInformation.getIcon();
    }

    @Override
    public final int getOffset() {
        //todo: fixme
        return -1;
    }

    @NonNull
    final FileObject getRoot() {
        return root;
    }

    @NonNull
    final ElementHandle<TypeElement> getOwner() {
        return owner;
    }
    
    @NonNull
    private static String replace(
            @NonNull String name,
            @NonNull final String simpleName) {
        int upBound = name.length();
        int pos = upBound - simpleName.length();
        if ((pos == 0 || (pos > 0 && name.charAt(pos-1) == '$')) &&     //NOI18N
                name.substring(pos).equalsIgnoreCase(simpleName)) {
            upBound = pos;
        }
        int i = 1;
        for (; i < upBound; i++) {
            char c = name.charAt(i);
            if (c == '$' &&             //NOI18N
                name.charAt(i-1) != '.' && //NOI18N
                i < name.length() - 1 && name.charAt(i+1) != '.') { //NOI18N
                break;
            }
        }
        if (i < upBound) {
            final char[] data = name.toCharArray();
            for (; i < upBound; i++) {
                char c = name.charAt(i);
                if (c == '$' &&             //NOI18N
                    name.charAt(i-1) != '.' && //NOI18N
                    i < name.length() - 1 && name.charAt(i+1) != '.') { //NOI18N
                    c = '.';    //NOI18N
                }
                data[i] = c;
            }
            name = new String(data);
        }
        return name;
    }
}
