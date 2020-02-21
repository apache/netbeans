/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.api.model;
import java.util.Collection;
import org.openide.filesystems.FileObject;

/**
 * Represents a source file
 */
public interface CsmFile extends CsmNamedElement, CsmScope, CsmValidable {

    /** Gets this file normalized absolute path */
    CharSequence getAbsolutePath();
    
    /** Gets file object */
    FileObject getFileObject();

    /** Gets the project, to which the file belongs*/
    CsmProject getProject();

    /** Gets this file text */
    CharSequence getText();

    /** Gets this file text */
    CharSequence getText(int start, int end);

    /** Sorted (by start offset) list of #include directives in the file */
    Collection<CsmInclude> getIncludes();

    /** Sorted (by start offset) list of #error directives in the file */
    Collection<CsmErrorDirective> getErrors();
    
    /**
     * Sorted (by start offset) list of declarations in the file
     * No order for declarations with same start offset
     */
    Collection<CsmOffsetableDeclaration> getDeclarations();
    
    /** Sorted (by start offset) list of #define directives in the file */
    Collection<CsmMacro> getMacros();
    
    /** 
     * Returns true if the file has been already parsed
     * (i.e. was parsed since last change),
     * otherwise false 
     */
    boolean isParsed();
    
    /*
     * Checks whether the file needs to be parsed,
     * if yes, scedules parsing this file.
     * If wait parameter is true, waits until this file is parsed.
     * If the file is already parsed, immediately returns.
     *
     * @param wait determines whether to wait until the file is parsed:
     * if true, waits, otherwise doesn't wait, just puts the given file
     * into parser queue
     */
    void scheduleParsing(boolean wait) throws InterruptedException;
    
    /** returns true if file is source file. */
    boolean isSourceFile();

    /** returns true if file is header file. */
    public boolean isHeaderFile();

    public FileType getFileType();
    
    public static enum FileType {

        UNDEFINED_FILE,
        SOURCE_FILE,
        SOURCE_C_FILE,
        SOURCE_CPP_FILE,
        SOURCE_FORTRAN_FILE,
        HEADER_FILE,
    };
}
