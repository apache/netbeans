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

package org.netbeans.modules.cnd.api.project;

import java.util.List;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

public interface NativeFileItem {
    public enum Language {
	C, CPP, FORTRAN, C_HEADER, OTHER
    }
    
    public enum LanguageFlavor {
    	UNKNOWN(0),
        C(1), C89(2), C99(3),
        CPP98(4), CPP11(8),
        F77(5), F90(6), F95(7),
        DEFAULT(9),
        C11(10), CPP14(11), CPP17(12),
        C17(13), C23(14),
        CPP20(15), CPP23(16);
        private final int flavor;
        
        private LanguageFlavor(int flavor) {
            this.flavor = flavor;
        }
        public int toExternal(){
            return flavor;
        }
        public static LanguageFlavor fromExternal(int i) {
            switch (i) {
                case 0: return UNKNOWN;
                case 1: return C;
                case 2: return C89;
                case 3: return C99;
                case 4: return CPP98;
                case 5: return F77;
                case 6: return F90;
                case 7: return F95;
                case 8: return CPP11;
                case 9: return DEFAULT;
                case 10: return C11;
                case 11: return CPP14;
                case 12: return CPP17;
                case 13: return C17;
                case 14: return C23;
                case 15: return CPP20;
                case 16: return CPP23;
                default: return UNKNOWN;
            }
        }
    }
    
    /**
     * Returns the native project this file item belongs to.
     * @return the native project
     */
    public NativeProject getNativeProject();
   
    /**
     * The absolute file path
     * @return absolute path
     */
    public String getAbsolutePath();
    
    /**
     * File name (with extension)
     * @return 
     */
    public String getName();

    /**
     * Returns the file object associated with this file item.
     * @return the file associated with this file item. There is no guarantee that the file actually exists.
     */
    public FileObject getFileObject();

    /**
     * Returns a list "Include Search Path" of compiler defined include paths used when parsing 'orpan' source files.
     * @return a list "Include Search Path" of compiler defined include paths.
     * A path is always an absolute path.
     */
    public List<IncludePath> getSystemIncludePaths();
    
    /**
     * Returns a list "Include Search Path" of user defined include paths used when parsing 'orpan' source files.
     * @return a list "Include Search Path" of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    public List<IncludePath> getUserIncludePaths();
    
    /**
     * Returns a list of system pre-included headers.
     * @return list of included files
     * A path is always an absolute path.
     */
    public List<FSPath> getSystemIncludeHeaders();

    /**
     * Returns a list of '-include file' options 
     * as if #include "file" appeared as the first line of the primary source file.
     * However, the first directory searched for file is the preprocessor's working directory 
     * instead of the directory containing the main source file. 
     * If not found there, it is searched for in the remainder of the #include "..." search chain as normal. 
     * @return list of included files
     */
    public List<FSPath> getIncludeFiles();
    
    /**
     * Returns a list "String" of compiler defined macro definitions used when compiling this file item.
     * @return a list "String" of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     * It looks like MACRO=VALUE
     */
    public List<String> getSystemMacroDefinitions();
    
    /**
     * Returns a list "String" of user defined macro definitions used when compiling this file item.
     * @return a list "String" of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     * It looks like MACRO=VALUE
     */
    public List<String> getUserMacroDefinitions();

    /**
     * Returns the language of the file. 
     * @return the language of the file
     */
    public Language getLanguage();
    
    /**
     * Returns the language flavor of the file or UNKNOWN if unknown.
     * @return the language flavor (or UNKNOWN) of the file
     */
    public LanguageFlavor getLanguageFlavor();
    
    /**
     * Returns true if file excluded from build.
     * @return true if file excluded from build.
     */
    public boolean isExcluded();
}
