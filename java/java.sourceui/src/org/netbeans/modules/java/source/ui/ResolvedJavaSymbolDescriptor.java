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

import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.java.ui.Icons;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
final class ResolvedJavaSymbolDescriptor extends JavaSymbolDescriptorBase {

    private final String simpleName;
    private final String simpleNameSuffix;
    private final ElementHandle<?> me;
    private final ElementKind kind;
    private final Set<Modifier> modifiers;

    ResolvedJavaSymbolDescriptor (
            @NonNull final JavaSymbolDescriptorBase base,
            @NonNull final String simpleName,
            @NullAllowed final String simpleNameSuffix,
            @NullAllowed final String ownerName,
            @NonNull final ElementKind kind,
            @NonNull final Set<Modifier> modifiers,
            @NonNull final ElementHandle<?> me) {
        super(base, ownerName);
        assert simpleName != null;
        assert kind != null;
        assert modifiers != null;
        assert me != null;
        this.simpleName = simpleName;
        this.simpleNameSuffix = simpleNameSuffix;
        this.kind = kind;
        this.modifiers = modifiers;
        this.me = me;
    }

    @Override
    public Icon getIcon() {
        return Icons.getElementIcon(kind, modifiers);
    }

    @Override
    public String getSymbolName() {
        return simpleNameSuffix == null ?
                simpleName :
                simpleName + simpleNameSuffix;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public void open() {
        FileObject file = getFileObject();
        if (file != null) {
	    ClasspathInfo cpInfo = ClasspathInfo.create(file);

	    ElementOpen.open(cpInfo, me);
        }
    }
}
