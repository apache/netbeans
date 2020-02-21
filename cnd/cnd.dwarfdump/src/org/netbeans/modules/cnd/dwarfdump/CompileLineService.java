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

import org.netbeans.modules.cnd.dwarfdump.source.SourceFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 * Light weight service that gets command line from binary file in case Sun Studio compiler
 *
 */
public class CompileLineService {
    private static final String COMPILE_DIRECTORY = "\"directory\": "; //NOI18N
    private static final String SOURCE_FILE = "\"file\": "; //NOI18N
    private static final String COMMAND_LINE = "\"command\": "; //NOI18N
    private static final String SOURCE_PATH = "\"path\": "; //NOI18N
    private static final String LANGUAGE = "\"language\": "; //NOI18N
    private static final String MAIN = "\"main\": "; //NOI18N
    private static final String MAIN_LINE = "\"line\": "; //NOI18N
    private static final String DWARF_DUMP = "\"dwarf\": "; //NOI18N

    private CompileLineService() {
    }

    public static void main(String[] args){
        if (args.length < 2) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.CompileLineService -file binaryFileName [-dwarf]"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.CompileLineService -folder folderName [-dwarf]"); // NOI18N
            return;
        }
        try {
            if (args.length == 3 &&  "-dwarf".equals(args[2])) { // NOI18N
                dump(args[0], args[1], true, System.out);
            } else {
                dump(args[0], args[1], false, System.out);
            }
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.INFO, "File "+args[1], ex); // NOI18N
        }
    }

    private static void dump(String kind, String objFileName, boolean dwarf, PrintStream out) throws IOException, Exception {
        List<SourceFile> res = null;
        if ("-file".equals(kind)){ // NOI18N
            res = getSourceFileProperties(objFileName, dwarf);
        } else if ("-folder".equals(kind)){ // NOI18N
            res = getSourceFolderProperties(objFileName, dwarf);
        } else {
            throw new Exception("Wrong arguments: "+kind+" "+objFileName); // NOI18N
        }
        out.println("["); // NOI18N
        boolean first = true;
        for(SourceFile entry : res) {
            if (!first) {
                out.println(","); // NOI18N
                
            }
            out.println("{"); // NOI18N
            boolean finished = true;
            finished = printLine(out, COMPILE_DIRECTORY, entry.getCompilationDir(), finished);
            finished = printLine(out, SOURCE_FILE, entry.getSourceFileName(), finished);
            finished = printLine(out, COMMAND_LINE, entry.getCommandLine(), finished);
            finished = printLine(out, SOURCE_PATH, entry.getSourceFileAbsolutePath(), finished);
            finished = printLine(out, LANGUAGE, entry.getSourceLanguage(), finished);
            finished = printLine(out, MAIN, entry.hasMain(), finished);
            finished = printLine(out, MAIN_LINE, entry.getMainLine(), finished);
            finished = printLine(out, DWARF_DUMP, entry.getDwarfDump(), finished);
            out.println(""); // NOI18N
            out.print("}"); // NOI18N
            first = false;
        }
        if (!first) {
            out.println(""); // NOI18N
        }
        out.println("]"); // NOI18N
    }

    private static boolean printLine(PrintStream out, String key, String value, boolean finished) {
        if (value != null && value.length() > 0) {
            if (!finished) {
                out.println(","); // NOI18N
            }
            out.print(" "); // NOI18N
            out.print(key);
            out.print("\""); // NOI18N
            out.print(value);
            out.print("\""); // NOI18N
            finished = false;
        }
        return finished;
    }

    private static boolean printLine(PrintStream out, String key, boolean value, boolean finished) {
        if (value) {
            if (!finished) {
                out.println(","); // NOI18N
                finished = false;
            }
            out.print(" "); // NOI18N
            out.print(key);
            out.print("true"); // NOI18N
        }
        return finished;
    }

    private static boolean printLine(PrintStream out, String key, int value, boolean finished) {
        if (value != 0) {
            if (!finished) {
                out.println(","); // NOI18N
                finished = false;
            }
            out.print(" "); // NOI18N
            out.print(key);
            out.print(""+value); // NOI18N
        }
        return finished;
    }
    
    public static List<SourceFile> getSourceProperties(BufferedReader out) throws IOException {
        return readSourceProperties(out);
    }

    private static List<SourceFile> readSourceProperties(BufferedReader out) throws IOException {
        List<SourceFile> list = new ArrayList<SourceFile>();
        String line;
        String compileDir = null;
        String sourceFile = null;
        String compileLine = null;
        String absolutePath = null;
        String sourceLanguage = null;
        boolean hasMain = false;
        int lineNumber = 0;
        String dwarf = null;
        while ((line=out.readLine())!= null){
            line = line.trim();
            if (line.startsWith("[")) { // NOI18N
                // start output
                continue;
            }
            if (line.startsWith("]")) { // NOI18N
                // end output
                continue;
            }
            if (line.startsWith("{")) { // NOI18N
                // start item
                compileDir = null;
                sourceFile = null;
                compileLine = null;
                absolutePath = null;
                sourceLanguage = null;
                hasMain = false;
                lineNumber = 0;
                dwarf = null;
                continue;
            }
            if (line.startsWith("}")) { // NOI18N
                final SourceFile src = SourceFile.createSourceFile(compileDir, sourceFile, compileLine, absolutePath, sourceLanguage, hasMain, lineNumber, dwarf);
                list.add(src);
                continue;
            }
            if (line.startsWith(COMPILE_DIRECTORY)) {
                compileDir = removeQuotesAndComma(line.substring(COMPILE_DIRECTORY.length()));
                continue;
            }
            if (line.startsWith(SOURCE_FILE)) {
                sourceFile = removeQuotesAndComma(line.substring(SOURCE_FILE.length()));
                continue;
            }
            if (line.startsWith(COMMAND_LINE)) {
                compileLine = removeQuotesAndComma(line.substring(COMMAND_LINE.length()));
                continue;
            }
            if (line.startsWith(SOURCE_PATH)) {
                absolutePath = removeQuotesAndComma(line.substring(SOURCE_PATH.length()));
                continue;
            }
            if (line.startsWith(LANGUAGE)) {
                sourceLanguage = removeQuotesAndComma(line.substring(LANGUAGE.length()));
                continue;
            }
            if (line.startsWith(MAIN)) {
                hasMain = "true".equals(removeQuotesAndComma(line.substring(MAIN.length()))); // NOI18N
                continue;
            }
            if (line.startsWith(MAIN_LINE)) {
                try {
                    lineNumber = Integer.parseInt(removeQuotesAndComma(line.substring(MAIN_LINE.length())));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace(System.err);
                }
                continue;
            }
            if (line.startsWith(DWARF_DUMP)) {
                dwarf = removeQuotesAndComma(line.substring(DWARF_DUMP.length()));
                continue;
            }
        }
        return list;
    }
    
    private static String removeQuotesAndComma(String str) {
        str = str.trim();
        if (str.endsWith(",")) { // NOI18N
            str = str.substring(0, str.length() - 1);
        }
        if (str.length() >= 2 && (str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'' || // NOI18N
            str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"')) {// NOI18N
            str = str.substring(1, str.length() - 1); // NOI18N
        }
        return str;
    }

    // valid on Solaris or Linux
    public static List<SourceFile> getSourceFolderProperties(String objFolderName, boolean dwarf) {
        List<SourceFile> list = new ArrayList<SourceFile>();
        for(String objFileName : getObjectFiles(objFolderName)) {
            list.addAll(getSourceFileProperties(objFileName, dwarf));
        }
        return list;
    }

    public static List<SourceFile> getSourceFileProperties(String objFileName, boolean dwarf) {
        List<SourceFile> list = new ArrayList<SourceFile>();
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            Dwarf.CompilationUnitIterator iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnitInterface cu = iterator.next();
                if (cu != null) {
                    if (cu.getSourceFileName() == null) {
                        if (Dwarf.LOG.isLoggable(Level.FINE)) {
                            Dwarf.LOG.log(Level.FINE, "Compilation unit has broken name in file {0}", objFileName);  // NOI18N
                        }
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        if (Dwarf.LOG.isLoggable(Level.FINE)) {
                            Dwarf.LOG.log(Level.FINE, "Compilation unit has unresolved language in file {0}for {1}", new Object[]{objFileName, cu.getSourceFileName()});  // NOI18N
                        }
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang)
                            || LANG.DW_LANG_C89.toString().equals(lang)
                            || LANG.DW_LANG_C99.toString().equals(lang)
                            || LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        try {
                            list.add(SourceFile.createSourceFile(cu, dwarf));
                        } catch (IOException ex){
                            throw ex;
                        } catch (Exception ex){
                            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                                Dwarf.LOG.log(Level.FINE, "Compilation unit {0} {1}", new Object[]{cu.getSourceFileName(), ex.getMessage()});  // NOI18N
                            }
                            continue;
                        }
                    } else {
                        if (Dwarf.LOG.isLoggable(Level.FINE)) {
                            Dwarf.LOG.log(Level.FINE, "Unknown language: {0}", lang);  // NOI18N
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "File not found {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "Unsuported format of file {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    public static SourceFile createSourceFile(String compileDir, String sourceFile, String compileLine) {
        return SourceFile.createSourceFile(compileDir, sourceFile, compileLine, null, null, false, -1, null);
    }

    private static Set<String> getObjectFiles(String root){
        HashSet<String> map = new HashSet<String>();
        gatherSubFolders(new File(root), map, new HashSet<String>());
        return map;
    }

    private static boolean isExecutable(File file){
        String name = file.getName();
        return name.indexOf('.') < 0;
    }

    private static void gatherSubFolders(File d, HashSet<String> map, HashSet<String> antiLoop){
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (ignoreFolder(d)){
                return;
            }
            String canPath;
            try {
                canPath = d.getCanonicalPath();
            } catch (IOException ex) {
                Dwarf.LOG.log(Level.INFO, "File "+d.getAbsolutePath(), ex);
                return;
            }
            if (!antiLoop.contains(canPath)){
                antiLoop.add(canPath);
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isDirectory()) {
                            gatherSubFolders(ff[i], map, antiLoop);
                        } else if (ff[i].isFile()) {
                            String name = ff[i].getName();
                            if (name.endsWith(".o") ||  // NOI18N
                                name.endsWith(".so") || // NOI18N
                                name.endsWith(".a") ||  // NOI18N
                                isExecutable(ff[i])){
                                String path = ff[i].getAbsolutePath();
                                map.add(path);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean ignoreFolder(File file){
        if (file.isDirectory()) {
            String name = file.getName();
            return name.equals("SCCS") || name.equals("CVS") || name.equals(".hg") || name.equals("SunWS_cache") || name.equals(".svn"); // NOI18N
        }
        return false;
    }
}
