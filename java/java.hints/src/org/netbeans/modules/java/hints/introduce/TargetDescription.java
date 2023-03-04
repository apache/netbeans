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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.util.TreePath;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.hints.errors.Utilities;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
public final class TargetDescription {
    public final String displayName;
    public final ElementHandle<TypeElement> type;
    public final boolean allowForDuplicates;
    public final boolean anonymous;
    public final boolean iface;
    public final boolean canStatic;
    public final TreePathHandle pathHandle;

    private TargetDescription(String displayName, ElementHandle<TypeElement> type, TreePathHandle pathHandle, boolean allowForDuplicates, boolean anonymous, boolean iface, boolean canStatic) {
        this.displayName = displayName;
        this.type = type;
        this.allowForDuplicates = allowForDuplicates;
        this.anonymous = anonymous;
        this.iface = iface;
        this.canStatic = canStatic;
        this.pathHandle = pathHandle;
    }

    public static TargetDescription create(CompilationInfo info, TypeElement type, TreePath path, boolean allowForDuplicates, boolean iface) {
        boolean canStatic = true;
        if (iface) {
            // interface cannot have static methods
            canStatic = false;
        } else {
            if (type.getNestingKind() == NestingKind.ANONYMOUS || 
                type.getNestingKind() == NestingKind.LOCAL ||
                (type.getNestingKind() != NestingKind.TOP_LEVEL && !type.getModifiers().contains(Modifier.STATIC))) {
                canStatic = false;
            }
        }
        return new TargetDescription(Utilities.target2String(type), 
                ElementHandle.create(type), 
                TreePathHandle.create(path, info),
                allowForDuplicates, 
                type.getSimpleName().length() == 0, iface, canStatic);
    }

    public String toDebugString() {
        return type.getBinaryName() + ":" + (allowForDuplicates ? "D" : "") + (anonymous ? "A" : "");
    }
    
}
