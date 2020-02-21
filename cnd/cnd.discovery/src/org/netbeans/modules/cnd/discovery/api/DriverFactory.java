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
package org.netbeans.modules.cnd.discovery.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;

/**
 *
 */
public final class DriverFactory {

    public static Driver getDriver(CompilerSet set) {
        return new DriverImpl(set);
    }
    
    public static ItemProperties.LanguageStandard getLanguageStandard(ItemProperties.LanguageStandard standard, Artifacts artifacts) {
        ItemProperties.LanguageStandard res = standard;
        for (String lang : artifacts.getLanguageArtifacts()) {
            if (null != lang) {
                switch (lang) {
                    case "c89": //NOI18N
                        res = ItemProperties.LanguageStandard.C89;
                        break;
                    case "c99": //NOI18N
                        res = ItemProperties.LanguageStandard.C99;
                        break;
                    case "c11": //NOI18N
                        res = ItemProperties.LanguageStandard.C11;
                        break;
                    case "c++98": //NOI18N
                        res = ItemProperties.LanguageStandard.CPP98;
                        break;
                    case "c++11": //NOI18N
                        res = ItemProperties.LanguageStandard.CPP11;
                        break;
                    case "c++14": //NOI18N
                        res = ItemProperties.LanguageStandard.CPP14;
                        break;
                    case "c++17": //NOI18N
                        res = ItemProperties.LanguageStandard.CPP17;
                        break;
                    default:
                        break;
                }
            }
        }
        return res;
    }
    
    public static String importantFlagsToString(Artifacts artifacts) {
        StringBuilder buf = new StringBuilder();
        for (String flag : artifacts.getImportantFlags()) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            if (flag.indexOf(' ')>0) {
                buf.append('\'').append(flag).append('\'');
            } else {
                buf.append(flag);
            }
        }
        return buf.toString();
    }
    
    public static String removeQuotes(String path) {
        if (path.length() >= 2 && (path.charAt(0) == '\'' && path.charAt(path.length() - 1) == '\'' || // NOI18N
                path.charAt(0) == '"' && path.charAt(path.length() - 1) == '"')) {// NOI18N
            path = path.substring(1, path.length() - 1);
        }
        return path;
    }

    public static String normalizeDefineOption(String value, CompileLineOrigin isScriptOutput, boolean isQuote) {
        switch (isScriptOutput) {
            case BuildLog:
                if (value.length() >= 2 && value.charAt(0) == '`' && value.charAt(value.length() - 1) == '`') { // NOI18N
                    value = value.substring(1, value.length() - 1);  // NOI18N
                }
                if (value.length() >= 6
                        && (value.charAt(0) == '"' && value.charAt(1) == '\\' && value.charAt(2) == '"'// NOI18N
                        &&  value.charAt(value.length() - 3) == '\\' && value.charAt(value.length() - 2) == '"' && value.charAt(value.length() - 1) == '"')) { // NOI18N
                    // What is it?
                    value = value.substring(2, value.length() - 3) + "\"";  // NOI18N
                } else if  (value.length() >= 4
                        && (value.charAt(0) == '\\' && value.charAt(1) == '"' // NOI18N
                        &&  value.charAt(value.length() - 2) == '\\' && value.charAt(value.length() - 1) == '"')) { // NOI18N
                    value = value.substring(1, value.length() - 2) + "\"";  // NOI18N
                } else if  (value.length() >= 4
                        && (value.charAt(0) == '\\' && value.charAt(1) == '\'' // NOI18N
                        &&  value.charAt(value.length() - 2) == '\\' && value.charAt(value.length() - 1) == '\'')) { // NOI18N
                    value = value.substring(1, value.length() - 2) + "'";  // NOI18N
                } else if (!isQuote && value.length() >= 2
                        && (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'' // NOI18N
                        ||  value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')) { // NOI18N
                    value = value.substring(1, value.length() - 1);
                }
                break;
            case DwarfCompileLine:
                if (value.length() >= 2
                        && (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'' // NOI18N
                        ||  value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')) { // NOI18N
                    value = removeEscape(value.substring(1, value.length() - 1));
                }
                break;
            case ExecLog:
                // do nothing
                break;
        }
        return value;
    }

    public static String removeEscape(String s) {
        int n = s.length();
        StringBuilder ret = new StringBuilder(n);
        char prev = 0;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') || // NOI18N
                    (c == ':') || (c == '\'') || // NOI18N
                    (c == '*') || (c == '\"') || // NOI18N
                    (c == '[') || (c == ']') || // NOI18N
                    (c == '(') || (c == ')') || // NOI18N
                    (c == ';')) { // NOI18N
                if (prev == '\\') { // NOI18N
                    ret.setLength(ret.length() - 1);
                }
            }
            ret.append(c);
            prev = c;
        }
        return ret.toString();
    }

    private static final class DriverImpl implements Driver {

        private static final List<String> C89 =   Collections.unmodifiableList(Arrays.asList("-std=c89", "-std=iso9899:1990", "-std=iso9899:1990", "-std=c90")); // NOI18N
        private static final List<String> C99 =   Collections.unmodifiableList(Arrays.asList("-xc99", "-std=c9x", "-std=iso9899:199409", "-std=iso9899:199x", "-std=iso9899:1999", "-std=gnu99", "-std=gnu9x", "-std=c99")); // NOI18N
        private static final List<String> C11 =   Collections.unmodifiableList(Arrays.asList("-std=c11", "-std=gnu1x", "-std=gnu11", "-std=iso9899:2011", "-std=c1x")); // NOI18N
        private static final List<String> CPP98 = Collections.unmodifiableList(Arrays.asList("-std=c++98", "-std=c++03", "-std=sun03")); // NOI18N
        private static final List<String> CPP11 = Collections.unmodifiableList(Arrays.asList("-std=c++11", "-std=c++0x", "-std=gnu++11", "-std=gnu++0x")); // NOI18N
        private static final List<String> CPP14 = Collections.unmodifiableList(Arrays.asList("-std=c++14", "-std=c++1y", "-std=gnu++14", "-std=gnu++1y")); // NOI18N
        private static final List<String> CPP17 = Collections.unmodifiableList(Arrays.asList("-std=c++17", "-std=c++1z", "-std=gnu++17", "-std=gnu++1z")); // NOI18N

        private Pattern cPattern;
        private boolean cPatternInited;
        private Pattern cppPattern;
        private boolean cppPatternInited;
        final CompilerSet compilerSet;

        private DriverImpl(CompilerSet compilerSet) {
            this.compilerSet = compilerSet;
        }

        @Override
        public List<String> splitCommandLine(String line, CompileLineOrigin isScriptOutput) {
            List<String> res = new ArrayList<>();
            int i = 0;
            StringBuilder current = new StringBuilder();
            boolean isSingleQuoteMode = false;
            boolean isDoubleQuoteMode = false;
            boolean isParen = false;
            while (i < line.length()) {
                char c = line.charAt(i);
                i++;
                switch (c) {
                    case '\'': // NOI18N
                        if (isSingleQuoteMode) {
                            isSingleQuoteMode = false;
                        } else if (!isDoubleQuoteMode) {
                            isSingleQuoteMode = true;
                        }
                        isParen = false;
                        current.append(c);
                        break;
                    case '\"': // NOI18N
                        if (isDoubleQuoteMode) {
                            isDoubleQuoteMode = false;
                        } else if (!isSingleQuoteMode) {
                            isDoubleQuoteMode = true;
                        }
                        isParen = false;
                        current.append(c);
                        break;
                    case ' ': // NOI18N
                    case '\t': // NOI18N
                    case '\n': // NOI18N
                    case '\r': // NOI18N
                        if (isSingleQuoteMode || isDoubleQuoteMode) {
                            current.append(c);
                            break;
                        } else if (isParen && isScriptOutput == CompileLineOrigin.DwarfCompileLine) {
                            current.append(c);
                        } else {
                            if (current.length() > 0) {
                                res.add(current.toString());
                                current.setLength(0);
                            }
                        }
                        break;
                    case '(': // NOI18N
                        if (!(isSingleQuoteMode || isDoubleQuoteMode)) {
                            isParen = true;
                        }
                        current.append(c);
                        break;
                    case ')': // NOI18N
                        if (!(isSingleQuoteMode || isDoubleQuoteMode)) {
                            isParen = false;
                        }
                        current.append(c);
                        break;
                    default:
                        current.append(c);
                        break;
                }
            }
            if (current.length() > 0) {
                res.add(current.toString());
            }
            return res;
        }

        @Override
        public Artifacts gatherCompilerLine(String line, CompileLineOrigin isScriptOutput, boolean isCpp) {
            List<String> list = splitCommandLine(line, isScriptOutput);
            boolean hasQuotes = false;
            for (String s : list) {
                if (s.startsWith("\"")) {  //NOI18N
                    hasQuotes = true;
                    break;
                }
            }
            if (hasQuotes) {
                List<String> newList = new ArrayList<>();
                for (int i = 0; i < list.size();) {
                    String s = list.get(i);
                    if (s.startsWith("-D") && s.endsWith("=") && i + 1 < list.size() && list.get(i + 1).startsWith("\"")) { // NOI18N
                        String longString = null;
                        for (int j = i + 1; j < list.size() && list.get(j).startsWith("\""); j++) {  //NOI18N
                            if (longString != null) {
                                longString += " " + list.get(j);  //NOI18N
                            } else {
                                longString = list.get(j);
                            }
                            i = j;
                        }
                        newList.add(s + "`" + longString + "`");  //NOI18N
                    } else {
                        newList.add(s);
                    }
                    i++;
                }
                list = newList;
            }
            ListIterator<String> st = list.listIterator();
            if (st.hasNext()) {
                String option = st.next();
                if (option.equals("+") && st.hasNext()) { // NOI18N
                    st.next();
                }
            }
            return gatherCompilerLine(st, isScriptOutput, isCpp);
        }

        @Override
        public Artifacts gatherCompilerLine(ListIterator<String> st, CompileLineOrigin isScriptOutput, boolean isCpp) {
            String option;
            ArtifactsImpl artifacts = new ArtifactsImpl();
            List<String> what = artifacts.input;
            List<String> importantCandidates = new ArrayList<>();
            while (st.hasNext()) {
                option = st.next();
                boolean isQuote = false;
                if (isScriptOutput == CompileLineOrigin.BuildLog) {
                    if (option.startsWith("'") && option.endsWith("'") || // NOI18N
                            option.startsWith("\"") && option.endsWith("\"")) { // NOI18N
                        if (option.length() >= 2) {
                            option = option.substring(1, option.length() - 1);
                            isQuote = true;
                        }
                    }
                }
                if (option.startsWith("--")) { // NOI18N
                    option = option.substring(1);
                }
                if (option.startsWith("-D")) { // NOI18N
                    String macro;
                    if (option.equals("-D") && st.hasNext()) {  //NOI18N
                        macro = st.next();
                    } else {
                        macro = option.substring(2);
                    }
                    macro = removeQuotes(macro);
                    int i = macro.indexOf('=');
                    if (i > 0) {
                        String value = macro.substring(i + 1).trim();
                        value = normalizeDefineOption(value, isScriptOutput, isQuote);
                        String key = removeEscape(macro.substring(0, i));
                        addDef(key, value, artifacts.userMacros, artifacts.undefinedMacros);
                    } else {
                        String key = removeEscape(macro);
                        addDef(key, null, artifacts.userMacros, artifacts.undefinedMacros);
                    }
                } else if (option.startsWith("-U")) { // NOI18N
                    String macro = option.substring(2);
                    if (macro.length() == 0 && st.hasNext()) {
                        macro = st.next();
                    }
                    macro = removeQuotes(macro);
                    addUndef(macro, artifacts.userMacros, artifacts.undefinedMacros);
                } else if (option.startsWith("-I")) { // NOI18N
                    String path = option.substring(2);
                    if (path.length() == 0 && st.hasNext()) {
                        path = st.next();
                    }
                    path = removeQuotes(path);
                    artifacts.userIncludes.add(path);
                } else if (option.startsWith("-F")) { // NOI18N
                    if (option.equals("-F") && st.hasNext()) { // NOI18N
                        String path = st.next();
                        path = removeQuotes(path)+Driver.FRAMEWORK;
                        artifacts.userIncludes.add(path);
                    }
                } else if (option.startsWith("-isystem")) { // NOI18N
                    String path = option.substring(8);
                    if (path.length() == 0 && st.hasNext()) {
                        path = st.next();
                    }
                    path = removeQuotes(path);
                    artifacts.userIncludes.add(path);
                } else if (option.startsWith("-include")) { // NOI18N
                    String path = option.substring(8);
                    if (path.length() == 0 && st.hasNext()) {
                        path = st.next();
                    }
                    path = removeQuotes(path);
                    artifacts.userFiles.add(path);
                } else if (option.startsWith("-imacros")) { // NOI18N
                    String path = option.substring(8);
                    if (path.length() == 0 && st.hasNext()) {
                        path = st.next();
                    }
                    path = removeQuotes(path);
                    artifacts.userFiles.add(path);
                } else if (option.startsWith("-Y")) { // NOI18N
                    String defaultSearchPath = option.substring(2);
                    if (defaultSearchPath.length() == 0 && st.hasNext()) {
                        defaultSearchPath = st.next();
                    }
                    if (defaultSearchPath.startsWith("I,")) { // NOI18N
                        defaultSearchPath = defaultSearchPath.substring(2);
                        defaultSearchPath = removeQuotes(defaultSearchPath);
                        artifacts.userIncludes.add(defaultSearchPath);
                    }
                } else if (option.startsWith("-idirafter")) { // NOI18N
                    //Search dir for header files, but do it after all directories specified with -I and the standard system directories have been exhausted.
                    if (option.equals("-idirafter") && st.hasNext()) { // NOI18N
                        st.next();
                    }
                } else if (option.startsWith("-iprefix")) { // NOI18N
                    //Specify prefix as the prefix for subsequent -iwithprefix options.
                    if (option.equals("-iprefix") && st.hasNext()) { // NOI18N
                        st.next();
                    }
                } else if (option.startsWith("-iwithprefix")) { // NOI18N
                    //Append dir to the prefix specified previously with -iprefix, and add the resulting directory to the include search path.
                    if (option.equals("-iwithprefix") && st.hasNext()) { // NOI18N
                        st.next();
                    }
                } else if (option.startsWith("-iwithprefixbefore")) { // NOI18N
                    //Append dir to the prefix specified previously with -iprefix, and add the resulting directory to the include search path.
                    if (option.equals("-iwithprefixbefore") && st.hasNext()) { // NOI18N
                        st.next();
                    }
                } else if (option.startsWith(Driver.ISYSROOT_FLAG)) { // NOI18N
                    String path = option.substring(9);
                    if (path.length() == 0 && st.hasNext()) {
                        path = st.next();
                    }
                    // sure it is an important flag
                    artifacts.importantFlags.add(option);
                    path = removeQuotes(path);
                    artifacts.importantFlags.add(path);
                } else if (option.startsWith("-iquote")) { // NOI18N
                    //Search dir only for header files requested with "#include " file ""
                    if (option.equals("-iquote") && st.hasNext()) { // NOI18N
                        st.next();
                    }
                } else if (option.equals("-K")) { // NOI18N
                    // Skip pic
                    if (st.hasNext()) {
                        String next = st.next();
                        if (next.equals("PIC") || next.equals("pic")) { // NOI18N
                            // options = "-K"+next;
                            importantCandidates.add(option + next);
                        } else {
                            st.previous();
                        }
                    }
                } else if (option.equals("-R")) { // NOI18N
                    // Skip runtime search path 
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.startsWith("-l")) { // NOI18N
                    String lib = option.substring(2);
                    if (lib.length() == 0 && st.hasNext()) {
                        lib = st.next();
                    }
                    // library
                    if (lib.length() > 0) {
                        artifacts.libraries.add(lib);
                    }
                } else if (option.equals("-L")) { // NOI18N
                    // Skip library search path
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-M")) { // NOI18N
                    // Skip library search path
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-h")) { // NOI18N
                    // Skip generated dynamic shared library
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-o")) { // NOI18N
                    // Skip result
                    if (st.hasNext()) {
                        artifacts.output = st.next();
                    }
                    // generation 2 of params
                } else if (option.equals("-z")) { // NOI18N
                    // ld params of gcc
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-x")) { // NOI18N
                    // Specify explicitly the language for the following input files (rather than letting the compiler choose a default based on the file name suffix).
                    if (st.hasNext()) {
                        String lang = st.next();
                        artifacts.languageArtifacts.add(lang);
                        if (lang.equals("c")) {// NOI18N
                            isCpp = false;
                        } else if (lang.equals("c++")) {// NOI18N
                            isCpp = true;
                        }
                        importantCandidates.add(option + lang);
                    }
                } else if (option.equals("-xc")) { // NOI18N
                    artifacts.languageArtifacts.add("c"); // NOI18N	
                    isCpp = false;
                    importantCandidates.add(option);
                } else if (option.equals("-xc++")) { // NOI18N
                    artifacts.languageArtifacts.add("c++"); // NOI18N
                    isCpp = true;
                    importantCandidates.add(option);
                } else if (C89.contains(option)) {
                    artifacts.languageArtifacts.add("c89"); // NOI18N
                    isCpp = false;
                    importantCandidates.add(option);
                } else if (C99.contains(option)) {
                    artifacts.languageArtifacts.add("c99"); // NOI18N
                    isCpp = false;
                    importantCandidates.add(option);
                } else if (C11.contains(option)) {
                    artifacts.languageArtifacts.add("c11"); // NOI18N
                    isCpp = false;
                    importantCandidates.add(option);
                } else if (CPP11.contains(option)) {
                    artifacts.languageArtifacts.add("c++11"); // NOI18N
                    isCpp = true;
                    importantCandidates.add(option);
                } else if (CPP14.contains(option)) {
                    artifacts.languageArtifacts.add("c++14"); // NOI18N
                    isCpp = true;
                    importantCandidates.add(option);
                } else if (CPP17.contains(option)) {
                    artifacts.languageArtifacts.add("c++17"); // NOI18N
                    isCpp = true;
                    importantCandidates.add(option);
                } else if (CPP98.contains(option)) {
                    artifacts.languageArtifacts.add("c++98"); // NOI18N
                    isCpp = true;
                    importantCandidates.add(option);
                } else if (option.equals("-xMF")) { // NOI18N
                    // ignore dependency output file
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-MF")) { // NOI18N
                    // ignore dependency output file
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-MT")) { // NOI18N
                    // once more fancy preprocessor option with parameter. Ignore.
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-MQ")) { // NOI18N
                    // once more fancy preprocessor option with parameter. Ignore.
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.equals("-aux-info")) { // NOI18N
                    // Output to the given filename prototyped declarations for all functions declared and/or defined in a translation unit, including those in header files. Ignore.
                    if (st.hasNext()) {
                        st.next();
                    }
                } else if (option.startsWith("-")) { // NOI18N
                    importantCandidates.add(option);
                } else if (option.startsWith("ccfe")) { // NOI18N
                    // Skip option
                } else if (option.startsWith(">")) { // NOI18N
                    // Skip redurect
                    break;
                } else {
                    if (SourcesVisibilityQuery.getDefault().isVisible(option)) {
                        what.add(option);
                    }
                }
            }
            for (String candidate : importantCandidates) {
                if (isImportantFlag(candidate, isCpp)) {
                    artifacts.importantFlags.add(candidate);
                }
            }
            return artifacts;
        }
  
        private void addDef(String macro, String value, Map<String, String> userMacros, List<String> undefinedMacros) {
            undefinedMacros.remove(macro);
            userMacros.put(macro, value);
        }

        private void addUndef(String macro, Map<String, String> userMacros, List<String> undefinedMacros) {
            if (userMacros.containsKey(macro)) {
                userMacros.remove(macro);
            } else {
                if (!undefinedMacros.contains(macro)) {
                    undefinedMacros.add(macro);
                }
            }
        }
        
        private boolean isImportantFlag(String flag, boolean isCPP) {
            if (isCPP) {
                if (!cppPatternInited) {
                    AbstractCompiler compiler;
                    if (compilerSet != null) {
                        compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                        if (compiler != null && compiler.getDescriptor() != null) {
                            String importantFlags = compiler.getDescriptor().getImportantFlags();
                            if (importantFlags != null && importantFlags.length() > 0) {
                                cppPattern = Pattern.compile(importantFlags);
                            }
                        }
                    }
                    cppPatternInited = true;
                }
                if (cppPattern != null) {
                    return cppPattern.matcher(flag).find();
                }
            } else {
                if (!cPatternInited) {
                    AbstractCompiler compiler;
                    if (compilerSet != null) {
                        compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
                        if (compiler != null && compiler.getDescriptor() != null) {
                            String importantFlags = compiler.getDescriptor().getImportantFlags();
                            if (importantFlags != null && importantFlags.length() > 0) {
                                cPattern = Pattern.compile(importantFlags);
                            }
                        }
                    }
                    cPatternInited = true;
                }
                if (cPattern != null) {
                    return cPattern.matcher(flag).find();
                }
            }
            return false;
        }  
    }
    
    
    private static final class ArtifactsImpl implements Artifacts {
        public final List<String> input = new ArrayList<>();
        public final List<String> userIncludes = new ArrayList<>();
        public final List<String> userFiles = new ArrayList<>();
        public final Map<String, String> userMacros = new HashMap<>();
        public final List<String> undefinedMacros = new ArrayList<>();
        public final Set<String> libraries = new HashSet<>();
        public final List<String> languageArtifacts = new ArrayList<>();
        public final List<String> importantFlags = new ArrayList<>();
        public String output;

        @Override
        public List<String> getInput() {
            return input;
        }

        @Override
        public List<String> getUserIncludes() {
            return userIncludes;
        }

        @Override
        public List<String> getUserFiles() {
            return userFiles;
        }

        @Override
        public Map<String, String> getUserMacros() {
            return userMacros;
        }

        @Override
        public List<String> getUserUndefinedMacros() {
            return undefinedMacros;
        }

        @Override
        public Set<String> getLibraries() {
            return libraries;
        }

        @Override
        public List<String> getLanguageArtifacts() {
            return languageArtifacts;
        }

        @Override
        public List<String> getImportantFlags() {
            return importantFlags;
        }

        @Override
        public String getOutput() {
            return output;
        }
    }

}
