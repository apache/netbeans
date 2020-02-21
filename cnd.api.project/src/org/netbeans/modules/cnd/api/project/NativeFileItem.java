/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        C11(10), CPP14(11), CPP17(12);
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
