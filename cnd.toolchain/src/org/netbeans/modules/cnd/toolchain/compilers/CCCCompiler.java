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

package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.PredefinedMacro;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetPreferences;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.toolchain.support.CompilerDefinition;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import static org.netbeans.modules.nativeexecution.api.util.Path.getPath;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

public abstract class CCCCompiler extends AbstractCompiler {
    
    private static final Logger LOG = Logger.getLogger(CCCCompiler.class.getName());
    private static final String DEV_NULL = "/dev/null"; // NOI18N
    private static final String NB69_VERSION_PATTERN = "/var/cache/cnd/remote-includes/"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("ReadErrorStream", 2); // NOI18N
    private static final String SYSTEM_INCLUDE_KEY = ".systemIncludes"; //NOI18N
    private static final String SYSTEM_INCLUDE_HEADER_KEY = ".systemIncludeHeaders"; //NOI18N
    private static final String SYSTEM_MACROS_KEY = ".systemMacros"; //NOI18N
    private static final String USER_ADDED_KEY = ".useradded."; //NOI18N
    private static final String LIST_COUNT_KEY = ".count"; //NOI18N

    private volatile CompilerDefinitions compilerDefinitions;
    private static File emptyFile = null;

    protected CCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    @Override
    public boolean setSystemIncludeDirectories(List<String> values) {
        return copySystemIncludeDirectoriesImpl(values, true);
    }
    
    protected final boolean copySystemIncludeDirectories(List<String> values) {
        boolean res = copySystemIncludeDirectoriesImpl(values, false);
        if (res) {
            if (values instanceof CompilerDefinition) {
                CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemIncludeDirectoriesList).clear();
                CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemIncludeDirectoriesList).
                        addAll(CompilerDefinitionAccessor.get().getUserAddedDefinitions((CompilerDefinition)values));
            }
        }
        return res;
    }
    
    private boolean copySystemIncludeDirectoriesImpl(List<String> values, boolean normalize) {
        assert values != null;
        if (compilerDefinitions == null) {
            compilerDefinitions = new CompilerDefinitions();
        }
        if (values.equals(compilerDefinitions.systemIncludeDirectoriesList)) {
            return false;
        }
        CompilerDefinition systemIncludeDirectoriesList = new CompilerDefinition(values);
        if (normalize) {
            normalizePaths(systemIncludeDirectoriesList);
        }
        CompilerDefinitionAccessor.get().getUserAddedDefinitions(systemIncludeDirectoriesList).
                addAll(CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemIncludeDirectoriesList));
        compilerDefinitions.systemIncludeDirectoriesList = systemIncludeDirectoriesList;
        return true;
    }

    @Override
    public boolean setSystemIncludeHeaders(List<String> values) {
        return copySystemIncludeHeadersImpl(values, true);
    }

    protected final boolean copySystemIncludeHeaders(List<String> values) {
        boolean res = copySystemIncludeHeadersImpl(values, false);
        if (res) {
            if (values instanceof CompilerDefinition) {
                CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemIncludeHeadersList).clear();
                CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemIncludeHeadersList).
                        addAll(CompilerDefinitionAccessor.get().getUserAddedDefinitions((CompilerDefinition)values));
            }
        }
        return res;
    }

    private boolean copySystemIncludeHeadersImpl(List<String> values, boolean normalize) {
        assert values != null;
        if (compilerDefinitions == null) {
            compilerDefinitions = new CompilerDefinitions();
        }
        if (values.equals(compilerDefinitions.systemIncludeHeadersList)) {
            return false;
        }
        CompilerDefinition systemIncludeHeadersList = new CompilerDefinition(values);
        if (normalize) {
            normalizePaths(systemIncludeHeadersList);
        }
        CompilerDefinitionAccessor.get().getUserAddedDefinitions(systemIncludeHeadersList).
                addAll(CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemIncludeHeadersList));
        compilerDefinitions.systemIncludeHeadersList = systemIncludeHeadersList;
        return true;
    }
    
    @Override
    public boolean setSystemPreprocessorSymbols(List<String> values) {
        assert values != null;
        if (compilerDefinitions == null) {
            compilerDefinitions = new CompilerDefinitions();
        }
        if (values.equals(compilerDefinitions.systemPreprocessorSymbolsList)) {
            return false;
        }
        CompilerDefinition systemPreprocessorSymbolsList = new CompilerDefinition(values);
        CompilerDefinitionAccessor.get().getUserAddedDefinitions(systemPreprocessorSymbolsList).
                addAll(CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemPreprocessorSymbolsList));
        compilerDefinitions.systemPreprocessorSymbolsList = systemPreprocessorSymbolsList;
        return true;
    }
    
    protected final boolean copySystemPreprocessorSymbols(List<String> values) {
        boolean res = setSystemPreprocessorSymbols(values);
        if (res) {
            if (values instanceof CompilerDefinition) {
                CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemPreprocessorSymbolsList).clear();
                CompilerDefinitionAccessor.get().getUserAddedDefinitions(compilerDefinitions.systemPreprocessorSymbolsList).
                        addAll(CompilerDefinitionAccessor.get().getUserAddedDefinitions((CompilerDefinition)values));
            }
        }
        return res;
    }

    @Override
    public List<String> getSystemPreprocessorSymbols() {
        CompilerDefinitions cur = compilerDefinitions;
        if (cur == null) {
            cur = resetAndGetCompilerDefinitions();
        }
        return cur.systemPreprocessorSymbolsList;
    }
    
    private final Map<String,CompilerDefinitions> particularModel = new HashMap<String,CompilerDefinitions>();
    
    @Override
    public List<String> getSystemPreprocessorSymbols(String flags) {
        if (flags != null && !flags.isEmpty()) {
            CompilerDefinitions particular;
            synchronized (particularModel) {
                particular = particularModel.get(flags);
                if (particular == null) {
                    MyCallable<CompilerDefinitions> callable = getCallable();
                    particular = callable.call(flags);
                    particularModel.put(flags, particular);
                    if (particular.systemPreprocessorSymbolsList.size() > 6 && particular.exitCode == 0) {
                        applyUserAddedDefinitions(particular);
                    }
                }
            }
            if (particular.systemPreprocessorSymbolsList.size() > 6 && particular.exitCode == 0) {
                return predefinedMacrosByFlags(particular.systemPreprocessorSymbolsList, flags);
            }
        }
        return predefinedMacrosByFlags(getSystemPreprocessorSymbols(), flags);
    }

    private void applyUserAddedDefinitions(CompilerDefinitions particular) {
        {
            List<String> systemPreprocessorSymbols = getSystemPreprocessorSymbols();
            if (systemPreprocessorSymbols instanceof CompilerDefinition) {
                for (int i : CompilerDefinitionAccessor.get().getUserAddedDefinitions((CompilerDefinition) systemPreprocessorSymbols)) {
                    addUniqueOrReplace(particular.systemPreprocessorSymbolsList, systemPreprocessorSymbols.get(i));
                }
            }
        }
        {
            List<String> systemIncludeDirectories = getSystemIncludeDirectories();
            if (systemIncludeDirectories instanceof CompilerDefinition) {
                for (int i : CompilerDefinitionAccessor.get().getUserAddedDefinitions((CompilerDefinition) systemIncludeDirectories)) {
                    // TODO implement "merge" which inserts user's path in best place
                    addUnique(particular.systemIncludeDirectoriesList, systemIncludeDirectories.get(i));
                }
            }
        }
        {
            List<String> systemIncludeHeaders = getSystemIncludeHeaders();
            if (systemIncludeHeaders instanceof CompilerDefinition) {
                for (int i : CompilerDefinitionAccessor.get().getUserAddedDefinitions((CompilerDefinition) systemIncludeHeaders)) {
                    // TODO implement "merge" which inserts user's path in best place
                    addUnique(particular.systemIncludeHeadersList, systemIncludeHeaders.get(i));
                }
            }
        }
    }
    
    @Override
    public List<String> getSystemIncludeDirectories() {
        CompilerDefinitions cur = compilerDefinitions;
        if (cur == null) {
            cur = resetAndGetCompilerDefinitions();
        }
        return cur.systemIncludeDirectoriesList;
    }

    @Override
    public List<String> getSystemIncludeDirectories(String flags) {
        if (flags != null && !flags.isEmpty()) {
            CompilerDefinitions particular;
            synchronized (particularModel) {
                particular = particularModel.get(flags);
                if (particular == null) {
                    MyCallable<CompilerDefinitions> callable = getCallable();
                    particular = callable.call(flags);
                    particularModel.put(flags, particular);
                    if (particular.systemPreprocessorSymbolsList.size() > 6 && particular.exitCode == 0) {
                        applyUserAddedDefinitions(particular);
                    }
                }
            }
            if (particular.systemPreprocessorSymbolsList.size() > 6 && particular.exitCode == 0) {
                return particular.systemIncludeDirectoriesList;
            }
        }
        return getSystemIncludeDirectories();
    }

    @Override
    public List<String> getSystemIncludeHeaders() {
        CompilerDefinitions cur = compilerDefinitions;
        if (cur == null) {
            cur = resetAndGetCompilerDefinitions();
        }
        return cur.systemIncludeHeadersList;
    }

    @Override
    public List<String> getSystemIncludeHeaders(String flags) {
        if (flags != null && !flags.isEmpty()) {
            CompilerDefinitions particular;
            synchronized (particularModel) {
                particular = particularModel.get(flags);
                if (particular == null) {
                    MyCallable<CompilerDefinitions> callable = getCallable();
                    particular = callable.call(flags);
                    particularModel.put(flags, particular);
                    if (particular.systemPreprocessorSymbolsList.size() > 6 && particular.exitCode == 0) {
                        applyUserAddedDefinitions(particular);
                    }
                }
            }
            if (particular.systemPreprocessorSymbolsList.size() > 6 && particular.exitCode == 0) {
                return particular.systemIncludeHeadersList;
            }
        }
        return getSystemIncludeHeaders();
    }

    @Override
    public boolean isReady() {
        return compilerDefinitions != null;
    }

    @Override
    public void waitReady(boolean reset) {
        if (reset || !isReady()) {
            resetCompilerDefinitions();
        }
    }

    private CompilerDefinitions resetAndGetCompilerDefinitions() {
        CndUtils.assertNonUiThread();
        CompilerDefinitions res = getFreshCompilerDefinitions();
        compilerDefinitions = res;
        return res;
    }

    @Override
    public final void resetCompilerDefinitions(boolean lazy) {
        if (lazy) {
            compilerDefinitions = null;
        } else {
            resetAndGetCompilerDefinitions();
        }
    }

    
    @Override
    public void loadSettings(Preferences prefs, String prefix) {
        String version = prefs.get(CompilerSetPreferences.VERSION_KEY, "1.0"); // NOI18N
        {
            List<String> includeDirList = new ArrayList<String>();
            List<Integer> userAddedInclude = new ArrayList<Integer>();
            String includeDirPrefix = prefix + SYSTEM_INCLUDE_KEY;
            int includeDirCount = prefs.getInt(includeDirPrefix + LIST_COUNT_KEY, 0);
            for (int i = 0; i < includeDirCount; ++i) {
                String includeDir = prefs.get(includeDirPrefix + '.' + i, null); // NOI18N
                if (includeDir != null) {
                    if ("1.1".equals(version)) { // NOI18N
                        if (Utilities.isWindows()) {
                            includeDir = includeDir.replace('\\', '/'); // NOI18N
                        }
                        int start = includeDir.indexOf(NB69_VERSION_PATTERN);
                        if (start > 0) {
                            includeDir = includeDir.substring(start+NB69_VERSION_PATTERN.length());
                            int index = includeDir.indexOf('/'); // NOI18N
                            if (index > 0) {
                                includeDir = includeDir.substring(index);
                            }
                        }

                    }
                    includeDirList.add(includeDir);
                    String added = prefs.get(includeDirPrefix + USER_ADDED_KEY + i, null);
                    if ("true".equals(added)) { // NOI18N
                        userAddedInclude.add(includeDirList.size()-1);
                    }
                }
            }
            if (includeDirList.isEmpty()) {
                // try to load using the old way;  this might be removed at some moment in future
                List<String> oldIncludeDirList = PersistentList.restoreList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
                if (oldIncludeDirList != null) {
                    includeDirList.addAll(oldIncludeDirList);
                }
            }
            copySystemIncludeDirectories(includeDirList);
            if (!userAddedInclude.isEmpty()) {
                for(Integer i : userAddedInclude) {
                    compilerDefinitions.systemIncludeDirectoriesList.setUserAdded(true, i);
                }
            }
        }

        {
            List<String> includeHeaderList = new ArrayList<String>();
            List<Integer> userAddedIncludeHeaders = new ArrayList<Integer>();
            String includeHeaderPrefix = prefix + SYSTEM_INCLUDE_HEADER_KEY;
            int includeHeaderCount = prefs.getInt(includeHeaderPrefix + LIST_COUNT_KEY, 0);
            for (int i = 0; i < includeHeaderCount; ++i) {
                String includeHeader = prefs.get(includeHeaderPrefix + '.' + i, null); // NOI18N
                if (includeHeader != null) {
                    includeHeaderList.add(includeHeader);
                    String added = prefs.get(includeHeaderPrefix + USER_ADDED_KEY + i, null);
                    if ("true".equals(added)) { // NOI18N
                        userAddedIncludeHeaders.add(includeHeaderList.size()-1);
                    }
                }
            }
            copySystemIncludeHeaders(includeHeaderList);
            if (!userAddedIncludeHeaders.isEmpty()) {
                for(Integer i : userAddedIncludeHeaders) {
                    compilerDefinitions.systemIncludeHeadersList.setUserAdded(true, i);
                }
            }
        }

        {
            List<String> preprocSymbolList = new ArrayList<String>();
            List<Integer> userAddedpreprocSymbol = new ArrayList<Integer>();
            String preprocSymbolPrefix = prefix + SYSTEM_MACROS_KEY;
            int preprocSymbolCount = prefs.getInt(preprocSymbolPrefix + LIST_COUNT_KEY, 0);
            for (int i = 0; i < preprocSymbolCount; ++i) {
                String preprocSymbol = prefs.get(preprocSymbolPrefix + '.' + i, null); // NOI18N
                if (preprocSymbol != null) {
                    preprocSymbolList.add(preprocSymbol);
                    String added = prefs.get(preprocSymbolPrefix + USER_ADDED_KEY + i, null);
                    if ("true".equals(added)) { // NOI18N
                        userAddedpreprocSymbol.add(preprocSymbolList.size()-1);
                    }
                }
            }
            if (preprocSymbolList.isEmpty()) {
                // try to load using the old way;  this might be removed at some moment in future
                List<String> oldPreprocSymbolList = PersistentList.restoreList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
                if (oldPreprocSymbolList != null) {
                    preprocSymbolList.addAll(oldPreprocSymbolList);
                }
            }
            copySystemPreprocessorSymbols(preprocSymbolList);
            if (!userAddedpreprocSymbol.isEmpty()) {
                for(Integer i : userAddedpreprocSymbol) {
                    compilerDefinitions.systemPreprocessorSymbolsList.setUserAdded(true, i);
                }
            }
        }
    }

    @Override
    public void saveSettings(Preferences prefs, String prefix) {
        {
            List<String> includeDirList = getSystemIncludeDirectories();
            String includeDirPrefix = prefix + SYSTEM_INCLUDE_KEY;
            prefs.putInt(includeDirPrefix + LIST_COUNT_KEY, includeDirList.size());
            for (int i = 0; i < includeDirList.size(); ++i) {
                prefs.put(includeDirPrefix + '.' + i, includeDirList.get(i)); // NOI18N
                if (compilerDefinitions.systemIncludeDirectoriesList.isUserAdded(i)) {
                    prefs.put(includeDirPrefix + USER_ADDED_KEY + i, "true"); // NOI18N
                }
            }
        }
        {
            List<String> includeHeaderList = getSystemIncludeHeaders();
            String includeHeaderPrefix = prefix + SYSTEM_INCLUDE_HEADER_KEY;
            prefs.putInt(includeHeaderPrefix + LIST_COUNT_KEY, includeHeaderList.size());
            for (int i = 0; i < includeHeaderList.size(); ++i) {
                prefs.put(includeHeaderPrefix + '.' + i, includeHeaderList.get(i)); // NOI18N
                if (compilerDefinitions.systemIncludeHeadersList.isUserAdded(i)) {
                    prefs.put(includeHeaderPrefix + USER_ADDED_KEY + i, "true"); // NOI18N
                }
            }
        }
        {
            List<String> preprocSymbolList = getSystemPreprocessorSymbols();
            String preprocSymbolPrefix = prefix + SYSTEM_MACROS_KEY;
            prefs.putInt(preprocSymbolPrefix + LIST_COUNT_KEY, preprocSymbolList.size());
            for (int i = 0; i < preprocSymbolList.size(); ++i) {
                prefs.put(preprocSymbolPrefix + '.' + i, preprocSymbolList.get(i)); // NOI18N
                if (compilerDefinitions.systemPreprocessorSymbolsList.isUserAdded(i)) {
                    prefs.put(preprocSymbolPrefix + USER_ADDED_KEY + i, "true"); // NOI18N
                }
            }
        }
    }

    protected final void getSystemIncludesAndDefines(String arguments, final boolean stdout, CompilerDefinitions pair) throws IOException {
        String compilerPath = getPath();
        if (compilerPath == null || compilerPath.length() == 0) {
            return;
        }
        ExecutionEnvironment execEnv = getExecutionEnvironment();
        NativeProcess startedProcess = null;
        Task errorTask = null;
        try {
            if (execEnv.isLocal() && Utilities.isWindows()) {
                compilerPath = LinkSupport.resolveWindowsLink(compilerPath);
            }
            try {
                if (!HostInfoUtils.fileExists(execEnv, compilerPath)) {
                    compilerPath = getDefaultPath();
                    if (!HostInfoUtils.fileExists(execEnv, compilerPath)) {
                        return;
                    }
                }
            } catch (Throwable ex) {
                return;
            }

            List<String> argsList = new ArrayList<String>();
            argsList.addAll(Arrays.asList(arguments.trim().split(" +"))); // NOI18N
            argsList.add(getEmptyFile(execEnv));

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(compilerPath);
            npb.setArguments(argsList.toArray(new String[argsList.size()]));
            npb.getEnvironment().prependPathVariable("PATH", ToolUtils.getDirName(compilerPath)); // NOI18N
            
            final NativeProcess process = npb.call();
            startedProcess = process;
            if (process.getState() != State.ERROR) {
                InputStream stream = stdout? process.getInputStream() : process.getErrorStream();
                errorTask = RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (stdout) {
                                ProcessUtils.readProcessError(process);
                            } else {
                                ProcessUtils.readProcessOutput(process);
                            }
                        } catch (Throwable ex) {
                        }
                    }
                });
                
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    try {
                        parseCompilerOutput(reader, pair);
                    } finally {
                        reader.close();
                    }
                }
                process.waitFor();
                pair.exitCode = process.exitValue();
                startedProcess = null;
                errorTask = null;
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            throw new IOException(ex);
        } finally {
            if (errorTask != null){
                errorTask.cancel();
            }
            if (startedProcess != null) {
                startedProcess.destroy();
            }
        }
    }

    // To be overridden
    protected abstract void parseCompilerOutput(BufferedReader reader, CompilerDefinitions pair);

    protected abstract CompilerDefinitions getFreshCompilerDefinitions();

    protected String getDefaultPath() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getNames().length > 0){
            return compiler.getNames()[0];
        }
        return ""; // NOI18N
    }

    /**
     * Determines whether the given macro presents in the list
     * @param macrosList list of macros strings (in the form "macro=value" or just "macro")
     * @param macroToFind the name of the macro to search for
     * @return true if macro with the given name is found, otherwise false
     */
    protected boolean containsMacro(List<String> macrosList, String macroToFind) {
	int len = macroToFind.length();
	for (Iterator<String> it = macrosList.iterator(); it.hasNext();) {
	    String macro = it.next();
	    if (macro.startsWith(macroToFind) ) {
		if( macro.length() == len ) {
		    return true; // they are just equal
		}
		if( macro.charAt(len) == '=' ) {
		    return true; // it presents in the form macro=value
		}
	    }
	}
	return false;
    }

    static void parseUserMacros(final String line, final List<String> preprocessorList) {
        List<String> list = scanCommandLine(line);
        for(String s : list) {
            if (s.startsWith("\"") && s.endsWith("\"") || // NOI18N
                s.startsWith("'") && s.endsWith("'")) { // NOI18N
                if (s.length() > 2) {
                    s = s.substring(1, s.length()-1).trim();
                }
            }
            if (s.startsWith("-D")) { // NOI18N
                String token = s.substring(2);
                if (token.length() > 0) {
                    String name = token;
                    int i = token.indexOf('=');
                    if (i >= 0) {
                        name = token.substring(0,i);
                    }
                    if (isValidMacroName(name)) {
                        addUnique(preprocessorList, token);
                    }
                }
            }
        }
    }

    static boolean isValidMacroName(String macroName) {
        boolean par = false;
        for (int i = 0; i < macroName.length(); i++) {
            char c = macroName.charAt(i);
            if (c == '_') {
                continue;
            } else if (c >= 'A' && c <= 'Z') {
                continue;
            } else if (c >= 'a' && c <= 'z') {
                continue;
            } else if (c >= '0' && c <= '9' && i > 0) {
                continue;
            } else if (c == '(' && i > 0) {
                if (par) {
                    return false;
                }
                par = true;
            } else if (c == ')') {
                if (!par) {
                    return false;
                }
                return i == macroName.length() - 1;
            } else if (c == ' ' || c == ',' || c == '.') {
                if (!par) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
    
    static String[] getMacro(String line) {
        int sepIdx = -1; // index of space separating macro name and body
        int parCount = 0; // parenthesis counter
        loop:
        for (int i = 0; i < line.length(); ++i) {
            switch (line.charAt(i)) {
                case '(':
                    ++parCount;
                    break;
                case ')':
                    --parCount;
                    break;
                case ' ':
                    if (parCount == 0) {
                        sepIdx = i;
                        break loop;
                    }
            }
        }
        if (sepIdx > 0) {
            return new String[] {line.substring(0, sepIdx),line.substring(sepIdx + 1).trim()};
        } else {
            return new String[] {line, null};
        }
    }
    
    private static List<String> scanCommandLine(String line){
        List<String> res = new ArrayList<String>();
        int i = 0;
        StringBuilder current = new StringBuilder();
        boolean isSingleQuoteMode = false;
        boolean isDoubleQuoteMode = false;
        while (i < line.length()) {
            char c = line.charAt(i);
            i++;
            switch (c){
                case '\'': // NOI18N
                    if (isSingleQuoteMode) {
                        isSingleQuoteMode = false;
                    } else if (!isDoubleQuoteMode) {
                        isSingleQuoteMode = true;
                    }
                    current.append(c);
                    break;
                case '\"': // NOI18N
                    if (isDoubleQuoteMode) {
                        isDoubleQuoteMode = false;
                    } else if (!isSingleQuoteMode) {
                        isDoubleQuoteMode = true;
                    }
                    current.append(c);
                    break;
                case ' ': // NOI18N
                case '\t': // NOI18N
                case '\n': // NOI18N
                case '\r': // NOI18N
                    if (isSingleQuoteMode || isDoubleQuoteMode) {
                        current.append(c);
                        break;
                    } else {
                        if (current.length()>0) {
                            res.add(current.toString());
                            current.setLength(0);
                        }
                    }
                    break;
                default:
                    current.append(c);
                    break;
            }
        }
        if (current.length()>0) {
            res.add(current.toString());
        }
        return res;
    }

    private String getEmptyFile(ExecutionEnvironment execEnv) {
        if (execEnv.isLocal() && Utilities.isWindows()) {
            // no /dev/null on Windows, so we need a real file
            if (emptyFile == null) {
                try {
                    File tmpFile = File.createTempFile("xyz", ".c"); // NOI18N
                    tmpFile.deleteOnExit();
                    emptyFile = tmpFile;
                } catch (IOException ioe) {
                }
            }
            return emptyFile == null? DEV_NULL : emptyFile.getAbsolutePath();
        } else {
            return DEV_NULL;
        }
    }

    protected String getUniqueID() {
        if (getCompilerSet() == null || getCompilerSet().isAutoGenerated()) {
            return getClass().getName() +
                    ExecutionEnvironmentFactory.toUniqueID(getExecutionEnvironment()).hashCode() + getPath().hashCode() + "."; // NOI18N
        } else {
            return getClass().getName() + getCompilerSet().getName() +
                    ExecutionEnvironmentFactory.toUniqueID(getExecutionEnvironment()).hashCode() + getPath().hashCode() + "."; // NOI18N
        }
    }

    protected static void addUnique(List<String> list, String element) {
        String pattern = element;
        if (element.indexOf('=') > 0) {
            pattern = pattern.substring(0, element.indexOf('='));
        }
        for(String s : list) {
            if (s.indexOf('=') > 0) {
                if (pattern.equals(s.substring(0, s.indexOf('=')))) {
                    return;
                }
            } else {
                if (pattern.equals(s)) {
                    return;
                }
            }
        }
        list.add(element);
    }

    protected static void addUniqueOrReplace(List<String> list, String element) {
        String pattern = element;
        if (element.indexOf('=') > 0) {
            pattern = pattern.substring(0, element.indexOf('='));
        }
        for(int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s.indexOf('=') > 0) {
                if (pattern.equals(s.substring(0, s.indexOf('=')))) {
                    list.set(i, element);
                    return;
                }
            } else {
                if (pattern.equals(s)) {
                    list.set(i, element);
                    return;
                }
            }
        }
        list.add(element);
    }
    
    protected static void removeUnique(List<String> list, String element) {
        for(int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s.startsWith(element)) {
                if (s.length() > element.length() && s.charAt(element.length())=='=' ||
                    s.contains(element)) {
                    list.remove(i);
                    break;
                }
            }
        }
    }
    
    private List<String> predefinedMacrosByFlags(List<String> macrosList, String flags) {
        final CompilerDescriptor descriptor = getDescriptor();
        if (descriptor != null && flags != null && !flags.isEmpty()) {
            final List<PredefinedMacro> predefinedMacros = descriptor.getPredefinedMacros();
            if (predefinedMacros != null) {
                List<String> res = null;
                for(String flag : flags.split(" ")) { // NOI18N
                    if (flag.startsWith("-")) { // NOI18N
                        for(ToolchainManager.PredefinedMacro macro : predefinedMacros) {
                            if (flag.equals(macro.getFlags())) {
                                if (res == null) {
                                    res = new ArrayList<String>(macrosList);
                                }
                                if (macro.isHidden()) {
                                    // remove macro
                                    removeUnique(res, macro.getMacro());
                                } else {
                                    // add macro
                                    addUniqueOrReplace(res, macro.getMacro());
                                }
                            }
                        }
                    }
                }
                if (res != null) {
                    return res;
                }
            }
        }
        return macrosList;
    }
    
    protected void completePredefinedMacros(CompilerDefinitions pair) {
        final CompilerDescriptor descriptor = getDescriptor();
        if (descriptor != null) {
            final List<PredefinedMacro> predefinedMacros = descriptor.getPredefinedMacros();
            if (predefinedMacros != null) {
                for(ToolchainManager.PredefinedMacro macro : predefinedMacros) {
                    if (macro.getFlags() == null) {
                        if (macro.isHidden()) {
                            // remove macro
                            removeUnique(pair.systemPreprocessorSymbolsList, macro.getMacro());
                        } else {
                            // add macro
                            addUnique(pair.systemPreprocessorSymbolsList, macro.getMacro());
                        }
                    }
                }
            }
        }
    }
    
    //For testing. Compare compiler macros definition with real mactos privided by compiler.
    protected void checkModel(CompilerDefinitions res, MyCallable<CompilerDefinitions> get) {
        if (!LOG.isLoggable(Level.FINE)) {
            return;
        }
        final CompilerDescriptor descriptor = getDescriptor();
        if (descriptor == null) {
            return;
        }
        final List<PredefinedMacro> predefinedMacros = descriptor.getPredefinedMacros();
        if (predefinedMacros == null || predefinedMacros.isEmpty()) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("Compiler: ").append(getPath()); // NOI18N
        StringBuilder toolChainPatch = new StringBuilder();
        toolChainPatch.append("Proposed patch for compiler: ").append(getPath()); // NOI18N
        List<String> importantFlagsList = new ArrayList<String>();
        StringBuilder importantFlags = new StringBuilder();
        importantFlags.append("Important flags for compiler: ").append(getPath()).append("\n"); // NOI18N
        Set<String> checked = new HashSet<String>();
        List<String> allFlags =  new ArrayList<String>();
        for (PredefinedMacro macro : predefinedMacros) {
            if (macro.getFlags() != null && !checked.contains(macro.getFlags())) {
                allFlags.add(macro.getFlags());
                checked.add(macro.getFlags());
            }
        }
        List<String> undefinedAlternatives = new ArrayList<String>();
        if (getPath().endsWith("/CC") || getPath().endsWith("/cc")) { // NOI18N
            List<String> flags = new ArrayList<String>();
            getCompilerOutput(" -flags", flags, undefinedAlternatives, false); // NOI18N
            for(final String flag : flags) {
                if (!checked.contains(flag)) {
                    allFlags.add(flag);
                    checked.add(flag);
                }
            }
        } else { //if (getPath().endsWith("/g++") || getPath().endsWith("/gcc")) { // NOI18N
            // suppose that compiler supports help as GNU compiler
            List<String> flags = new ArrayList<String>();
            getCompilerOutput("-v --help", flags, undefinedAlternatives, true); // NOI18N
            for(final String flag : flags) {
                if (!checked.contains(flag)) {
                    allFlags.add(flag);
                    checked.add(flag);
                }
            }
        }
        Collections.sort(allFlags);
        for (String flag : allFlags) {
            CompilerDefinitions tmp = get.call(flag);
            if (tmp.systemPreprocessorSymbolsList.size() <= 6 || tmp.exitCode != 0) {
                if (LOG.isLoggable(Level.FINER)) {
                    buf.append("\nThe flag ").append(flag).append(" is not supported. Exit code "+tmp.exitCode); // NOI18N
                }
                continue;
            }
            FlagModel flagModel = new FlagModel(flag);
            flagModel.diff(res, tmp);
            List<String> expectedDiff = new ArrayList<String>();
            List<String> expectedRm = new ArrayList<String>();
            for (PredefinedMacro m : predefinedMacros) {
                if (m.getFlags() != null && m.getFlags().equals(flag)) {
                    if (m.isHidden()) {
                        expectedRm.add(m.getMacro());
                    } else {
                        expectedDiff.add(m.getMacro());
                    }
                }
            }
            if (!flagModel.added.isEmpty() || !flagModel.changed.isEmpty() || !flagModel.removed.isEmpty()) {
                importantFlags.append(flagModel.flag).append(";"); // NOI18N
                importantFlagsList.add(flagModel.flag);
                toolChainPatch.append("\n"); // NOI18N
                if (!flagModel.added.isEmpty()) {
                    for (String t : flagModel.added) {
                        toolChainPatch.append("\n            <macro stringvalue=\"").append(t) // NOI18N
                                      .append("\" flags=\"").append(flagModel.flag).append("\"/>"); // NOI18N
                    }
                }
                if (!flagModel.changed.isEmpty()) {
                    for (String t : flagModel.changed) {
                        toolChainPatch.append("\n            <macro stringvalue=\"").append(t) // NOI18N
                                      .append("\" flags=\"").append(flagModel.flag).append("\"/>"); // NOI18N
                    }
                }
                if (!flagModel.removed.isEmpty()) {
                    for (String t : flagModel.removed) {
                        toolChainPatch.append("\n            <macro stringvalue=\"").append(t).append("\" flags=\"").append(flagModel.flag).append("\" hide=\"true\"/>"); // NOI18N
                    }
                }
            } else {
                if (flagModel.changedPaths) {
                    importantFlags.append(flagModel.flag).append(";"); // NOI18N
                    importantFlagsList.add(flagModel.flag);
                }
            }
            if (flagModel.changedPaths) {
                if (LOG.isLoggable(Level.FINER)) {
                    buf.append("\nThe flag ").append(flag).append(" changes predefined include paths"); // NOI18N
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                if (!flagModel.added.isEmpty() || !flagModel.changed.isEmpty() || !expectedDiff.isEmpty() || !flagModel.removed.isEmpty() || !expectedRm.isEmpty()) {
                    buf.append("\nThe flag ").append(flag); // NOI18N
                    if (!flagModel.added.isEmpty() || !flagModel.changed.isEmpty() || !expectedDiff.isEmpty()) {
                        if (!flagModel.added.isEmpty()) {
                            buf.append("\n\tadds predefined macros:"); // NOI18N
                            for (String t : flagModel.added) {
                                buf.append("\n\t\t").append(t); // NOI18N
                            }
                        }
                        if (!flagModel.changed.isEmpty()) {
                            buf.append("\n\tchanges predefined macros:"); // NOI18N
                            for (String t : flagModel.changed) {
                                buf.append("\n\t\t").append(t); // NOI18N
                            }
                        }
                        buf.append("\n\tby tool collection descriptor:"); // NOI18N
                        for (String t : expectedDiff) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                    }
                    if (!flagModel.removed.isEmpty() || !expectedRm.isEmpty()) {
                        buf.append("\n\tremoves predefined macros:"); // NOI18N
                        for (String t : flagModel.removed) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                        buf.append("\n\tby tool collection descriptor:"); // NOI18N
                        for (String t : expectedRm) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                    }
                } else {
                    buf.append("\nNo changes for flag ").append(flag); // NOI18N
                }
            }
        }
        LOG.log(Level.FINE, buf.toString());
        LOG.log(Level.FINE, toolChainPatch.toString());
        LOG.log(Level.FINE, importantFlags.toString());
        importantFlags.setLength(0);
        importantFlags.append("Important flags pattern for compiler: ").append(getPath()).append("\n"); // NOI18N
        importantFlags.append("        <important_flags flags=\"").append(convertToRegularExpression(importantFlagsList)).append("\"/>\n"); // NOI18N
        importantFlags.append("Undefined alternatives:\n"); // NOI18N
        for(String s :undefinedAlternatives) {
            importantFlags.append("|"+s+".*"); // NOI18N
        }
        LOG.log(Level.FINE, importantFlags.toString());
    }

    protected static String convertToRegularExpression(List<String> flags) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        String lastGroup = null;
        while(true) {
            if (i >= flags.size()) {
                break;
            }
            String current = flags.get(i);
            int eq = current.indexOf('=');
            if (eq < 0) {
                if (lastGroup != null) {
                    if (buf.length() > 0) {
                        buf.append('|');
                    }
                    buf.append(lastGroup).append(".*"); // NOI18N
                    lastGroup = null;
                }
                if (buf.length() > 0) {
                    buf.append('|');
                }
                if (i+1 < flags.size()) {
                    String next = flags.get(i+1);
                    if (next.startsWith(current)) {
                        current = current+"(\\W|$|-)"; // NOI18N
                        buf.append(current);
                        i++;
                        continue;
                    }
                }
                buf.append(current);
                i++;
            } else {
                String candidate = current.substring(0, eq+1);
                if (lastGroup != null) {
                    if (lastGroup.equals(candidate)) {
                        i++;
                        continue;
                    } else {
                        if (buf.length() > 0) {
                            buf.append('|');
                        }
                        buf.append(lastGroup).append(".*"); // NOI18N
                    }
                }
                lastGroup = candidate;
                i++;
            }
        }
        if (lastGroup != null) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(lastGroup).append(".*"); // NOI18N
        }
        return buf.toString();
    }
    
    //For testing. Run compiler to obtain flags help.
    private void getCompilerOutput(String arguments, List<String> options, List<String> undefinedAlternatives, boolean isGcc) {
        String compilerPath = getPath();
        if (compilerPath == null || compilerPath.length() == 0) {
            return;
        }
        ExecutionEnvironment execEnv = getExecutionEnvironment();
        if (execEnv.isLocal() && Utilities.isWindows()) {
            compilerPath = LinkSupport.resolveWindowsLink(compilerPath);
        }
        try {
            if (!HostInfoUtils.fileExists(execEnv, compilerPath)) {
                compilerPath = getDefaultPath();
                if (!HostInfoUtils.fileExists(execEnv, compilerPath)) {
                    return;
                }
            }
        } catch (Throwable ex) {
            return;
        }

        List<String> argsList = new ArrayList<String>();
        argsList.addAll(Arrays.asList(arguments.trim().split(" +"))); // NOI18N
        ProcessUtils.ExitStatus execute = ProcessUtils.execute(execEnv, compilerPath, argsList.toArray(new String[argsList.size()]));
        // GNU 4.9.2 returns exit code 1
        //if (execute.isOK()) {
            discoverFlags(execute.getOutputString(), options, undefinedAlternatives, isGcc);
        //}
    }

    //For testing. Discover compiler flags from compiler help output.
    protected static void discoverFlags(String output, List<String> options, List<String> undefinedAlternatives, boolean isGcc) {
        String[] split = output.split("\n"); // NOI18N
        for(int index = 0; index < split.length; index++) {
            String line = split[index];
            String s = line.trim();
            if (s.startsWith("-") && !s.startsWith("--")) { // NOI18N
                final String[] splitOption = s.split(" "); // NOI18N
                if (splitOption.length > 1 && splitOption[1].startsWith("<")) { // NOI18N
                    continue;
                }
                String option = splitOption[0];
                if (isGcc) {
                    if (option.indexOf("<") >= 0) { // NOI18N
                        int i = option.indexOf("<"); // NOI18N
                        int j = option.indexOf(">"); // NOI18N
                        if (j > i) {
                            String alternatives = option.substring(i+1, j);
                            if (alternatives.indexOf("|")>0) { // NOI18N
                                final String[] splitAlternatives = alternatives.split("\\|"); // NOI18N
                                if (splitAlternatives.length > 1) {
                                    for(String alternative : splitAlternatives) {
                                        options.add(option.substring(0,i)+alternative);
                                    }
                                }
                            } else {
                                if ("-O".equals(option.substring(0,i))) { // NOI18N
                                    for(int n = 0; n < 6; n++) {
                                        options.add(option.substring(0,i)+n);
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    if (option.indexOf("[") >= 0) { // NOI18N
                        int i = option.indexOf("["); // NOI18N
                        int j = option.indexOf("]"); // NOI18N
                        if (j > i) {
                            String alternatives = option.substring(i+1, j);
                            final String[] splitAlternatives = alternatives.split("\\|"); // NOI18N
                            if (splitAlternatives.length > 1) {
                                for(String alternative : splitAlternatives) {
                                    options.add(option.substring(0,i)+alternative);
                                }
                                continue;
                            }
                            option = option.substring(0, i)+option.substring(j+1);
                        } else {
                            continue;
                        }
                    }
                    if (option.indexOf("=CPU") > 0) { // NOI18N
                        if (index +1 < split.length)
                        if (line.indexOf("CPU is one of:")>0 || (index +1 < split.length && split[index +1].indexOf("CPU is one of:")>0)) { // NOI18N
                            List<String> CPUTypes = new ArrayList<String>();
                            int shift = line.indexOf("CPU is one of:")>0 ? 1 : 2; // NOI18N
                            for(int lineNumber = index+shift; index < split.length; lineNumber++) {
                                String current = split[lineNumber];
                                current = current.trim();
                                if (current.startsWith("-")) { // NOI18N
                                    break;
                                }
                                for(String variant : current.split("\\,")) { // NOI18N
                                    variant = variant.trim();
                                    if (!variant.isEmpty() && variant.indexOf(" ") < 0) { // NOI18N
                                        CPUTypes.add(variant);
                                    }
                                }
                                if (!current.endsWith(",")) { // NOI18N
                                    break;
                                }
                            }
                            if (CPUTypes.size()>0) {
                                int start = option.indexOf("=CPU"); // NOI18N
                                for(String type : CPUTypes) {
                                    options.add(option.substring(0,start+1)+type);
                                }
                                continue;
                            }
                        }
                    }
                    options.add(option);
                } else {
                    option = option.replace("[,<a>]", ""); // NOI18N
                    if (option.indexOf("<") >= 0) { // NOI18N
                        int i = option.indexOf("<"); // NOI18N
                        int j = option.indexOf(">"); // NOI18N
                        if (j > i) {
                            String subtitute = option.substring(i, j+1);
                            boolean found = false;
                            for(int k= 1; k < splitOption.length; k++) {
                                if (splitOption[k].startsWith(subtitute+"=")) { // NOI18N
                                    String def = splitOption[k].substring(subtitute.length()+1);
                                    if (def.startsWith("{")) { // NOI18N
                                        if (!def.endsWith("}")) { // NOI18N
                                            for(int d = k+1; d < splitOption.length; d++) {
                                                def = def+" "+splitOption[d]; // NOI18N
                                                if (def.endsWith("}")) { // NOI18N
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    option = option.substring(0,i)+def+option.substring(j+1);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found && index+1 < split.length && !split[index+1].trim().startsWith("-")) { // NOI18N
                                String[] nextLine = split[index+1].trim().split(" "); // NOI18N
                                for(int k= 0; k < nextLine.length; k++) {
                                    if (nextLine[k].startsWith(subtitute+"=")) { // NOI18N
                                        option = option.substring(0,i)+nextLine[k].substring(subtitute.length()+1)+option.substring(j+1);
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                if ("-O".equals(option.substring(0,i))) { // NOI18N
                                    for(int n = 0; n < 6; n++) {
                                        options.add(option.substring(0,i)+n);
                                    }
                                    continue;
                                }
                            }
                        }
                    }
                    
                    if (option.indexOf("[") >= 0) { // NOI18N
                        int i = option.indexOf("["); // NOI18N
                        int j = option.lastIndexOf("]"); // NOI18N
                        if (j > i && option.substring(0,i).indexOf("<") < 0) { // NOI18N
                            options.add(option.substring(0,i));
                            String alternatives = option.substring(i+1, j);
                            if (alternatives.indexOf("{") < 0 && alternatives.indexOf("<") < 0) { // NOI18N
                                if (alternatives.indexOf("|")>0) { // NOI18N
                                    final String[] splitAlternatives = alternatives.split("\\|"); // NOI18N
                                    if (splitAlternatives.length > 1) {
                                        for(String alternative : splitAlternatives) {
                                            if (alternative.startsWith("<")) { // NOI18N
                                                undefinedAlternatives.add(option.substring(0,i)+alternative);
                                            } else {
                                                options.add(option.substring(0,i)+alternative);
                                            }
                                        }
                                    }
                                    continue;
                                } else if (alternatives.indexOf(",")>0) { // NOI18N
                                    final String[] splitAlternatives = alternatives.split(","); // NOI18N
                                    if (splitAlternatives.length > 1) {
                                        for(String alternative : splitAlternatives) {
                                            if (alternative.startsWith("<")) { // NOI18N
                                                undefinedAlternatives.add(option.substring(0,i)+alternative);
                                            } else {
                                                options.add(option.substring(0,i)+alternative);
                                            }
                                        }
                                    }
                                    continue;
                                }
                            }
                            option = option.substring(0,i)+option.substring(i+1, j);
                        } else {
                            continue;
                        }
                    }
                    if (option.indexOf("{") >= 0) { // NOI18N
                        int i = option.indexOf("{"); // NOI18N
                        int j = option.lastIndexOf("}"); // NOI18N
                        if (j > i) {
                            String alternatives = option.substring(i+1, j);
                            if (alternatives.indexOf("|")>0) { // NOI18N
                                final String[] splitAlternatives = alternatives.split("\\|"); // NOI18N
                                if (splitAlternatives.length > 1) {
                                    for(String alternative : splitAlternatives) {
                                        if (alternative.startsWith("<")) { // NOI18N
                                            undefinedAlternatives.add(option.substring(0,i)+alternative);
                                        } else {
                                            options.add(option.substring(0,i)+alternative);
                                        }
                                    }
                                }
                            } else if (alternatives.indexOf(",")>0) { // NOI18N
                                final String[] splitAlternatives = alternatives.split(","); // NOI18N
                                if (splitAlternatives.length > 1) {
                                    for(String alternative : splitAlternatives) {
                                        if (alternative.startsWith("<")) { // NOI18N
                                            undefinedAlternatives.add(option.substring(0,i)+alternative);
                                        } else {
                                            options.add(option.substring(0,i)+alternative);
                                        }
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    if (option.indexOf("[") >= 0) { // NOI18N
                        int i = option.indexOf("["); // NOI18N
                        int j = option.indexOf("]"); // NOI18N
                        if (j > i) {
                            String alternatives = option.substring(i+1, j);
                            final String[] splitAlternatives = alternatives.split("\\|"); // NOI18N
                            if (splitAlternatives.length > 1) {
                                for(String alternative : splitAlternatives) {
                                    options.add(option.substring(0,i)+alternative);
                                }
                            }
                        }
                        continue;
                    }
                    if (option.indexOf("<") >= 0) { // NOI18N
                        //Do not know alternatives, add as important
                        undefinedAlternatives.add(option.substring(0,option.indexOf("<")));  // NOI18N
                        continue;
                    }
                    options.add(option);
                }
            }
        }
    }
   
    protected abstract MyCallable<CompilerDefinitions> getCallable();
    
    protected static final class CompilerDefinitions {
        public CompilerDefinition systemIncludeDirectoriesList;
        public CompilerDefinition systemPreprocessorSymbolsList;
        public CompilerDefinition systemIncludeHeadersList;
        public int exitCode;
        public CompilerDefinitions(){
            systemIncludeDirectoriesList = new CompilerDefinition(0);
            systemPreprocessorSymbolsList = new CompilerDefinition(0);
            systemIncludeHeadersList = new CompilerDefinition(0);
            exitCode = 0;
        }

    }
    
    
    protected interface MyCallable<V>{
        V call(String p);
    }
    
    protected static final class FlagModel {
        private final String flag;
        private final List<String> added;
        private final List<String> changed;
        private final List<String> removed;
        private boolean changedPaths;
        
        private FlagModel(String flag) {
            this.flag = flag;
            added = new ArrayList<String>();
            changed = new ArrayList<String>();
            removed = new ArrayList<String>();
        }
        private boolean isIgnored(String macro) {
            return macro.startsWith("__LINE__") || macro.startsWith("__FILE__") || macro.startsWith("__DATE__") || macro.startsWith("__TIME__"); // NOI18N
                
        }
        private void diff(CompilerDefinitions golden, CompilerDefinitions particular) {
            diffMacros(golden, particular);
            diffPaths(golden, particular);
        }
        private void diffPaths(CompilerDefinitions golden, CompilerDefinitions particular) {
            {
                if (particular.systemIncludeDirectoriesList.size()==0) {
                    changedPaths = false;
                    return;
                }
                if (particular.systemIncludeDirectoriesList.size() != golden.systemIncludeDirectoriesList.size()) {
                    changedPaths = true;
                    return;
                }
                for(int i = 0; i < golden.systemIncludeDirectoriesList.size(); i++) {
                    String s1 = golden.systemIncludeDirectoriesList.get(i);
                    String s2 = particular.systemIncludeDirectoriesList.get(i);
                    if (!s1.equals(s2)) {
                        changedPaths = true;
                        return;
                    }
                }
            }
            {
                if (particular.systemIncludeHeadersList.size()==0) {
                    changedPaths = false;
                    return;
                }
                if (particular.systemIncludeHeadersList.size() != golden.systemIncludeHeadersList.size()) {
                    changedPaths = true;
                    return;
                }
                for(int i = 0; i < golden.systemIncludeHeadersList.size(); i++) {
                    String s1 = golden.systemIncludeHeadersList.get(i);
                    String s2 = particular.systemIncludeHeadersList.get(i);
                    if (!s1.equals(s2)) {
                        changedPaths = true;
                        return;
                    }
                }
            }
            changedPaths = false;
        }
        private void diffMacros(CompilerDefinitions golden, CompilerDefinitions particular) {
            for (String t : particular.systemPreprocessorSymbolsList) {
                String pattern = t;
                int i = t.indexOf('='); // NOI18N
                if (i > 0) {
                    pattern = pattern.substring(0, i);
                }
                String found = null;
                for (String s : golden.systemPreprocessorSymbolsList) {
                    i = s.indexOf('='); // NOI18N
                    if (i > 0) {
                        if (pattern.equals(s.substring(0, i))) {
                            found = s;
                            break;
                        }
                    } else {
                        if (pattern.equals(s)) {
                            found = s;
                            break;
                        }
                    }
                }
                if (found == null) {
                    if (!isIgnored(t)) {
                        added.add(t);
                    }
                } else {
                    if (!t.equals(found)) {
                        if (!isIgnored(t)) {
                            boolean skip = false;
                            if (t.endsWith("=1")) { // NOI18N
                                if (found.indexOf("=") < 0) { // NOI18N
                                    skip = true;
                                }
                            } else if (found.endsWith("=1")) { // NOI18N
                                if (t.indexOf("=") < 0) { // NOI18N
                                    skip = true;
                                }
                            }
                            if (!skip) {
                                changed.add(t);
                            }
                        }
                    }
                }
            }
            for (String t : golden.systemPreprocessorSymbolsList) {
                String pattern = t;
                int i = t.indexOf('='); // NOI18N
                if (i > 0) {
                    pattern = pattern.substring(0, i);
                }
                boolean found = false;
                for (String s : particular.systemPreprocessorSymbolsList) {
                    i = s.indexOf('='); // NOI18N
                    if (i > 0) {
                        if (pattern.equals(s.substring(0, i))) {
                            found = true;
                            break;
                        }
                    } else {
                        if (pattern.equals(s)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    if (!isIgnored(pattern)) {
                        removed.add(pattern);
                    }
                }
            }
        }
    }
}
