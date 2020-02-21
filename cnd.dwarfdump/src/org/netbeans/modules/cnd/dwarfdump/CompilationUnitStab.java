/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdump;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;

/**
 *
 */
public class CompilationUnitStab implements CompilationUnitInterface {
    private final String sourceName;
    private final String compileLine;
    private final String compileDir;
    private final String objectFile;
    private final boolean hasMain;
    private final int mainLine;
    private final int desc;

    public CompilationUnitStab(String sourceName, String line, String objectFile, boolean hasMain, int mainLine, int desc) {
        this.sourceName = sourceName;
        int i = line.indexOf(';');
        if (i > 0) {
            compileDir = line.substring(0,i).trim();
            compileLine = line.substring(i+1).trim();
        } else {
            compileDir = line;
            compileLine = line;
        }
        this.objectFile = objectFile;
        this.hasMain = hasMain;
        this.mainLine = mainLine;
        this.desc = desc;
    }

    public String getCompilationDir() throws IOException {
        return compileDir;
    }

    public String getSourceFileName() throws IOException {
        return sourceName;
    }

    public String getCommandLine() throws IOException {
        return compileLine;
    }

    public String getObjectFile() throws IOException {
        return objectFile;
    }

    public boolean hasMain() throws IOException {
        return hasMain;
    }

    public int getMainLine() throws IOException {
        return mainLine;
    }

    public String getSourceFileAbsolutePath() throws IOException {
        String result;

        String dir = getCompilationDir();
        String name = getSourceFileName();
        if (dir != null) {
            if (isAbsolute(name)) {
                result = name;
            } else {
                if (dir.endsWith("/") || dir.endsWith("\\")) { // NOI18N
                    result = dir+name;
                } else {
                    result = dir+ File.separator + name;
                }
            }
        } else {
            result = name;
        }

        return result;
    }

    private boolean isAbsolute(String path) {
        if (path.startsWith("/") || path.length() > 2 && path.charAt(1) == ':') { // NOI18N
            return true;
        }
        return false;
    }

    public String getSourceLanguage() throws IOException {
        switch (desc) {
            case 1: /* Assembler */
                return null;
            case 2: /* C */
                return LANG.DW_LANG_C.toString();
            case 3: /* ANSI C */
                return LANG.DW_LANG_C89.toString();
            case 4: /* C++ */
                return LANG.DW_LANG_C_plus_plus.toString();
            case 5: /* Fortran 77 */
                return LANG.DW_LANG_Fortran77.toString();
            case 6: /* Pascal */
                return LANG.DW_LANG_Pascal83.toString();
            case 7: /* Fortran 90 */
                return LANG.DW_LANG_Fortran90.toString();
            case 8: /* Java */
                return LANG.DW_LANG_Java.toString();
            case 9: /* C99 */
                return LANG.DW_LANG_C99.toString();
        }
        return null;
    }

    public void dump(PrintStream out) throws IOException {
        out.println("*** " + getSourceFileAbsolutePath() + " ***"); // NOI18N
        out.println("    Source Name:  " + sourceName); // NOI18N
        out.println("    Compile Dir:  " + compileDir); // NOI18N
        out.println("    Compile Line: " + compileLine); // NOI18N
        out.println("    Object File:  " + objectFile); // NOI18N
        out.println("    Has Main:     " + hasMain); // NOI18N
        out.println();
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out);
            return st.toString("UTF-8"); //NOI18N
        } catch (IOException ex) {
            return ""; // NOI18N
        }
    }
}
