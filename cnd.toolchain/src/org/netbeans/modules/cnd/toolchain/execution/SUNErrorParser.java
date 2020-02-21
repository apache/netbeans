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

import java.util.ArrayList;
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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

public final class SUNErrorParser extends ErrorParser {

    private final List<Pattern> errorScuners = new ArrayList<>();
    private final List<Pattern> patterns = new ArrayList<>();
    private final List<String> severity = new ArrayList<>();
    private final List<Pattern> SunStudioOutputFilters = new ArrayList<>();
    private Pattern SUN_DIRECTORY_ENTER;
    private OutputListenerRegistry listenerRegistry;

    public SUNErrorParser(Project project, CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(project, execEnv, relativeTo);
	init(flavor);
    }

    private void init(CompilerFlavor flavor) {
	ScannerDescriptor scanner = flavor.getToolchainDescriptor().getScanner();
	for(ScannerPattern s : scanner.getPatterns()){
	    Pattern pattern = Pattern.compile(s.getPattern());
	    patterns.add(pattern);
	    severity.add(s.getSeverity());
	    errorScuners.add(pattern);
	}
	if (scanner.getEnterDirectoryPattern() != null) {
	    SUN_DIRECTORY_ENTER = Pattern.compile(scanner.getEnterDirectoryPattern());
	    patterns.add(SUN_DIRECTORY_ENTER);
	}
	for(String s : scanner.getFilterOutPatterns()){
	    SunStudioOutputFilters.add(Pattern.compile(s));
	}
    }

    @Override
    public void setOutputListenerRegistry(OutputListenerRegistry regestry) {
        listenerRegistry = regestry;
    }

    @Override
    public Result handleLine(String line) {
        Result res = handleLineImpl(line);
        if (res == null || res == ErrorParserProvider.NO_RESULT) {
            // Remove lines extra lines from Sun Compiler output
            for (Pattern pattern : SunStudioOutputFilters) {
                Matcher skipper = pattern.matcher(line);
                boolean found = skipper.find();
                if (found && skipper.start() == 0) {
                    return ErrorParserProvider.REMOVE_LINE;
                }
            }
        }
        return res;
    }

    private Result handleLineImpl(String line) {
        for (Pattern p : patterns) {
            Matcher m = p.matcher(line);
            boolean found = m.find();
            if (found && m.start() == 0) {
                return handleLine(line, m);
            }
        }
        return null;
    }

    private Result handleLine(String line, Matcher m) {
        if (m.pattern() == SUN_DIRECTORY_ENTER) {
            FileObject myObj = resolveFile(m.group(1), true);
            if (myObj != null) {
                getMakeContext().push(-1, myObj);
            }
            return ErrorParserProvider.NO_RESULT;
        }
	int i = errorScuners.indexOf(m.pattern());
        if (i >= 0) {
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
                    if (m.groupCount()>= 3) {
                        description = m.group(3);
                    }
                } else {
                    lineNumber = Integer.valueOf(m.group(2));
                    description = m.group(1);
                }
                FileObject relativeTo = getMakeContext().getTopContext();
                if (relativeTo != null && relativeTo.isValid()) {
                    FileObject fo = resolveRelativePath(relativeTo, file);
                    boolean important = severity.get(i).equals("error"); // NOI18N
                    if (fo != null && fo.isValid()) {
                        return new Results(line, listenerRegistry.register(fo, lineNumber - 1, important, description));
                    }
                }
            } catch (NumberFormatException e) {
            }
            return ErrorParserProvider.NO_RESULT;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
    }
}
