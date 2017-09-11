/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
