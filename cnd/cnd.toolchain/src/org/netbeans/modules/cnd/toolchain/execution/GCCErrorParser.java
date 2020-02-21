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

package org.netbeans.modules.cnd.toolchain.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerPattern;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.OutputListenerRegistry;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Result;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Results;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class GCCErrorParser extends ErrorParser {

    private final List<Pattern> GCC_ERROR_SCANNER = new ArrayList<>();
    private final List<Pattern> patterns = new ArrayList<>();
    private Pattern GCC_DIRECTORY_ENTER;
    private Pattern GCC_DIRECTORY_LEAVE;
    private Pattern GCC_DIRECTORY_CD;
    private Pattern GCC_DIRECTORY_MAKE_ALL;
    private final List<Pattern> GCC_STACK_HEADER = new ArrayList<>();
    private final List<Pattern> GCC_STACK_NEXT = new ArrayList<>();

    private final ArrayList<StackIncludeItem> errorInludes = new ArrayList<>();
    private OutputListenerRegistry listenerRegistry;

    public GCCErrorParser(Project project, CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(project, execEnv, relativeTo);
    	init(flavor);
    }

    private void init(CompilerFlavor flavor) {
	ScannerDescriptor scanner = flavor.getToolchainDescriptor().getScanner();
	if (scanner.getEnterDirectoryPattern() != null) {
	    GCC_DIRECTORY_ENTER = Pattern.compile(scanner.getEnterDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_ENTER);
	}
	if (scanner.getLeaveDirectoryPattern() != null) {
	    GCC_DIRECTORY_LEAVE = Pattern.compile(scanner.getLeaveDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_LEAVE);
	}
	if (scanner.getChangeDirectoryPattern() != null) {
	    GCC_DIRECTORY_CD = Pattern.compile(scanner.getChangeDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_CD);
	}
	if (scanner.getChangeDirectoryPattern() != null) {
	    GCC_DIRECTORY_MAKE_ALL = Pattern.compile(scanner.getMakeAllInDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_MAKE_ALL);
	}
	if (scanner.getStackHeaderPattern().size() > 0 && scanner.getStackNextPattern().size() > 0) {
            for(String pattern : scanner.getStackHeaderPattern()) {
                GCC_STACK_HEADER.add(Pattern.compile(pattern));
            }
	    patterns.addAll(GCC_STACK_HEADER);
            for(String pattern : scanner.getStackNextPattern()) {
                GCC_STACK_NEXT.add(Pattern.compile(pattern));
            }
	    patterns.addAll(GCC_STACK_NEXT);
	}
	for(ScannerPattern s : scanner.getPatterns()){
	    Pattern pattern = Pattern.compile(s.getPattern());
	    GCC_ERROR_SCANNER.add(pattern);
	    patterns.add(pattern);
	}
    }

    @Override
    public void setOutputListenerRegistry(OutputListenerRegistry regestry) {
        listenerRegistry = regestry;
    }

    private String getNoEscapeLine(String line) {
        int startEscape = line.indexOf("\u001b["); //NOI18N
        if (startEscape>=0) {
            // colored line
            StringBuilder buf = new StringBuilder();
            int i = 0;
            loop:while(true) {
                if (i >= line.length()) {
                    break;
                }
                char c = line.charAt(i);
                if (c == '\u001b' && i+1 < line.length()  && line.charAt(i+1) == '[') { //NOI18N
                    i+=2;
                    while(i < line.length()) {
                        char t = line.charAt(i);
                        if (t == ';' || (t >= '0'&& t <= '9')) { //NOI18N
                            i++;
                            continue;
                        }
                        i++;
                        continue loop;
                    }
                }
                i++;
                buf.append(c);
            }
            line = buf.toString();
        }
        return line;
    }
    
    @Override
    public Result handleLine(String line) {
        String noEscape = getNoEscapeLine(line);
        for (Pattern p : patterns) {
            Matcher m = p.matcher(noEscape);
            boolean found = m.find();
            if (found && m.start() == 0) {
                return handleLine(line, m);
            }
        }
        if (!errorInludes.isEmpty()) {
            Results res = new Results();
            for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                StackIncludeItem item = it.next();
                if (item.fo != null) {
                    res.add(item.line, listenerRegistry.register(item.fo, item.lineNumber, false, item.getMessage())); // NOI18N
                } else {
                    res.add(item.line, null);
                }
            }
            errorInludes.clear();
            res.add(line, null);
            return res;
        }
        return null;
    }

    private Result handleLine(String line, Matcher m) {
        if (m.pattern() == GCC_DIRECTORY_ENTER || m.pattern() == GCC_DIRECTORY_LEAVE) {
            String levelString = m.group(1);
            String directory = m.group(2);
            int level = levelString == null ? 0 : Integer.parseInt(levelString);
            if (m.pattern() == GCC_DIRECTORY_LEAVE) {
                getMakeContext().pop(level);
            } else {
                if (!CndPathUtilities.isAbsolute(directory)) {
                    FileObject lastContext = getMakeContext().getLastContext();
                    if (lastContext != null) {
                        if (lastContext.isFolder()) {
                            directory = lastContext.toURL().getPath() + File.separator + directory;
                        }
                    }
                }
                FileObject relativeDir = resolveFile(directory, true);
                if (relativeDir != null && relativeDir.isValid()) {
                    getMakeContext().push(level, relativeDir);
                }
            }
            return ErrorParserProvider.NO_RESULT;
        } else if (m.pattern() == GCC_DIRECTORY_CD) {
            String directory = trimQuotes(m.group(1));
            if (!CndPathUtilities.isAbsolute(directory)) {
                FileObject lastContext = getMakeContext().getLastContext();
                if (lastContext != null) {
                    if (lastContext.isFolder()) {
                        directory = lastContext.toURL().getPath() + File.separator + directory;
                    }
                }
            }
            FileObject relativeDir = resolveFile(directory, true);
            if (relativeDir != null && relativeDir.isValid()) {
                getMakeContext().push(-1, relativeDir);
            }
            return ErrorParserProvider.NO_RESULT;
        } else if (m.pattern() == GCC_DIRECTORY_MAKE_ALL) {
            String directory = m.group(1);
            if (!CndPathUtilities.isAbsolute(directory)) {
                FileObject lastContext = getMakeContext().getLastContext();
                if (lastContext != null) {
                    if (lastContext.isFolder()) {
                        directory = lastContext.toURL().getPath() + File.separator + directory;
                    }
                }
            }
            FileObject relativeDir = resolveFile(directory, true);
            if (relativeDir != null && relativeDir.isValid()) {
                getMakeContext().push(-1, relativeDir);
            }
            return ErrorParserProvider.NO_RESULT;
        }
        if (GCC_STACK_NEXT.contains(m.pattern()) || GCC_STACK_HEADER.contains(m.pattern())) {
            try {
                String file = m.group(1);
                if (m.groupCount() >= 2){
                    Integer lineNumber = Integer.valueOf(m.group(2));
                    FileObject relativeDir = getMakeContext().getTopContext();
                    if (relativeDir != null) {
                        FileObject fo = resolveRelativePath(relativeDir, file);
                        if (fo != null && fo.isValid()) {
                            errorInludes.add(new StackIncludeItem(fo, line, lineNumber - 1));
                            return new Results();
                        }
                    }
                }
            } catch (NumberFormatException e) {
            }
            errorInludes.add(new StackIncludeItem(null, line, 0));
            return new Results();
        }
        if (GCC_ERROR_SCANNER.contains(m.pattern())) {
            Results res = new Results();
            try {
                String file = null;
                if(m.groupCount() == 5) {
                    file = m.group(4);
                }                
                Integer lineNumber;
                String description = null;
                if(file == null || !file.matches(".*\\.pc")) { // NOI18N 
                    file = m.group(1);
                    lineNumber = Integer.valueOf(m.group(2));
                    if (m.groupCount()>= 4) {
                        description = m.group(4);
                    }
                } else {
                    lineNumber = Integer.valueOf(m.group(2));
                    description = m.group(1);
                }
                FileObject relativeDir = getMakeContext().getTopContext();
                if (relativeDir != null) {
                    //FileObject fo = relativeDir.getFileObject(file);
                    FileObject fo = resolveRelativePath(relativeDir, file);
                    boolean important = false;
                    if (m.groupCount() > 2) {
                        if (isNumber(m.group(3))) {
                            // group 3 contains column since GNU 4.x
                            // description contains " severity: description"
                            if (description != null) {
                                important = description.trim().toLowerCase().indexOf("error:") == 0; // NOI18N
                            }
                        } else {
                            // group 3 contains severity of the message for GNU 3.x
                            important = m.group(3).toLowerCase().indexOf("error") != (-1); // NOI18N
                        }
                    }
                    if (fo != null && fo.isValid()) {
                        for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                            StackIncludeItem item = it.next();
                            if (item.fo != null) {
                                res.add(item.line, listenerRegistry.register(item.fo, item.lineNumber, important, item.getMessage()));
                            } else {
                                res.add(item.line, null);
                            }
                        }
                        errorInludes.clear();
                        res.add(line, listenerRegistry.register(fo, lineNumber - 1, important, description));
                        return res;
                    }
                }
            } catch (NumberFormatException e) {
            }
            for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                StackIncludeItem item = it.next();
                res.add(item.line, null);
            }
            errorInludes.clear();
            res.add(line, null);
            return res;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
    }

    private boolean isNumber(String s) {
        boolean res = false;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            res = true;
        }
        return res;
    }
    
    private String trimQuotes(String s){
        if (s.length() > 2) {
            if (s.startsWith("\"") && s.endsWith("\"")) { // NOI18N
                return s.substring(1, s.length()-1);
            }else if (s.startsWith("'") && s.endsWith("'")) { // NOI18N
                return s.substring(1, s.length()-1);
            }
        }
        return s;
    }

    private static class StackIncludeItem {

        private final FileObject fo;
        private final String line;
        private final int lineNumber;

        private StackIncludeItem(FileObject fo, String line, int lineNumber) {
            super();
            this.fo = fo;
            this.line = line;
            this.lineNumber = lineNumber;
        }
        
        private String getMessage() {
            if (line.contains("instantiation of") || line.contains("instantiated from") ) { //NOI18N
                //TODO move to scanner
                return NbBundle.getMessage(GCCErrorParser.class, "HINT_InstantiatedFrom"); //NOI18N
            }
            return NbBundle.getMessage(GCCErrorParser.class, "HINT_IncludedFrom"); //NOI18N
        }
    }
}
