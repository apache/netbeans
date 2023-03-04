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
package org.netbeans.modules.profiler.nbimpl.javac;

import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavacMethodInfo extends SourceMethodInfo {
    private ElementHandle<ExecutableElement> handle;
    
    public JavacMethodInfo(ExecutableElement method, CompilationController cc) {
        super(ElementUtilities.getBinaryName((TypeElement) method.getEnclosingElement()), method.getSimpleName().toString(), ElementUtilitiesEx.getBinaryName(method, cc), getVMMethodName(method), isExecutable(method), convertModifiers(method.getModifiers()));
        handle = ElementHandle.create(method);
    }
    
    public ExecutableElement resolve(CompilationController cc) {
        return handle.resolve(cc);
    }

    private static boolean isExecutable(ExecutableElement method) {
        if (method == null) {
            return false;
        }
        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.ABSTRACT) || modifiers.contains(Modifier.NATIVE)) {
            return false;
        }
        return true;
    }
    
    private static String getVMMethodName(ExecutableElement method) {
        // Constructor returns <init>
        // Static initializer returns <clinit>
        // Method returns its simple name
        return method.getSimpleName().toString();
    }

    @Override
    public String toString() {
        return getClassName() + "." + getName() + getSignature();
    }
    
    private static int convertModifiers(Set<Modifier> mods) {
        int modifiers = 0;
        if (mods.contains(Modifier.ABSTRACT)) {
            modifiers |= java.lang.reflect.Modifier.ABSTRACT;
        }
        if (mods.contains(Modifier.FINAL)) {
            modifiers |= java.lang.reflect.Modifier.FINAL;
        }
        if (mods.contains(Modifier.NATIVE)) {
            modifiers |= java.lang.reflect.Modifier.NATIVE;
        }
        if (mods.contains(Modifier.PRIVATE)) {
            modifiers |= java.lang.reflect.Modifier.PRIVATE;
        }
        if (mods.contains(Modifier.PROTECTED)) {
            modifiers |= java.lang.reflect.Modifier.PROTECTED;
        }
        if (mods.contains(Modifier.PUBLIC)) {
            modifiers |= java.lang.reflect.Modifier.PUBLIC;
        }
        if (mods.contains(Modifier.STATIC)) {
            modifiers |= java.lang.reflect.Modifier.STATIC;
        }
        if (mods.contains(Modifier.STRICTFP)) {
            modifiers |= java.lang.reflect.Modifier.STRICT;
        }
        if (mods.contains(Modifier.SYNCHRONIZED)) {
            modifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
        }
        if (mods.contains(Modifier.TRANSIENT)) {
            modifiers |= java.lang.reflect.Modifier.TRANSIENT;
        }
        if (mods.contains(Modifier.VOLATILE)) {
            modifiers |= java.lang.reflect.Modifier.VOLATILE;
        }
        return modifiers;
    }
}
