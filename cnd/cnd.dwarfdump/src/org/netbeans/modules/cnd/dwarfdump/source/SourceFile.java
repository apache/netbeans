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
package org.netbeans.modules.cnd.dwarfdump.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.section.FileEntry;

/**
 *
 */
public final class SourceFile implements CompilationUnitInterface {
    private final String compileLine;
    private final String compileDir;
    private final String sourceFile;
    private String dwarfDump;
    private Map<String, String> userMacros;
    private List<String> userUndefs;
    private List<String> userPaths;
    private List<String> userIncludes;
    private final String absolutePath;
    private final String sourceLanguage;
    private final boolean hasMain;
    private final int mainLine;

    public static SourceFile createSourceFile(String compileDir, String sourceFile, String compileLine) {
        return new SourceFile(compileDir, sourceFile, compileLine, null, null, false, -1, null);
    }

    public static SourceFile createSourceFile(String compileDir, String sourceFile, String compileLine, String absolutePath, String sourceLanguage, boolean hasMain, int mainLine, String dwarf) {
        return new SourceFile(compileDir, sourceFile, compileLine, absolutePath, sourceLanguage, hasMain, mainLine, dwarf);
    }

    public static SourceFile createSourceFile(CompilationUnitInterface cu, boolean dwarf) throws IOException, Exception {
        SourceFile res = new SourceFile(cu);
        if (res.compileLine.length() == 0 && (cu instanceof CompilationUnit) && dwarf) {
            CompilationUnit dcu = (CompilationUnit)cu;
            StringBuilder buf = new StringBuilder();
            DwarfStatementList dwarfStatementTable = dcu.getStatementList();
            if (dwarfStatementTable != null) {
                for(String dir : dwarfStatementTable.includeDirs) {
                    buf.append(" -d").append("'").append(dir).append("'"); // NOI18N
                }
                for(FileEntry fileEntry : dwarfStatementTable.fileEntries) {
                    buf.append(" -f:").append(""+fileEntry.dirIndex).append(":'").append(fileEntry.fileName).append("'"); // NOI18N
                }
            }
            DwarfMacinfoTable dwarfMacroTable = dcu.getMacrosTable();
            if (dwarfMacroTable != null) {
                List<DwarfMacinfoEntry> table = dwarfMacroTable.getCommandLineMarcos();
                for (Iterator<DwarfMacinfoEntry> it = table.iterator(); it.hasNext();) {
                    DwarfMacinfoEntry entry = it.next();
                    if ((entry.type == MACINFO.DW_MACINFO_define ||
                         entry.type == MACINFO.DW_MACRO_define_indirect) &&
                         entry.definition != null) {
                        String def = entry.definition;
                        int i = def.indexOf(' ');
                        if (i>0){
                            buf.append(" -D").append(def.substring(0,i)).append("='").append(def.substring(i+1).trim()).append("'"); // NOI18N
                        } else {
                            buf.append(" -D").append(def.substring(0,i)); // NOI18N
                        }
                    } else if ((entry.type == MACINFO.DW_MACINFO_undef ||
                         entry.type == MACINFO.DW_MACRO_undef_indirect) &&
                         entry.definition != null) {
                        buf.append(" -U").append(entry.definition); // NOI18N
                    }
                }
                if (dwarfStatementTable != null) {
                    List<Integer> commandLineIncludedFiles = dwarfMacroTable.getCommandLineIncludedFiles();
                    for(int i : commandLineIncludedFiles) {
                        String includedSource = dwarfStatementTable.getFilePath(i);
                        if (includedSource.startsWith("./")) { // NOI18N
                            includedSource = res.compileDir+includedSource.substring(1);
                        }
                        if (!res.absolutePath.equals(includedSource)) {
                            buf.append(" -include").append("'").append(includedSource).append("'"); // NOI18N
                        }
                    }
                }
            }
            res.dwarfDump = buf.toString().trim();
        }
        return res;
    }

    private SourceFile(CompilationUnitInterface cu) throws IOException, Exception {
        String s = cu.getCommandLine();
        if (s == null) {
            // client may be interested in compilation units also
            s = ""; // NOI18N
            //throw new Exception("Dwarf information does not contain compile line");  // NOI18N
        }
        compileLine = s.trim();
        compileDir = cu.getCompilationDir();
        sourceFile = cu.getSourceFileName();
        if (sourceFile == null) {
            throw new Exception("Dwarf information does not contain source file name"); // NOI18N
        }
        absolutePath = cu.getSourceFileAbsolutePath();
        sourceLanguage = cu.getSourceLanguage();
        hasMain = cu.hasMain();
        mainLine = cu.getMainLine();
    }

    private SourceFile(String compileDir, String sourceFile, String compileLine, String absolutePath, String sourceLanguage, boolean hasMain, int mainLine, String dwarf) {
        this.compileLine = compileLine == null ? "" : compileLine;
        this.compileDir = compileDir;
        this.sourceFile = sourceFile;
        this.absolutePath = absolutePath;
        this.sourceLanguage = sourceLanguage;
        this.hasMain = hasMain;
        this.mainLine = mainLine;
        this.dwarfDump = dwarf;
    }

    public final String getCompilationDir() {
        return compileDir;
    }

    public final String getSourceFileName() {
        return sourceFile;
    }

    public final String getCommandLine() {
        return compileLine;
    }

    public String getSourceFileAbsolutePath() {
        return absolutePath;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public boolean hasMain() {
        return hasMain;
    }

    public int getMainLine() {
        return mainLine;
    }

    public final String getDwarfDump() {
        return dwarfDump;
    }

    public final Map<String, String> getUserMacros() {
        if (userMacros == null) {
            initMacrosAndPaths();
        }
        return userMacros;
    }

    public final List<String> getUndefs() {
        if (userUndefs == null) {
            initMacrosAndPaths();
        }
        return userUndefs;
    }

    public final List<String> getUserPaths() {
        if (userPaths == null) {
            initMacrosAndPaths();
        }
        return userPaths;
    }

    public final List<String> getIncludeFiles() {
        if (userIncludes == null) {
            initMacrosAndPaths();
        }
        return userIncludes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceFile other = (SourceFile) obj;
        if ((this.compileLine == null) ? (other.compileLine != null) : !this.compileLine.equals(other.compileLine)) {
            return false;
        }
        if ((this.compileDir == null) ? (other.compileDir != null) : !this.compileDir.equals(other.compileDir)) {
            return false;
        }
        if ((this.sourceFile == null) ? (other.sourceFile != null) : !this.sourceFile.equals(other.sourceFile)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.compileLine != null ? this.compileLine.hashCode() : 0);
        hash = 97 * hash + (this.compileDir != null ? this.compileDir.hashCode() : 0);
        hash = 97 * hash + (this.sourceFile != null ? this.sourceFile.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "SourceFile{" + "compileLine=" + compileLine + ", compileDir=" + compileDir + ", sourceFile=" + sourceFile + '}'; // NOI18N
    }

    private void initMacrosAndPaths() {
        ListIterator<String> st = null;
        DefaultDriver driver = new DefaultDriver();
        if (compileLine.length() > 0) {
            st = driver.splitCommandLine(compileLine, CompileLineOrigin.DwarfCompileLine).listIterator();
        } else if (dwarfDump != null && dwarfDump.length() > 0) {
            st = driver.splitCommandLine(dwarfDump, CompileLineOrigin.DwarfCompileLine).listIterator();
        } else {
            userPaths = new ArrayList<String>();
            userIncludes = new ArrayList<String>();
            userMacros = new LinkedHashMap<String, String>();
            userUndefs = new ArrayList<String>();
            return;
        }
        Artifacts res = driver.gatherCompilerLine(st, CompileLineOrigin.BuildLog, LANG.DW_LANG_C_plus_plus.toString().equals(sourceLanguage));
        userPaths = new ArrayList<String>(res.getUserIncludes());
        userIncludes = new ArrayList<String>(res.getUserFiles());
        userMacros = new LinkedHashMap<String, String>(res.getUserMacros());
        userUndefs = new ArrayList<String>(res.getUserUndefinedMacros());
    }
}
