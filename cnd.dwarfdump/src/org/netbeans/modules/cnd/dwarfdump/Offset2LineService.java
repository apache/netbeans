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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;

/**
 *
 */
public class Offset2LineService {
    private static final boolean TRACE = false;
    private Map<String, String> onePath;

    private Offset2LineService() {
    }

    public static void main(String[] args){
        if (args.length < 1) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.Offset2LineService binaryFileName"); // NOI18N
            return;
        }
        try {
            dump(args[0], System.out);
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + args[0], ex);  // NOI18N
        }
    }

    private static void dump(String executable, PrintStream out) throws IOException {
        Map<String, AbstractFunctionToLine> res = getOffset2Line(executable);
        for(Map.Entry<String, AbstractFunctionToLine> entry : res.entrySet()) {
            out.println(entry.getKey());
            entry.getValue().dump(out);
        }
    }

    public static Map<String, AbstractFunctionToLine> getOffset2Line(BufferedReader out) throws IOException {
        return new Offset2LineService().readOffset2Line(out);
    }

    public static Map<String, AbstractFunctionToLine> getOffset2Line(String executable) throws IOException {
        return new Offset2LineService().getSourceInfo(executable);
    }


    private Map<String, AbstractFunctionToLine> readOffset2Line(BufferedReader out) throws IOException {
        onePath = new HashMap<String, String>();
        Map<String, AbstractFunctionToLine> sourceInfoMap = new HashMap<String, AbstractFunctionToLine>();
        String line;
        int state = 0;
        String functionName = null;
        String fileName = null;
        int baseLine = 0;
        List<Integer> lines = new ArrayList<Integer>();
        List<Integer> startOffset = new ArrayList<Integer>();
        List<Integer> endOffset = new ArrayList<Integer>();
        while ((line=out.readLine())!= null){
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            switch (state) {
                case 0:
                    // read function name
                    functionName = line;
                    state++;
                    break;
                case 1:
                    // read file name
                    fileName = line;
                    state++;
                    break;
                case 2:
                    // read base name
                    try {
                        baseLine = Integer.parseInt(line);
                        lines.clear();
                        startOffset.clear();
                        endOffset.clear();
                        state++;
                    } catch (NumberFormatException ex) {
                        state = 0;
                    }
                    break;
                case 3:
                    char c = line.charAt(0);
                    if (c >= '0' && c <= '9') {
                        // line-offset table
                        String[] split = line.split(","); // NOI18N
                        if (split.length > 2) {
                            try {
                                lines.add(Integer.valueOf(split[0]));
                                startOffset.add(Integer.valueOf(split[1]));
                                endOffset.add(Integer.valueOf(split[2]));
                            } catch (NumberFormatException ex) {
                                state = 0;
                            }
                        }
                    } else {
                        // end of table
                        sourceInfoMap.put(functionName, createAbstractFunctionToLine(fileName, baseLine, lines, startOffset, endOffset));
                        functionName = line;
                        state = 1;
                    }
                    break;
            }
        }
        if (state > 1) {
            sourceInfoMap.put(functionName, createAbstractFunctionToLine(fileName, baseLine, lines, startOffset, endOffset));
        }
        onePath = null;
        return sourceInfoMap;
    }

    private AbstractFunctionToLine createAbstractFunctionToLine(String fileName, int baseLine,
            List<Integer> lines, List<Integer> startOffsets, List<Integer> endOffsets) {
        if (lines.isEmpty()) {
            return new DeclarationToLine(fileName, baseLine, onePath);
        } else {
            return new FunctionToLine(fileName, baseLine, lines, startOffsets, endOffsets, onePath);
        }
    }

    private Map<String, AbstractFunctionToLine> getSourceInfo(String executable) throws IOException {
        onePath = new HashMap<String, String>();
        Map<String, AbstractFunctionToLine> sourceInfoMap = new HashMap<String, AbstractFunctionToLine>();
        if (TRACE) {
            System.err.println("Process file: "+executable); // NOI18N
        }
        Dwarf dwarf = new Dwarf(executable);
        try {
            Dwarf.CompilationUnitIterator iterator = dwarf.iteratorCompilationUnits();
            while(iterator.hasNext()) {
                CompilationUnitInterface compilationUnit = iterator.next();
                if (compilationUnit instanceof CompilationUnit) {
                    CompilationUnit cu = (CompilationUnit) compilationUnit;
                    TreeSet<LineNumber> lineNumbers = getCompilationUnitLines(cu);
                    String filePath = cu.getSourceFileAbsolutePath();
                    String compDir = cu.getCompilationDir();
                    Set<Long> antiLoop = new HashSet<Long>();
                    processEntries(cu, cu.getDeclarations(false), filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
                }
            }
        } finally {
            dwarf.dispose();
        }
        onePath = null;
        return sourceInfoMap;
    }

    private void processEntries(CompilationUnit compilationUnit, List<DwarfEntry> declarations, String filePath, String compDir,
            TreeSet<LineNumber> lineNumbers, Map<String, AbstractFunctionToLine> sourceInfoMap, Set<Long> antiLoop) throws IOException {
        for (DwarfEntry entry : declarations) {
            prosessEntry(compilationUnit, entry, filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
        }
    }

    private void prosessEntry(CompilationUnit compilationUnit, DwarfEntry entry, String filePath, String compDir,
            TreeSet<LineNumber> lineNumbers, Map<String, AbstractFunctionToLine> sourceInfoMap, Set<Long> antiLoop) throws IOException {
        if (antiLoop.contains(entry.getRefference())) {
            return;
        }
        antiLoop.add(entry.getRefference());
        switch (entry.getKind()) {
            case DW_TAG_subprogram:
            {
                if (entry.getLine() < 0 || entry.getDeclarationFilePath() == null) {
                    return;
                }
                if (entry.getLowAddress() == 0) {
                    DeclarationToLine functionToLine = new DeclarationToLine(filePath, compDir, entry, onePath);
                    sourceInfoMap.put(entry.getQualifiedName(), functionToLine);
                    if (TRACE) {
                        System.err.println("Function: "+entry.getQualifiedName()); // NOI18N
                        System.err.println(functionToLine);
                    }
                } else {
                    FunctionToLine functionToLine = new FunctionToLine(filePath, compDir, entry, lineNumbers, onePath);
                    sourceInfoMap.put(entry.getQualifiedName(), functionToLine);
                    if (TRACE) {
                        System.err.println("Function: "+entry.getQualifiedName()); // NOI18N
                        System.err.println(functionToLine);
                    }
                }
                break;
            }
            case DW_TAG_structure_type:
            case DW_TAG_class_type:
                processEntries(compilationUnit, entry.getChildren(), filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
                break;
            case DW_TAG_typedef:
            case DW_TAG_const_type:
            case DW_TAG_pointer_type:
            case DW_TAG_reference_type:
            case DW_TAG_array_type:
            case DW_TAG_ptr_to_member_type:
            {
                DwarfEntry type = compilationUnit.getReferencedType(entry);
                if (type != null) {
                    prosessEntry(compilationUnit, type, filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
                }
                break;
            }
        }
    }

    private static TreeSet<LineNumber> getCompilationUnitLines(CompilationUnit unit) throws IOException{
        Set<LineNumber> numbers = unit.getLineNumbers();
        return new TreeSet<LineNumber>(numbers);
    }

    public static final class SourceLineInfo {
        private final CharSequence fileName;
        private final int lineNumber;
        public SourceLineInfo(CharSequence fileName, int lineNumber) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public String getFileName() {
            return fileName.toString();
        }

        public int getLine() {
            return lineNumber;
        }

        @Override
        public String toString() {
            return fileName.toString() + ':' + lineNumber;
        }
    }

    public static abstract class AbstractFunctionToLine {
        protected final int baseLine;
        protected final String filePath;
        
        protected AbstractFunctionToLine(int baseLine, String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException {
            this.baseLine = baseLine;
            this.filePath = initPath(filePath, compDir, entry, onePath);
        }

        protected AbstractFunctionToLine(int baseLine, String filePath, Map<String, String> onePath) {
            this.baseLine = baseLine;
            this.filePath = getPath(filePath, onePath);
        }
        
        public abstract SourceLineInfo getLine(int offset);

        protected abstract void dump(PrintStream out);

        private String initPath(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException{
            String res = _initPath(filePath, compDir, entry);
            return getPath(res, onePath);
        }

        private String getPath(String path, Map<String, String> onePath) {
            String cached = onePath.get(path);
            if (cached == null) {
                onePath.put(path, path);
                cached = path;
            }
            return cached;
        }

        private String _initPath(String filePath, String compDir, DwarfEntry entry) throws IOException{
            String entyFilePath = entry.getDeclarationFilePath();
            if (entyFilePath != null && filePath.endsWith(entyFilePath)) {
                return filePath;
            } else {
                if (entyFilePath != null &&
                        (entyFilePath.startsWith("/") || // NOI18N
                         entyFilePath.length()>2 && entyFilePath.charAt(1) == ':')){ // NOI18N
                    return entyFilePath;
                } else {
                    if (compDir.endsWith("/") || compDir.endsWith("\\")) { // NOI18N
                        return compDir+entyFilePath;
                    } else {
                        return compDir+"/"+entyFilePath; // NOI18N
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.baseLine;
            hash = 29 * hash + (this.filePath != null ? this.filePath.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof AbstractFunctionToLine)) {
                return false;
            }
            final AbstractFunctionToLine other = (AbstractFunctionToLine) obj;
            if (this.baseLine != other.baseLine) {
                return false;
            }
            if ((this.filePath == null) ? (other.filePath != null) : !this.filePath.equals(other.filePath)) {
                return false;
            }
            return true;
        }
        
    }

    private static final class DeclarationToLine extends AbstractFunctionToLine {

        public DeclarationToLine(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException {
            super(entry.getLine(), filePath, compDir, entry, onePath);
            assert entry.getKind() == TAG.DW_TAG_subprogram;
        }

        public DeclarationToLine(String filePath, int baseLine, Map<String, String> onePath) {
            super(baseLine, filePath, onePath);
        }

        @Override
        public SourceLineInfo getLine(int offset){
            return new SourceLineInfo(filePath, baseLine);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("File: "+filePath); // NOI18N
            buf.append("\n\tBase Line:  ").append(baseLine); // NOI18N
            return buf.toString();
        }

        @Override
        protected void dump(PrintStream out) {
            out.println(filePath);
            out.println(""+baseLine); // NOI18N
        }
    }

    private static final class FunctionToLine extends AbstractFunctionToLine {
        private final int[] lineStorage;
        private final int[] startOffsetStorage;
        private final int[] endOffsetStorage;

        public FunctionToLine(String filePath, String compDir, DwarfEntry entry, TreeSet<LineNumber> numbers, Map<String, String> onePath) throws IOException {
            super(entry.getLine(), filePath, compDir, entry, onePath);
            assert entry.getKind() == TAG.DW_TAG_subprogram;
            assert entry.getLowAddress() != 0;
            long base =entry.getLowAddress();
            long baseHihg =entry.getHighAddress();
            //print = entry.getName().equals("threadfunc");
            //if (print)  {
            //    System.err.println(""+entry);
            //}
            List<Integer> lineStorageList = new ArrayList<Integer>();
            List<Integer> startOffsetStorageList = new ArrayList<Integer>();
            List<Integer> endOffsetStorageList = new ArrayList<Integer>();
            for(LineNumber l : numbers) {
                if (l.startOffset >= base && l.endOffset <= baseHihg) {
                    //if (print) System.err.println(""+l);
                    lineStorageList.add(l.line);
                    startOffsetStorageList.add((int)(l.startOffset - base));
                    endOffsetStorageList.add((int)(l.endOffset - base));
                }
            }
            lineStorage = new int[lineStorageList.size()];
            startOffsetStorage = new int[startOffsetStorageList.size()];
            endOffsetStorage = new int[endOffsetStorageList.size()];
            for (int i = 0; i < lineStorageList.size(); i++){
                lineStorage[i] = lineStorageList.get(i);
                startOffsetStorage[i] = startOffsetStorageList.get(i);
                endOffsetStorage[i] = endOffsetStorageList.get(i);
            }
        }

        public FunctionToLine(String filePath, int baseLine, List<Integer> lines, List<Integer> startOffsets, List<Integer> endOffsets, Map<String, String> onePath) {
            super(baseLine, filePath, onePath);
            assert lines.size() == startOffsets.size();
            lineStorage = new int[lines.size()];
            startOffsetStorage = new int[startOffsets.size()];
            endOffsetStorage = new int[endOffsets.size()];
            for(int i = 0; i < lines.size(); i++) {
                lineStorage[i] = lines.get(i);
                startOffsetStorage[i] = startOffsets.get(i);
                endOffsetStorage[i] = endOffsets.get(i);
            }
        }

        @Override
        public SourceLineInfo getLine(int offset){
            //if (print) {
            //    System.err.println("");
            //}
            if (offset < 0) {
                if (baseLine > 0) {
                    return new SourceLineInfo(filePath, baseLine);
                } else {
                    if (lineStorage.length > 0){
                        return new SourceLineInfo(filePath, lineStorage[0]);
                    }
                }
                return null;
            }
            int res = -1;
            for (int i = 0; i < startOffsetStorage.length; i++) {
                if (startOffsetStorage[i] <= offset && offset < endOffsetStorage[i]) {
                    if (res == -1) {
                        res = i;
                    }
                }
            }
            if (res < 0) {
                return new SourceLineInfo(filePath, baseLine);
            }
            return new SourceLineInfo(filePath, lineStorage[res]);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("File: "+filePath); // NOI18N
            buf.append("\n\tBase Line:  ").append(baseLine); // NOI18N
            if (lineStorage.length>0) {
                for(int i = 0; i < lineStorage.length; i++) {
                    buf.append("\n\tLine: ").append(lineStorage[i]).append("\t (").append(startOffsetStorage[i]).append('-').append(endOffsetStorage[i]).append(")"); // NOI18N
                }
                //buf.append("\n\tStart Line: ").append(lineStorage[0]).append("\t (").append(offsetStorage[0]).append(")"); // NOI18N
                //buf.append("\n\tEnd Line:   ").append(lineStorage[lineStorage.length - 1]).append("\t (").append(offsetStorage[lineStorage.length - 1]).append(")"); // NOI18N
            }
            return buf.toString();
        }

        @Override
        protected void dump(PrintStream out) {
            out.println(filePath);
            out.println(""+baseLine); // NOI18N
            for(int i = 0; i < lineStorage.length; i++) {
                out.println(""+lineStorage[i]+","+startOffsetStorage[i]+","+endOffsetStorage[i]); // NOI18N
            }
        }
    }
}
