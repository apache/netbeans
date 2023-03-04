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
package org.netbeans.modules.profiler.spi.java;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.openide.filesystems.FileObject;

/**
 * An SPI for {@linkplain JavaProfilerSource} functionality providers
 * @author Jaroslav Bachorik
 */
public interface AbstractJavaProfilerSource {
    public static final AbstractJavaProfilerSource NULL = new AbstractJavaProfilerSource() {

        @Override
        public boolean isTest(FileObject fo) {
            return false;
        }

        @Override
        public boolean isApplet(FileObject fo) {
            return false;
        }

        @Override
        public SourceClassInfo getTopLevelClass(FileObject fo) {
            return null;
        }

        @Override
        public Set<SourceClassInfo> getClasses(FileObject fo) {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<SourceClassInfo> getMainClasses(FileObject fo) {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<SourceMethodInfo> getConstructors(FileObject fo) {
            return Collections.EMPTY_SET;
        }

        @Override
        public SourceClassInfo getEnclosingClass(FileObject fo, int position) {
            return null;
        }

        @Override
        public SourceMethodInfo getEnclosingMethod(FileObject fo, int position) {
            return null;
        }

        @Override
        public boolean isInstanceOf(FileObject fo, String[] classNames, boolean allRequired) {
            return false;
        }

        @Override
        public boolean isInstanceOf(FileObject fo, String className) {
            return false;
        }

        @Override
        public boolean hasAnnotation(FileObject fo, String[] annotationNames, boolean allRequired) {
            return false;
        }

        @Override
        public boolean hasAnnotation(FileObject fo, String annotation) {
            return false;
        }

        @Override
        public boolean isOffsetValid(FileObject fo, int offset) {
            return false;
        }

        @Override
        public SourceMethodInfo resolveMethodAtPosition(FileObject fo, int position) {
            return null;
        }

        @Override
        public SourceClassInfo resolveClassAtPosition(FileObject fo, int position, boolean resolveField) {
            return null;
        }
    };
    
    /**
     * @param fo The source file. Must not be NULL
     * @return Returns true if the source represents a junit tet
     */
    boolean isTest(FileObject fo);

    /**
     * @param fo The source file. Must not be NULL
     * @return Returns true if the source is a java applet
     */
    boolean isApplet(FileObject fo);

    /**
     * @param fo The source file. Must not be NULL
     * @return Returns {@linkplain ClassInfo} of a top level class
     */
    SourceClassInfo getTopLevelClass(FileObject fo);
    
    /**
     * Lists all top level classes contained in the source
     * @param fo The source file. Must not be NULL
     * @return Returns a set of {@linkplain ClassInfo} instances from a source
     */
    Set<SourceClassInfo> getClasses(FileObject fo);

    /**
     * Lists all main classes contained in the source
     * @param fo The source file. Must not be NULL
     * @return Returns a set of {@linkplain ClassInfo} instances from a source
     */
    Set<SourceClassInfo> getMainClasses(FileObject fo);
    
    /**
     * Lists all constructors contained in the source
     * @param fo The source file. Must not be NULL
     * @return Returns a set of {@linkplain MethodInfo} instances from the source
     */
    Set<SourceMethodInfo> getConstructors(FileObject fo);

    /**
     * Finds a class present on the given position in the source
     * @param fo The source file. Must not be NULL
     * @param position The position in the source
     * @return Returns a {@linkplain ClassInfo} for the class present on the given position
     */
    SourceClassInfo getEnclosingClass(FileObject fo, final int position);

    /**
     * Finds a method present on the given position in the source
     * @param fo The source file. Must not be NULL
     * @param position The position in the source
     * @return Returns a {@linkplain MethodInfo} for the method present on the given position
     */
    SourceMethodInfo getEnclosingMethod(FileObject fo, final int position);

    /**
     * Checks whether the source represents any or all of the provided superclasses/interfaces
     * @param fo The source file. Must not be NULL
     * @param classNames A list of required superclasses/interfaces
     * @param allRequired Require all(TRUE)/any(FALSE) provided superclasses/interfaces to match
     * @return Returns TRUE if the source represents any or all of the provided classes/interfaces
     */
    boolean isInstanceOf(FileObject fo, String[] classNames, boolean allRequired);

    /**
     * Checks whether the source represents the provided superclass/interface
     * @param fo The source file. Must not be NULL
     * @param className The required superclass/interface
     * @return Returns TRUE if the source represents the provided superclass/interface
     */
    boolean isInstanceOf(FileObject fo, String className);

    /**
     * Checks whether the source contains any/all provided annotations
     * @param fo The source file. Must not be NULL
     * @param annotationNames A list of required annotations
     * @param allRequired Require all(TRUE)/any(FALSE) provided annotations to match
     * @return Returns TRUE if the source contains any or all of the provided annotations
     */
    boolean hasAnnotation(FileObject fo, String[] annotationNames, boolean allRequired);

    /**
     * Checks whether the source contains the provided annotation
     * @param fo The source file. Must not be NULL
     * @param annotation The required annotation
     * @return Returns TRUE if the source contains the provided annotation
     */
    boolean hasAnnotation(FileObject fo, String annotation);

    /**
     * Is the given offset valid within a particular source
     * @param fo The source file. Must not be NULL
     * @param offset The offset to check
     * @return Returns TRUE if the offset is valid for the source
     */
    boolean isOffsetValid(FileObject fo, int offset);
    
    /**
     * Resolves a method at the given position<br/>
     * In order to resolve the method there must be the method definition or invocation
     * at the given position.
     * @param fo The source file. Must not be NULL
     * @param position The position to check for method definition or invocation
     * @return Returns the {@linkplain MethodInfo} for the method definition or invocation at the given position or NULL if there is none
     */
    SourceMethodInfo resolveMethodAtPosition(FileObject fo, int position);
    
    /**
     * Resolves a class at the given position<br/>
     * In order to resolve the class there must be the class definition or reference
     * at the given position.
     * @param fo The source file. Must not be NULL
     * @param position The position to check for class definition or reference
     * @param resolveField Should the class be resolved from a variable type too?
     * @return Returns the {@linkplain ClassInfo} for the class definition or reference at the given position or NULL if there is none
     */
    SourceClassInfo resolveClassAtPosition(FileObject fo, int position, boolean resolveField);
}
