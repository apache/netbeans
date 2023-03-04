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
package org.netbeans.modules.java.hints.spi.support;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.Exceptions;

/** Factory for creating fixes, which add @SuppressWarnings to given Element
 *
 * @author Petr Hrebejk
 */
public final class FixFactory {

    private static final Set<Kind> DECLARATION = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.METHOD, Kind.VARIABLE);
    
    private FixFactory() {}

    /** Creates a fix, which when invoked adds a set of modifiers to the existing ones
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toAdd set of Modifiers to add
     * @param text text displayed as a fix description
     */
    public static final Fix addModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toAdd, String text) {
        return org.netbeans.spi.java.hints.support.FixFactory.addModifiersFix(compilationInfo, treePath, toAdd, text);
    }

    /** Creates a fix, which when invoked removes a set of modifiers from the existing ones
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toRemove set of Modifiers to remove
     * @param text text displayed as a fix description
     */
    public static final Fix removeModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toRemove, String text) {
        return org.netbeans.spi.java.hints.support.FixFactory.removeModifiersFix(compilationInfo, treePath, toRemove, text);
    }

    /** Creates a fix, which when invoked changes the existing modifiers
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toAdd set of Modifiers to add
     * @param toRemove set of Modifiers to remove
     * @param text text displayed as a fix description
     */
    public static final Fix changeModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toAdd, Set<Modifier> toRemove, String text) {
        return org.netbeans.spi.java.hints.support.FixFactory.changeModifiersFix(compilationInfo, treePath, toAdd, toRemove, text);
    }

    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        declaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g.
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */
    public static Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {
        return JavaFixImpl.Accessor.INSTANCE.createSuppressWarningsFix(compilationInfo, treePath, keys);
    }

    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        declaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g. 
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element 
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */ 
    public static List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {
        return JavaFixImpl.Accessor.INSTANCE.createSuppressWarnings(compilationInfo, treePath, keys);
    }

    /**
     * Do not use.
     */
    @Deprecated
    public static boolean isSuppressWarningsFix(Fix f) {
        return false;
    }

}
