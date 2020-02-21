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

package org.netbeans.modules.cnd.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MIME names.
 * We need these both in the loaders code and in the editor code
 * so we have a common definition here.
*/
public final class MIMENames {

    private MIMENames() {
        // do not instantiate
    }

    /** Header */
    public static final String HEADER_MIME_TYPE = "text/x-h"; //NOI18N

    /** Preprocessor */
    public static final String PREPROC_MIME_TYPE = "text/x-cpp+preprocessor";// NOI18N

    /** any c/c++/header source file */
    public static final String SOURCES_MIME_TYPE = "text/x-cnd+sourcefile";// NOI18N
    
    /** Doxygen embedded */
    public static final String DOXYGEN_MIME_TYPE = "text/x-doxygen"; // NOI18N

    /** Double quoted string embedded */
    public static final String STRING_DOUBLE_MIME_TYPE = "text/x-cpp-string-double"; // NOI18N

    /** Single quoted string embedded */
    public static final String STRING_SINGLE_MIME_TYPE = "text/x-cpp-string-single"; // NOI18N

    /** C++ */
    public static final String CPLUSPLUS_MIME_TYPE = "text/x-c++"; //NOI18N

    /** C */
    public static final String C_MIME_TYPE = "text/x-c"; //NOI18N

    /** Fortran */
    public static final String FORTRAN_MIME_TYPE = "text/x-fortran"; //NOI18N

    /** Makefiles */
    public static final String MAKEFILE_MIME_TYPE = "text/x-make"; //NOI18N
    public static final String CMAKE_MIME_TYPE = "text/x-cmake"; //NOI18N
    public static final String CMAKE_INCLUDE_MIME_TYPE = "text/x-cmake-include"; //NOI18N
    public static final String QTPROJECT_MIME_TYPE = "text/x-qtproject"; //NOI18N

    /** Shell */
    public static final String SHELL_MIME_TYPE = "text/sh"; //NOI18N

    /** Windows batch file */
    public static final String BAT_MIME_TYPE = "text/bat"; //NOI18N


    /** Visu x designer */
    public static final String VISU_MIME_TYPE = "text/x-visu"; //NOI18N

    /** Lex files */
    public static final String LEX_MIME_TYPE = "text/x-lex"; //NOI18N

    /** Yacc files */
    public static final String YACC_MIME_TYPE = "text/x-yacc"; //NOI18N

    /** SPARC Assembly files */
    public static final String ASM_MIME_TYPE = "text/x-asm"; //NOI18N

    /** ELF Executable files */
    public static final String ELF_EXE_MIME_TYPE = "application/x-executable+elf"; //NOI18N

    /** Generic Executable files */
    public static final String EXE_MIME_TYPE = "application/x-exe"; //NOI18N
    
    /** ELF Core files */
    public static final String ELF_CORE_MIME_TYPE = "application/x-core+elf"; //NOI18N

    /** ELF Shared Object files */
    public static final String ELF_SHOBJ_MIME_TYPE = "application/x-shobj+elf"; //NOI18N

    /** ELF Static Object files */
    public static final String ELF_STOBJ_MIME_TYPE = "application/x-stobj+elf"; //NOI18N

    /** ELF Object files */
    public static final String ELF_OBJECT_MIME_TYPE = "application/x-object+elf"; //NOI18N

    /** Generic ELF files (shouldn't be recognized anymore) */
    public static final String ELF_GENERIC_MIME_TYPE = "application/x-elf"; //NOI18N

    // special mime type for C Headers extensions
    /*package*/ static final String C_HEADER_MIME_TYPE = "text/x-c/text/x-h"; // NOI18N

    /** Qt Form files */
    public static final String QT_UI_MIME_TYPE = "text/qtui+xml"; // NOI18N

    /** Qt Resource files */
    public static final String QT_RESOURCE_MIME_TYPE = "text/qtresource+xml"; // NOI18N

    /** Qt Translation files */
    public static final String QT_TRANSLATION_MIME_TYPE = "text/qttranslation+xml"; // NOI18N

    public static final Set<String> CND_TEXT_MIME_TYPES;
    public static final Set<String> CND_SOURCE_MIME_TYPES;
    public static final Set<String> CND_SCRIPT_MIME_TYPES;

    static {
        CND_SOURCE_MIME_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
                    HEADER_MIME_TYPE, C_HEADER_MIME_TYPE, CPLUSPLUS_MIME_TYPE, C_MIME_TYPE, FORTRAN_MIME_TYPE,ASM_MIME_TYPE)));
        
        CND_SCRIPT_MIME_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
                    MAKEFILE_MIME_TYPE, CMAKE_MIME_TYPE, CMAKE_INCLUDE_MIME_TYPE, QTPROJECT_MIME_TYPE, SHELL_MIME_TYPE, BAT_MIME_TYPE,
                    VISU_MIME_TYPE,
                    LEX_MIME_TYPE, YACC_MIME_TYPE,
                    QT_UI_MIME_TYPE, QT_RESOURCE_MIME_TYPE, QT_TRANSLATION_MIME_TYPE)));
        
        CND_TEXT_MIME_TYPES = CND_SOURCE_MIME_TYPES;
    }

    public static boolean isCndMimeType(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return CND_SOURCE_MIME_TYPES.contains(mime)
                || CND_SCRIPT_MIME_TYPES.contains(mime)
                || CND_TEXT_MIME_TYPES.contains(mime);
    }

    public static boolean isCppOrC(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return mime.equals(CPLUSPLUS_MIME_TYPE) || mime.equals(C_MIME_TYPE);
    }

    public static boolean isCppOrCOrFortran(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return mime.equals(CPLUSPLUS_MIME_TYPE) || mime.equals(C_MIME_TYPE)  || mime.equals(FORTRAN_MIME_TYPE);
    }
    
    public static boolean isHeader(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return mime.equals(HEADER_MIME_TYPE) || mime.equals(C_HEADER_MIME_TYPE);
    }

    public static boolean isHeaderOrCpp(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return mime.equals(CPLUSPLUS_MIME_TYPE) || mime.equals(HEADER_MIME_TYPE) || mime.equals(C_HEADER_MIME_TYPE);
    }

    public static boolean isHeaderOrCppOrC(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return mime.equals(CPLUSPLUS_MIME_TYPE) || mime.equals(C_MIME_TYPE) || mime.equals(HEADER_MIME_TYPE) || mime.equals(C_HEADER_MIME_TYPE);
    }

    public static boolean isFortranOrHeaderOrCppOrC(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return mime.equals(CPLUSPLUS_MIME_TYPE) || mime.equals(C_MIME_TYPE) || mime.equals(HEADER_MIME_TYPE) || mime.equals(C_HEADER_MIME_TYPE) || mime.equals(FORTRAN_MIME_TYPE);
    }

    public static boolean isBinary(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return  mime.equals(EXE_MIME_TYPE) ||
                mime.equals(ELF_EXE_MIME_TYPE) ||
                mime.equals(ELF_CORE_MIME_TYPE) ||
                mime.equals(ELF_SHOBJ_MIME_TYPE) ||
                mime.equals(ELF_STOBJ_MIME_TYPE) ||
                mime.equals(ELF_GENERIC_MIME_TYPE) ||
                mime.equals(ELF_OBJECT_MIME_TYPE);
    }

    public static boolean isBinaryExecutable(String mime) {
        if (mime == null || mime.length() == 0) {
            return false;
        }
        return  mime.equals(EXE_MIME_TYPE) || mime.equals(ELF_EXE_MIME_TYPE);
    }
}
