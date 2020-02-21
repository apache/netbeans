/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.api;

import org.openide.filesystems.FileSystem;

/**
 * Type safe properties
 * 
 * 
 * @param <T> property type
 */
public final class ProviderPropertyType<T> {

    public enum PropertyKind {
        BinaryFile,
        ArtifactFileSystem,
        BinaryFiles,
        SourceFile,
        MakeLogFile,
        Folder,
        Folders,
        String,
        Boolean,
        Object
    }    
    
    public static final ProviderPropertyType<String> ExecLogPropertyType = new ProviderPropertyType<>("exec-log-file", PropertyKind.MakeLogFile); //NOI18N
    public static final ProviderPropertyType<String> MakeLogPropertyType = new ProviderPropertyType<>("make-log-file", PropertyKind.MakeLogFile); //NOI18N
    public static final ProviderPropertyType<FileSystem> LogFileSystemPropertyType = new ProviderPropertyType<>("log-filesystem", PropertyKind.ArtifactFileSystem); //NOI18N
    public static final ProviderPropertyType<String> RestrictSourceRootPropertyType = new ProviderPropertyType<>("restrict_source_root", PropertyKind.String); //NOI18N
    public static final ProviderPropertyType<String> RestrictCompileRootPropertyType = new ProviderPropertyType<>("restrict_compile_root", PropertyKind.String); //NOI18N

    public static final ProviderPropertyType<String> ExecutablePropertyType = new ProviderPropertyType<>("executable", PropertyKind.BinaryFile); //NOI18N
    public static final ProviderPropertyType<FileSystem> BinaryFileSystemPropertyType = new ProviderPropertyType<>("binary-filesystem", PropertyKind.ArtifactFileSystem); //NOI18N
    public static final ProviderPropertyType<String[]> LibrariesPropertyType = new ProviderPropertyType<>("libraries", PropertyKind.BinaryFiles); //NOI18N
    public static final ProviderPropertyType<Boolean> FindMainPropertyType = new ProviderPropertyType<>("find_main", PropertyKind.Boolean); //NOI18N
    
    public static final ProviderPropertyType<String> ExecutableFolderPropertyType = new ProviderPropertyType<>("folder", PropertyKind.Folder); //NOI18N
    
    public static final ProviderPropertyType<String> ModelFolderPropertyType = new ProviderPropertyType<>("model-folder", PropertyKind.Folder); //NOI18N
    public static final ProviderPropertyType<Boolean> PreferLocalFilesPropertyType = new ProviderPropertyType<>("prefer-local", PropertyKind.Boolean); //NOI18N
    
    private final String key;
    private final PropertyKind kind;

    ProviderPropertyType(String key, PropertyKind kind) {
        this.key = key;
        this.kind = kind;
    }

    public String key() {
        return key;
    }

    public PropertyKind kind() {
        return kind;
    }

    public T getProperty(DiscoveryProvider provider) {
        return (T) provider.getProperty(key);
    }

    public void setProperty(DiscoveryProvider provider, T value) {
        provider.getProperty(key).setValue(value);
    }

    @Override
    public String toString() {
        return key+"["+kind+"]"; //NOI18N
    }
}
