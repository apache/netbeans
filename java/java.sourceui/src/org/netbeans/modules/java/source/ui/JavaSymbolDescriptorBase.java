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
