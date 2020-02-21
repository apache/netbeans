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
