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
package org.netbeans.modules.maven.runjar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.customizer.RunJarPanel;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author sdedic
 */
public final class MavenExecuteUtils {
    /**
     * Hint for the default PrereqqCheckeers that explicit parameters have been already processed.
     * Will not be propagated to maven process.
     */
    public static final String RUN_EXPLICIT_PROCESSED = "NbIde.ExplicitParametersApplied"; // NOI18N
    
    /**
     * Name of the property for VM arguments.
     * @since 2.144
     */
    public static final String RUN_VM_PARAMS = "exec.vmArgs"; //NOI18N

    /**
     * Name of the property to pass main class. ${packageClassName} works as well.
     * @since 2.144
     */
    public static final String RUN_MAIN_CLASS = "exec.mainClass"; //NOI18N

    /**
     * Name of the property for application arguments.
     * @since 2.144
     */
    public static final String RUN_APP_PARAMS = "exec.appArgs"; //NOI18N

    /**
     * Name of the property that collects the entire command line.
     */
    public static final String RUN_PARAMS = "exec.args"; //NOI18N
    
    /**
     * Name of the property for working directory passed to th exec plugin
     */
    public static final String RUN_WORKDIR = "exec.workingdir"; //NOI18N
    
    private static final String RUN_VM_PARAMS_TOKEN = "${" + RUN_VM_PARAMS + "}"; //NOI18N
    private static final String RUN_APP_PARAMS_TOKEN = "${" + RUN_APP_PARAMS + "}"; //NOI18N
    private static final String RUN_MAIN_CLASS_TOKEN = "${" + RUN_MAIN_CLASS + "}"; //NOI18N
    static final String PACKAGE_CLASS_NAME_TOKEN = "${packageClassName}"; //NOI18N
    
    public static final String EXEC_ARGS_CLASSPATH_TOKEN = "-classpath %classpath"; // NOI18N
    public static final String DEFAULT_EXEC_ARGS_CLASSPATH = EXEC_ARGS_CLASSPATH_TOKEN + " ${packageClassName}"; // NOI18N
    static final String DEFAULT_DEBUG_PARAMS = "-agentlib:jdwp=transport=dt_socket,server=n,address=${jpda.address}"; //NOI18N
    static final String DEFAULT_EXEC_ARGS_CLASSPATH2 =  "${exec.vmArgs} -classpath %classpath ${exec.mainClass} ${exec.appArgs}"; // NOI18N

    public static final String ENV_PREFIX = "Env."; // NOI18N
    public static final String ENV_REMOVED = new String("null"); // A special instance for env vars to be removed. // NOI18N

    /**
     * ID of the 'profile' action.
     */
    public static final String PROFILE_CMD = "profile"; // NOI18N
    
    /**
     * Folder on config filesystem that contains "run" goal aliases. 
     */
    private static final String RUN_GOALS_CONFIG_ROOT = "Projects/org-netbeans-modules-maven/RunGoals";

    /**
     * A helper that can update action mappings based on changes
     * made on the helper instance. Use {@link #createExecutionEnvHelper}
     * to make an instance.
     */
    public static final class ExecutionEnvHelper {
        private final ActionToGoalMapping goalMappings;
        private final NbMavenProjectImpl project;
        
        private String oldAllParams;
        private String oldVmParams;
        private String oldAppParams;
        private String oldWorkDir;
        private String oldMainClass;
        
        private boolean currentRun;
        private boolean currentDebug;
        private boolean currentProfile;
        
        private String vmParams;
        private String appParams;
        private String workDir;
        private String mainClass;
        
        private NetbeansActionMapping run;
        private NetbeansActionMapping debug;
        private NetbeansActionMapping profile;

        private boolean mergedConfig;
        private boolean modified;

        ExecutionEnvHelper(
                NbMavenProjectImpl project,
                NetbeansActionMapping run,
                NetbeansActionMapping debug,
                NetbeansActionMapping profile,
                ActionToGoalMapping goalMappings) {
            this.project = project;
            this.goalMappings = goalMappings;
            this.run = run;
            this.debug = debug;
            this.profile = profile;
        }
        
        private String fallbackParams(String paramName, boolean stripDebug) {
            String val = run != null ? run.getProperties().get(paramName) : null;
            if (val == null && debug != null) {
                val = debug.getProperties().get(paramName);
                if (val != null && stripDebug) {
                    val = String.join(" ", extractDebugJVMOptions(val));
                }
            }
            if (val == null && profile != null) {
                val = profile.getProperties().get(paramName);
            }
            return val == null ? "" : val.trim(); // NOI18N
        }

        private String appendIfNotEmpty(String a, String b) {
            if (a == null || a.isEmpty()) {
                return b;
            }
            if (b == null || b.isEmpty()) {
                return a;
            }
            return a + " " + b;
        }

        public ActionToGoalMapping getGoalMappings() {
            return goalMappings;
        }

        public boolean isModified() {
            return modified;
        }

        public boolean isValid() {
            return currentRun && currentDebug && currentProfile;
        }

        public boolean isCurrentRun() {
            return currentRun;
        }

        public boolean isCurrentDebug() {
            return currentDebug;
        }

        public boolean isCurrentProfile() {
            return currentProfile;
        }
        
        public void setMainClass(String mainClass) {
            this.mainClass = mainClass;
        }

        public void setVmParams(String vmParams) {
            this.vmParams = vmParams;
        }

        public void setAppParams(String appParams) {
            this.appParams = appParams;
        }

        public NbMavenProjectImpl getProject() {
            return project;
        }

        public String getWorkDir() {
            return oldWorkDir;
        }

        public void setWorkDir(String workDir) {
            this.workDir = workDir;
        }

        public String getMainClass() {
            return mainClass;
        }

        public NetbeansActionMapping getRun() {
            return run;
        }

        public NetbeansActionMapping getProfile() {
            return profile;
        }

        public String getAllParams() {
            return oldAllParams;
        }

        public String getVmParams() {
            return vmParams;
        }

        public String getAppParams() {
            return appParams;
        }
        
        private NetbeansActionMapping getMapping(String a) {
            NetbeansActionMapping m = ActionToGoalUtils.getDefaultMapping(a, project);
            return m;
        }
        
        /**
         * Loads and parses values from the project's nbactions.xml
         */
        public void loadFromProject() {
            NetbeansActionMapping m;
            
            if (run == null) {
                run = getMapping(ActionProvider.COMMAND_RUN);
            }
            if (debug == null) {
                debug = getMapping(ActionProvider.COMMAND_DEBUG);
            }
            if (profile == null) {
                profile = getMapping(PROFILE_CMD);
            }
            
            currentRun = checkNewMapping(run);
            currentDebug = checkNewMapping(debug);
            currentProfile = checkNewMapping(profile);
            
            oldWorkDir = fallbackParams(RUN_WORKDIR, false);
            oldAllParams = fallbackParams(RUN_PARAMS, false);
            oldVmParams = fallbackParams(RUN_VM_PARAMS, true);
            oldAppParams = fallbackParams(RUN_APP_PARAMS, false);
            oldMainClass = fallbackParams(RUN_MAIN_CLASS, false);
            
            mergedConfig = (oldVmParams.isEmpty() && oldAppParams.isEmpty() && oldMainClass.isEmpty());
            
            appendVMParamsFromOldParams();
            addAppParamsFromOldParams();
            loadMainClass();
            
            workDir = oldWorkDir;
            vmParams = oldVmParams;
            appParams = oldAppParams;
            mainClass = oldMainClass;
        }
        
        private String eraseTokens(String original, boolean withNewlines, String... tokens) {
            StringBuilder sb = new StringBuilder();
            for (String p : tokens) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(Pattern.quote(p));
                if (withNewlines) {
                    sb.append("\\n?");
                }
            }
            return original.replaceAll(sb.toString(), "").trim();
        }
        
        private void appendVMParamsFromOldParams() {
            String oldSplitVMParams = splitJVMParams(oldAllParams, true);
            if (!oldSplitVMParams.isEmpty()) {
                // try to get VM arguments out of all exec.args, but ignore -classpath added automatically, and
                // exec.vmArgs present / added by default.
                oldSplitVMParams = eraseTokens(oldSplitVMParams, true, "-classpath %classpath", RUN_VM_PARAMS_TOKEN);
                oldVmParams = appendIfNotEmpty(oldVmParams, oldSplitVMParams);
            }
        }
        
        private void addAppParamsFromOldParams() {
            String p = splitParams(oldAllParams);
            if (!p.isEmpty()) {
                p = eraseTokens(p, false, RUN_APP_PARAMS_TOKEN);
                oldAppParams = appendIfNotEmpty(oldAppParams, p);
            }
        }
        
        private void loadMainClass() {
            if (oldMainClass.trim().isEmpty()) {
                oldMainClass = splitMainClass(oldAllParams);
                // splitMainClass is never null
            }
            if (PACKAGE_CLASS_NAME_TOKEN.equals(oldMainClass) || RUN_MAIN_CLASS_TOKEN.equals(oldMainClass)) {
                oldMainClass = "";
            }
        }

        private boolean checkNewMapping(NetbeansActionMapping map) {
            if (map == null || map.getGoals() == null) {
                return false; //#164323
            }
            if (map.getGoals().isEmpty()) {
                return true;
            }
            Iterator it = map.getGoals().iterator();
            FileObject goalRoot = FileUtil.getConfigFile(RUN_GOALS_CONFIG_ROOT); // NOI18N
            while (it.hasNext()) {
                String goal = (String) it.next();
                boolean goalFound = (goal.matches("org\\.codehaus\\.mojo\\:exec-maven-plugin\\:(.)+\\:exec") //NOI18N
                    || goal.contains("exec:exec")); // NOI18N
                if (!goalFound && goalRoot != null) {
                    int colon = goal.lastIndexOf(':');
                    if (colon != -1) {
                        String pluginId = goal.substring(0, colon);
                        String goalName = goal.substring(colon + 1);
                        String[] gav = pluginId.split(":");
                        
                        FileObject g = goalRoot.getFileObject(pluginId);
                        if (g == null && gav.length > 2) {
                            String justId = gav[0] + ":" + gav[1];
                            g = goalRoot.getFileObject(justId);
                        }
                        if (g != null) {
                            Object alias = g.getAttribute("alias");
                            if (alias instanceof String) {
                                try {
                                    URL u = new URL(URLMapper.findURL(g, URLMapper.INTERNAL), alias.toString());
                                    g = URLMapper.findFileObject(u);
                                } catch (MalformedURLException ex) {
                                    // expected
                                }
                            }
                        }
                        if (g != null) {
                            Object s = g.getAttribute("goals");
                            if (s instanceof String) {
                                goalFound = Arrays.asList(s.toString().split(" ")).contains(goalName);
                            }
                        }
                    }
                }
                if (goalFound) { //NOI18N
                    if (map.getProperties() != null) {
                        if (map.getProperties().containsKey("exec.args")) {
                            String execArgs = map.getProperties().get("exec.args");
                            if (execArgs.contains("-classpath")) {
                                return true;
                            }
                        }
                        if (map.getProperties().containsKey("exec.vmArgs")) {
                            String execArgs = map.getProperties().get("exec.vmArgs");
                            if (execArgs.contains("-classpath")) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        
        public void applyToMappings() {
            if (!(currentRun || currentDebug || currentProfile)) {
                return;
            }
            
            if (currentRun) {
                updateAction(run, "");
            }
            if (currentDebug) {
                updateAction(debug, DEFAULT_DEBUG_PARAMS);
            }
            if (currentProfile) {
                updateAction(profile, "");
            }
        }
        
        private void updateAction(NetbeansActionMapping mapping, String debuVMArgs) {
            boolean changed = false;
            // do not update for actiosn that have empty goals = are disabled.
            if (mapping.getGoals() == null || mapping.getGoals().isEmpty()) {
                return;
            }
            
            if (!oldWorkDir.equals(workDir)) {
                mapping.addProperty(RUN_WORKDIR, workDir);
                changed = true;
            }
            if (!oldAppParams.equals(appParams)) {
                mapping.addProperty(RUN_APP_PARAMS, appParams);
                changed = true;
            }
            String newMainClass = this.mainClass;
            if (newMainClass.trim().length() == 0) {
                newMainClass = PACKAGE_CLASS_NAME_TOKEN;
            }
            if (!oldMainClass.equals(newMainClass)) {
                mapping.addProperty(RUN_MAIN_CLASS, newMainClass);
                changed = true;
            }
            if (!workDir.equals(oldWorkDir)) {
                mapping.addProperty(RUN_WORKDIR, workDir);
                changed = true;
            }
            String oneLineVMParams = vmParams.replace('\n', ' ');
            String newVMParams = appendIfNotEmpty(oneLineVMParams, debuVMArgs);
            if (!oldVmParams.equals(newVMParams)) {
                mapping.addProperty(RUN_VM_PARAMS, newVMParams);
                changed = true;
            }
            
            if (changed) {
                // define the properties, if not defined ...
                Map<String, String> props = mapping.getProperties();
                if (mapping.getProperties().get(RUN_VM_PARAMS) == null) {
                    mapping.addProperty(RUN_VM_PARAMS, vmParams);
                }
                if (mapping.getProperties().get(RUN_APP_PARAMS) == null) {
                    mapping.addProperty(RUN_APP_PARAMS, appParams);
                }
                if (mapping.getProperties().get(RUN_MAIN_CLASS) == null) {
                    mapping.addProperty(RUN_MAIN_CLASS, newMainClass);
                }
                mapping.addProperty(RUN_PARAMS, 
                    "${exec.vmArgs} -classpath %classpath ${exec.mainClass} ${exec.appArgs}"
                );
            }
            
            if (changed) {
                ModelHandle2.setUserActionMapping(mapping, goalMappings);
                modified = true;
            }
        }
    }
    
    /**
     * Splits a command line, pays respect to quoting and newlines.
     * @param line original line
     * @return line split into individual arguments.
     */
    public static String[] splitCommandLine(String line) {
        if (line == null) {
            return new String[0];
        }
        String l = line.trim();
        if (l.isEmpty()) {
            return new String[0];
        }
        List<String> result = new ArrayList<>();
        for (String part : propertySplitter(l, true)) {
            result.add(part);
        }
        return result.toArray(new String[0]);
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
    
    /**
     * Checks that the mapping does not specify custom exec arguments. If
     * the exec.args is set to {@link #DEFAULT_EXEC_ARGS_CLASSPATH}, the 
     * `packageClassName' has not been set (= unspecified).
     * If the exec.args is set tp {@link #DEFAULT_EXEC_ARGS_CLASSPATH2}, then
     * none of the referenced properties can provide a value (the exec.args itself
     * is not changed by the IDE, just the referenced properties).
     * <p>
     * Other values of `exec.args' means user customizations.
     * <p>
     * Returns {@code null}, if there are user customizations. Returns the value of
     * exec.args (the default string) so the caller can retain the parameter
     * passing style.
     * 
     * @param mapp action mapping.
     * @return 
     */
    public static String doesNotSpecifyCustomExecArgs(NetbeansActionMapping  mapp) {
        return doesNotSpecifyCustomExecArgs(true, mapp.getProperties());
    }
    
    private static boolean equalsOrIncludes(boolean exact, String text, String toFind) {
        if (exact) {
            return text.equals(toFind);
        } else {
            return text.contains(toFind);
        }
    }
    
    /**
     * The inexact match is used from RunJarStartupArgs
     */
    public static String doesNotSpecifyCustomExecArgs(boolean exact, Map<? extends String, ? extends String> props) {
        String execArgs = props.get(RUN_PARAMS);
        String replacedMainClass = props.get(RUN_MAIN_CLASS);
        String template;
        boolean secondTry = replacedMainClass != null && execArgs.contains(PACKAGE_CLASS_NAME_TOKEN);
        
        template = DEFAULT_EXEC_ARGS_CLASSPATH;
        if (equalsOrIncludes(exact, execArgs, template)) {
            return template;
        }
        if (secondTry) {
            template = template.replace(PACKAGE_CLASS_NAME_TOKEN, replacedMainClass);
            if (equalsOrIncludes(exact, execArgs, template)) {
                return template;
            }
        }
        template = DEFAULT_EXEC_ARGS_CLASSPATH2;
        if (!equalsOrIncludes(exact, execArgs, template)) {
            if (!secondTry) {
                return null;
            }
            template = template.replace(PACKAGE_CLASS_NAME_TOKEN, replacedMainClass);
            if (!equalsOrIncludes(exact, execArgs, template)) {
                return null;
            }
        }

        if (!exact) {
            return template;
        }
        
        // none of the properties refrenced in DEFAULT_EXEC_ARGS_CLASSPATH2 is defined:
        if (isNullOrEmpty(props.get(RUN_APP_PARAMS)) && 
            isNullOrEmpty(props.get(RUN_VM_PARAMS))) {
            
            String mainClass = props.get(RUN_MAIN_CLASS);
            if (mainClass == null ||
                "".equals(mainClass) ||
                MavenExecuteUtils.PACKAGE_CLASS_NAME_TOKEN.equals(mainClass)) {
                return template;
            }
        }
        return null;
    }
    
    public static boolean isEnvRemovedValue(String value) {
        return value == ENV_REMOVED; // It's crutial to test the instance identity
    }
    
    /**
     * Creates a helper to edit the mapping instances. Individual settings can be
     * inspected by getters and changed by setters on the helper, changes can be then
     * applied back to the mappings.
     * @param project the target project
     * @param run run action mapping
     * @param debug debug action mapping
     * @param profile profile action mapping
     * @param goalMappings the mapping registry
     * @return 
     */
    public static ExecutionEnvHelper createExecutionEnvHelper(
            NbMavenProjectImpl project,
            NetbeansActionMapping run,
            NetbeansActionMapping debug,
            NetbeansActionMapping profile,
            ActionToGoalMapping goalMappings) {
        return new ExecutionEnvHelper(project, run, debug, profile, goalMappings);
    }
    
    /**
     * Joins parameters into a single string. Quotes as necessary if parameters contain
     * spaces. Checks for already quoted or escaped parameters.
     * @param params List of parameters.
     * @return single command line.
     */
    public static String joinParameters(String... params) {
        if (params == null) {
            return ""; // NOI18N
        }
        return joinParameters(Arrays.asList(params));
    }
    
    private static boolean isQuoteChar(char c) {
        return c == '\'' || c == '"';
    }
    
    public static List<String> escapeParameters(List<String> params) {
        List<String> ret = new ArrayList<>();
        for (String s : params) {
            if (s == null) {
                continue;
            }
            if (s.length() > 1) {
                char c = s.charAt(0);
                if (isQuoteChar(c) && s.charAt(s.length() - 1) == c) {
                    ret.add(s);
                    continue;
                }
            }
            // note: does not care about escaped spaces.
            if (!s.contains(" ")) {
                ret.add(s.replace("'", "\\'").replace("\"", "\\\""));
            } else {
                ret.add("\"" + s.replace("\"", "\\\"") + "\"");
            }
        }
        return ret;
    }
    
    public static String joinParameters(List<String> params) {
        return String.join(" ", escapeParameters(params));
    }

    public static List<String> extractDebugJVMOptions(String argLine) {
        Iterable<String> split = propertySplitter(argLine, true);
        List<String> toRet = new ArrayList<String>();
        for (String arg : split) {
            if ("-Xdebug".equals(arg)) { //NOI18N
                continue;
            }
            if ("-Djava.compiler=none".equals(arg)) { //NOI18N
                continue;
            }
            if ("-Xnoagent".equals(arg)) { //NOI18N
                continue;
            }
            if (arg.startsWith("-Xrunjdwp")) { //NOI18N
                continue;
            }
            if (arg.equals("-agentlib:jdwp")) { //NOI18N
                continue;
            }
            if (arg.startsWith("-agentlib:jdwp=")) { //NOI18N
                continue;
            }
            if (arg.trim().length() == 0) {
                continue;
            }
            toRet.add(arg);
        }
        return toRet;
    }

    
    /**
     * used by quickrun configuration.
     * @param argline
     * @return
     */
    public static String[] splitAll(String argline, boolean filterClassPath) {
        String jvm = argline == null ? null : splitJVMParams(argline, false);
        String mainClazz = argline == null ? null : splitMainClass(argline);
        String args = argline == null ? null : splitParams(argline);
        if (filterClassPath && jvm != null && jvm.contains("-classpath %classpath")) {
            jvm = jvm.replace("-classpath %classpath", "");
        }
        if (mainClazz != null && mainClazz.equals("${packageClassName}")) {
                    mainClazz = "";
        }
        return new String[] {
            (jvm != null ? jvm : ""),
            (mainClazz != null ? mainClazz : ""),
            (args != null ? args : "")
        };
    }
    
    public static String splitJVMParams(String line) {    
        return splitJVMParams(line, false);
    }
    
    @NonNull
    public static String splitJVMParams(String line, boolean newLines) {
        PropertySplitter ps = new PropertySplitter(line);
        ps.setSeparator(' '); //NOI18N
        String s = ps.nextPair();
        String jvms = ""; //NOI18N
        while (s != null) {
            if (s.startsWith("-") || /* #199411 */s.startsWith("\"-") || s.contains("%classpath")) { //NOI18N
                if(s.contains("%classpath")) {
                    jvms =  jvms + " " + s;
                } else {
                    jvms =  jvms + (jvms.isEmpty() ? "" : (newLines ? "\n" : " ")) + s;
                }
            } else if (s.equals(PACKAGE_CLASS_NAME_TOKEN) || s.equals(RUN_MAIN_CLASS_TOKEN) || s.matches("[\\w]+[\\.]{0,1}[\\w\\.]*")) { //NOI18N
                break;
            } else {
                jvms =  jvms + " " + s;
            }
            s = ps.nextPair();
        }
        return jvms.trim();
    }
    
    @NonNull
    public static String splitMainClass(String line) {
        PropertySplitter ps = new PropertySplitter(line);
        ps.setSeparator(' '); //NOI18N
        String s = ps.nextPair();
        while (s != null) {
            if (s.startsWith("-") || s.contains("%classpath")) { //NOI18N
                s = ps.nextPair();
                continue;
            } else if (s.equals(PACKAGE_CLASS_NAME_TOKEN) || s.equals(RUN_MAIN_CLASS_TOKEN) || s.matches("[\\w]+[\\.]{0,1}[\\w\\.]*")) { //NOI18N
                return s;
            } else {
                Logger.getLogger(RunJarPanel.class.getName()).fine("failed splitting main class from=" + line); //NOI18N
            }
            s = ps.nextPair();
        }
        return ""; //NOI18N
    }
    
    @NonNull
    public static String splitParams(String line) {
        int argsIndex = line.indexOf(RunJarStartupArgs.USER_PROGRAM_ARGS_MARKER);
        if (argsIndex > -1) {
            return line.substring(argsIndex + RunJarStartupArgs.USER_PROGRAM_ARGS_MARKER.length()).trim();
        }
        String main = splitMainClass(line);
        if (main.isEmpty()) {
            return "";
        }
        int i = line.indexOf(main);
        if (i > -1) {
            return line.substring(i + main.length()).trim();
        }
        return ""; //NOI18N
    }
    /**
     * Splits the line into sequence of arguments, respects quoting.
     * @param line the line as a string
     * @return arguments in an iterable
     */
    public static Iterable<String> propertySplitter(String line) {
        return propertySplitter(line, true);
    }
    
    public static Iterable<String> propertySplitter(String line, boolean outputQuotes) {
        class SplitIt implements Iterator<String> {
            private final PropertySplitter spl = new PropertySplitter(line);
            private String nextPair;

            public SplitIt() {
                spl.setSeparator(' ');
                spl.setOutputQuotes(outputQuotes);
            }
            
            @Override
            public boolean hasNext() {
                if (nextPair == null) {
                    nextPair = spl.nextPair();
                }
                return nextPair != null;
            }

            @Override
            public String next() {
                String s;
                if (nextPair == null) {
                    nextPair = spl.nextPair();
                }
                s = nextPair;
                nextPair = null;
                if (s != null) {
                    return s;
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new SplitIt();
            }
        };
    }
}
