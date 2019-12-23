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
package org.netbeans.modules.gradle.api.execute;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gradle.tooling.ConfigurableLauncher;
import org.openide.util.NbBundle;
import static org.netbeans.modules.gradle.api.execute.GradleCommandLine.Argument.Kind.*;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.GradleFiles;

/**
 * Object representation of a Gradle command line. This object can be used to
 * add remove different flags, options and properties in a Gradle command line.
 * It can be used to merge and subtract command lines.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class GradleCommandLine implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(GradleCommandLine.class.getName());
    private static final String PROP_JVMARGS = "org.gradle.jvmargs"; // NOI18N
    /**
     * Gradle log levels.
     */
    public enum LogLevel {
        DEBUG, INFO, LIFECYCLE, WARN, QUIET
    }

    /**
     * Gradle stacktrace output levels.
     */
    public enum StackTrace {
        NONE, SHORT, FULL
    }

    /**
     * The common name of the task which invokes tests.
     */
    public static final String TEST_TASK = "test"; //NOI18N
    /**
     * The common name of the task which invokes checks.
     */
    public static final String CHECK_TASK = "check"; //NOI18N

    /**
     * Gradle command line flags
     */
    public enum Flag {
        NO_REBUILD(PARAM, "-a", "--no-rebuild"),
        BUILD_CACHE(PARAM, "--build-cache"),
        CONFIGURE_ON_DEMAND(PARAM, "--configure-on-demand"),
        CONTINUE(PARAM, "--continue"),
        DRY_RUN(PARAM, "-m", "--dry-run"),
        OFFLINE(PARAM, "--offline"),
        PARALLEL(PARAM, "--parallel"),
        REFRESH_DEPENDENCIES(PARAM, "--refresh-dependencies"),
        RERUN_TASKS(PARAM, "--rerun-tasks"),
        LOG_DEBUG(PARAM, "-d", "--debug"),
        LOG_INFO(PARAM, "-i", "--info"),
        LOG_WARN(PARAM, "-w", "--warn"),
        LOG_QUIET(PARAM, "-q", "--quiet"),
        STACKTRACE(PARAM, "-s", "--stacktrace"),
        STACKTRACE_FULL(PARAM, "-S", "--full-stacktrace"),
        PROFILE(PARAM, "--profile"),
        NO_BUILD_CACHE(PARAM, "--no-build-cache"),
        NO_CONFIGURE_ON_DEMAND(PARAM, "--no-configure-on-demand"),
        NO_PARALLEL(PARAM, "--no-parallel"),
        SCAN(PARAM, "--scan"),
        NO_SCAN(PARAM, "--no-scan"),
        DAEMON(UNSUPPORTED, "--no-daemon"),
        NO_DAEMON(UNSUPPORTED, "--daemon"),
        HELP(UNSUPPORTED, "--help", "-h", "-?"),
        FOREGROUND(UNSUPPORTED, "--foreground"),
        GUI(UNSUPPORTED, "--gui"),
        STATUS(UNSUPPORTED, "--status"),
        STOP(UNSUPPORTED, "--stop"),
        CONTINUOUS(UNSUPPORTED, "--continuous", "-t"),
        NO_SEARCH_UPWARD(UNSUPPORTED, "--no-search-upward", "-u"),
        RECOMPILE_SCRIPTS(UNSUPPORTED, "--recompile-scripts"),
        VERSION(UNSUPPORTED, "--version", "-v");

        private Set<Flag> incompatible = Collections.emptySet();
        private final Argument.Kind kind;
        private final List<String> flags;

        static {
            DAEMON.incompatibleWith(NO_DAEMON);
            NO_DAEMON.incompatibleWith(DAEMON);

            LOG_DEBUG.incompatibleWith(LOG_INFO, LOG_QUIET, LOG_WARN);
            LOG_INFO.incompatibleWith(LOG_DEBUG, LOG_QUIET, LOG_WARN);
            LOG_WARN.incompatibleWith(LOG_DEBUG, LOG_INFO, LOG_QUIET);
            LOG_QUIET.incompatibleWith(LOG_DEBUG, LOG_INFO, LOG_WARN);

            STACKTRACE.incompatibleWith(STACKTRACE_FULL);
            STACKTRACE_FULL.incompatibleWith(STACKTRACE);

            SCAN.incompatibleWith(NO_SCAN);
            NO_SCAN.incompatibleWith(SCAN);

            CONFIGURE_ON_DEMAND.incompatibleWith(NO_CONFIGURE_ON_DEMAND);
            NO_CONFIGURE_ON_DEMAND.incompatibleWith(CONFIGURE_ON_DEMAND);

            BUILD_CACHE.incompatibleWith(NO_BUILD_CACHE);
            NO_BUILD_CACHE.incompatibleWith(BUILD_CACHE);

            PARALLEL.incompatibleWith(NO_PARALLEL);
            NO_PARALLEL.incompatibleWith(PARALLEL);
        }

        private Flag(Argument.Kind kind, String... flags) {
            this.kind = kind;
            this.flags = Arrays.asList(flags);
        }

        private void incompatibleWith(Flag first, Flag... rest) {
            incompatible = Collections.unmodifiableSet(EnumSet.of(first, rest));
        }

        public boolean isSupported() {
            return kind != UNSUPPORTED;
        }

        public List<String> getFlags() {
            return flags;
        }

        public final String getDescription() {
            return NbBundle.getMessage(GradleCommandLine.class, this.name() + "_DSC");
        }
    }

    public enum Property {
        PROJECT(PARAM, "-P", "--project-prop"),
        SYSTEM(Argument.Kind.SYSTEM, "-D", "--system-prop");

        private final Argument.Kind kind;
        private final String prefix;
        private final String flag;

        private Property(Argument.Kind kind, String prefix, String flag) {
            this.kind = kind;
            this.prefix = prefix;
            this.flag = flag;
        }

    }

    public enum Parameter {

        SETTINGS_FILE(UNSUPPORTED, "-c", "--settings-file"),
        CONSOLE(UNSUPPORTED, "--console"),
        GRADLE_USER_HOME(UNSUPPORTED, "-g", "--gradle-user-home"),
        INIT_SCRIPT(PARAM, "-I", "--init-script"),
        MAX_WORKER(PARAM, "--max-worker"),
        PROJECT_DIR(PARAM, "-p", "--project-dir"),
        PROJECT_CACHE_DIR(UNSUPPORTED, "--project-cache-dir"),
        EXCLUDE_TASK(PARAM, "-x", "--exclude-task"),
        IMPORT_BUILD(PARAM, "--import-build");

        final Argument.Kind kind;
        final List<String> flags;

        Parameter(Argument.Kind kind, String... flags) {
            this.kind = kind;
            this.flags = Arrays.asList(flags);
        }
    }

    //<editor-fold desc="Argument processing internals" defaultstate="collapsed"    >
    interface Argument {

        enum Kind {
            PARAM, SYSTEM, UNSUPPORTED
        }

        Kind getKind();

        List<String> getArgs();
    }

    interface ArgumentParser<T extends Argument> {

        T parse(String arg, Iterator<String> args);
    }

    static class FlagArgument implements Argument, ArgumentParser<FlagArgument> {

        final Flag flag;
        private static final EnumMap<Flag, FlagArgument> FLAG_ARGS = new EnumMap<>(Flag.class);

        static {
            for (Flag flag : Flag.values()) {
                FLAG_ARGS.put(flag, new FlagArgument(flag));
            }
        }

        public static FlagArgument of(Flag f) {
            return FLAG_ARGS.get(f);
        }

        private FlagArgument(Flag flag) {
            this.flag = flag;
        }

        @Override
        public List<String> getArgs() {
            return Collections.singletonList(flag.flags.get(0));
        }

        @Override
        public FlagArgument parse(String arg, Iterator<String> args) {
            return flag.flags.contains(arg) ? this : null;
        }

        @Override
        public int hashCode() {
            return flag.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FlagArgument other = (FlagArgument) obj;
            return this.flag == other.flag;
        }

        @Override
        public Kind getKind() {
            return flag.kind;
        }
    }

    static class PropertyArgument implements Argument {

        final Property prop;
        final String key;
        final String value;

        public PropertyArgument(Property prop, String key, String value) {
            this.prop = prop;
            this.key = key;
            this.value = value;
        }

        @Override
        public List<String> getArgs() {
            return Collections.singletonList(prop.prefix + key + "=" + value);
        }

        @Override
        public Kind getKind() {
            return prop.kind;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.prop);
            hash = 47 * hash + Objects.hashCode(this.key);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PropertyArgument other = (PropertyArgument) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            return this.prop == other.prop;
        }

    }

    static class PropertyParser implements ArgumentParser<PropertyArgument> {

        final Property prop;

        PropertyParser(Property prop) {
            this.prop = prop;
        }

        @Override
        public PropertyArgument parse(String arg, Iterator<String> args) {
            String keyValue = null;
            if (prop.flag.equals(arg)) {
                if (args.hasNext()) {
                    keyValue = args.next();
                }
            }
            if ((keyValue == null) && arg.startsWith(prop.prefix)) {
                keyValue = arg.substring(2);
            }
            if (keyValue != null) {
                int eq = keyValue.indexOf('=');
                int colon = keyValue.indexOf(':');
                int sep = (eq > 0) && (colon > 0) ? Math.min(eq, colon) : Math.max(eq, colon);
                if (sep > 0) {
                    String key = keyValue.substring(0, sep);
                    String value = keyValue.substring(sep + 1);
                    return new PropertyArgument(prop, key, value);
                }
            }
            return null;
        }

    }

    static class ParametricArgument implements Argument {

        final Parameter param;
        final String value;

        public ParametricArgument(Parameter param, String value) {
            this.param = param;
            this.value = value;
        }

        @Override
        public Kind getKind() {
            return param.kind;
        }

        @Override
        public List<String> getArgs() {
            return Arrays.asList(param.flags.get(0), value);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.param);
            hash = 79 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParametricArgument other = (ParametricArgument) obj;
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return this.param == other.param;
        }
    }

    static class ParameterParser implements ArgumentParser<ParametricArgument> {

        final Parameter param;

        public ParameterParser(Parameter param) {
            this.param = param;
        }

        @Override
        public ParametricArgument parse(String arg, Iterator<String> args) {
            return param.flags.contains(arg) && args.hasNext() ? new ParametricArgument(param, args.next()) : null;
        }

    }

    static final List<ArgumentParser<? extends Argument>> PARSERS = new LinkedList<>();

    static {
        for (Flag flag : Flag.values()) {
            PARSERS.add(FlagArgument.of(flag));
        }
        for (Property prop : Property.values()) {
            PARSERS.add(new PropertyParser(prop));
        }
        for (Parameter param : Parameter.values()) {
            PARSERS.add(new ParameterParser(param));
        }
    }
    //</editor-fold>

    final Set<Argument> arguments = new LinkedHashSet<>();
    final Set<String> tasks = new LinkedHashSet<>();

    public GradleCommandLine(GradleCommandLine cmd) {
        arguments.addAll(cmd.arguments);
        tasks.addAll(cmd.tasks);
    }

    public GradleCommandLine(String... args) {
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String arg = it.next();
            Argument parg = null;
            for (ArgumentParser<? extends Argument> parser : PARSERS) {
                parg = parser.parse(arg, it);
                if (parg != null) {
                    arguments.add(parg);
                    break;
                }
            }
            if (parg == null) {
                tasks.add(arg);
            }
        }
    }

    public GradleCommandLine(CharSequence argLine) {
        this(parseArgLine(argLine));
    }

    static String[] parseArgLine(CharSequence cli) {
        char quote = 0;
        StringBuilder buf = new StringBuilder();
        List<String> args = new ArrayList<>();
        for (int i = 0; i < cli.length(); i++) {
            char ch = cli.charAt(i);
            if (quote == 0) {
                if (Character.isWhitespace(ch)) {
                    if (buf.length() > 0) {
                        args.add(buf.toString());
                        buf.setLength(0);
                    }
                } else {
                    if (ch == '"' || ch == '\'') {
                        quote = ch;
                    } else {
                        buf.append(ch);
                    }
                }
            } else {
                if (quote == ch) {
                    quote = 0;
                } else {
                    buf.append(ch);
                }
            }
        }
        if (buf.length() > 0) {
            args.add(buf.toString());
        }
        return args.toArray(new String[args.size()]);
    }

    private List<String> getArgs(Set<Argument.Kind> kinds) {
        List<String> ret = new LinkedList<>();
        for (Argument arg : arguments) {
            if (kinds.contains(arg.getKind())) {
                ret.addAll(arg.getArgs());
            }
        }
        return ret;
    }

    /**
     * Retrieve the command line which is actually supported to be executed from
     * the IDE.
     *
     * @return the list of IDE supported arguments.
     */
    public List<String> getSupportedCommandLine() {
        List<String> ret = getArgs(EnumSet.of(PARAM, SYSTEM));
        ret.addAll(tasks);
        return ret;
    }

    /**
     * Retrieve the command line as a list of strings. This list can contain
     * arguments which is no meaning or not supported in the IDE, like
     * '--no-daemon' as due to the nature of Gradle tooling, Gradle daemon is
     * always being used.
     *
     * @return the list of Gradle arguments.
     */
    public List<String> getFullCommandLine() {
        List<String> ret = getArgs(EnumSet.allOf(Argument.Kind.class));
        ret.addAll(tasks);
        return ret;
    }

    public Set<String> getTasks() {
        return new LinkedHashSet<>(tasks);
    }

    public void setTasks(Collection<String> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
    }

    public void removeTask(String task) {
        tasks.remove(task);
    }

    public void addTask(String task) {
        tasks.add(task);
    }

    public boolean hasTask(String task) {
        return tasks.contains(task);
    }

    public boolean hasFlag(Flag flag) {
        return arguments.contains(FlagArgument.of(flag));
    }

    public void addFlag(Flag flag) {
        arguments.add(FlagArgument.of(flag));
    }

    public boolean canAdd(Flag f) {
        EnumSet<Flag> reserved = EnumSet.noneOf(Flag.class);
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof FlagArgument) {
                FlagArgument farg = (FlagArgument) arg;
                reserved.add(farg.flag);
                reserved.addAll(farg.flag.incompatible);
            }
        }
        return !reserved.contains(f);
    }

    public void removeFlag(Flag flag) {
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof FlagArgument) {
                FlagArgument farg = (FlagArgument) arg;
                if (farg.flag == flag) {
                    it.remove();
                }
            }
        }
    }

    public void setFlag(Flag flag, boolean b) {
        if (b) {
            addFlag(flag);
        } else {
            removeFlag(flag);
        }

    }

    public void addParameter(Parameter param, String value) {
        arguments.add(new ParametricArgument(param, value));
    }

    public String getFirstParameter(Parameter param) {
        for (Argument arg : arguments) {
            if (arg instanceof ParametricArgument) {
                ParametricArgument parg = (ParametricArgument) arg;
                if (parg.param == param) {
                    return parg.value;
                }
            }
        }
        return null;
    }

    public boolean hasParameter(Parameter param) {
        return getFirstParameter(param) != null;
    }

    public Collection<String> getParameters(Parameter param) {
        Collection<String> ret = new LinkedList<>();
        for (Argument arg : arguments) {
            if (arg instanceof ParametricArgument) {
                ParametricArgument parg = (ParametricArgument) arg;
                if (parg.param == param) {
                    ret.add(parg.value);
                }
            }
        }
        return ret;
    }

    public Set<String> getExcludedTasks() {
        return new LinkedHashSet<>(getParameters(Parameter.EXCLUDE_TASK));
    }

    public void setExcludedTasks(Collection<String> excluded) {
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof ParametricArgument) {
                ParametricArgument parg = (ParametricArgument) arg;
                if (parg.param == Parameter.EXCLUDE_TASK) {
                    it.remove();
                }
            }
        }
        for (String task : excluded) {
            arguments.add(new ParametricArgument(Parameter.EXCLUDE_TASK, task));
        }
    }

    public void removeParameters(Parameter param) {
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof ParametricArgument) {
                ParametricArgument parg = (ParametricArgument) arg;
                if (parg.param == param) {
                    it.remove();
                }
            }
        }
    }

    public void removeParameter(Parameter param, String value) {
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof ParametricArgument) {
                ParametricArgument parg = (ParametricArgument) arg;
                if (parg.param == param && parg.value.equals(value)) {
                    it.remove();
                }
            }
        }
    }

    public void removeProperty(Property prop, String key) {
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof PropertyArgument) {
                PropertyArgument parg = (PropertyArgument) arg;
                if (parg.prop == prop && parg.key.equals(key)) {
                    it.remove();
                }
            }
        }
    }

    public GradleCommandLine remove(GradleCommandLine mask) {
        GradleCommandLine ret = new GradleCommandLine();
        for (Argument argument : mask.arguments) {
            if (argument instanceof FlagArgument) {
                FlagArgument farg = (FlagArgument) argument;
                if (hasFlag(farg.flag)) {
                    ret.setFlag(farg.flag, true);
                    setFlag(farg.flag, false);
                }
            }
            if (argument instanceof ParametricArgument) {
                ParametricArgument parg = (ParametricArgument) argument;
                if (hasParameter(parg.param) && getParameters(parg.param).contains(parg.value)) {
                    removeParameter(parg.param, parg.value);
                    ret.addParameter(parg.param, parg.value);
                }
            }
            if (argument instanceof PropertyArgument) {
                PropertyArgument parg = (PropertyArgument) argument;
                String propValue = getProperty(parg.prop, parg.key);
                if (propValue != null) {
                    if (parg.prop == Property.PROJECT) {
                        removeProperty(parg.prop, parg.key);
                        switch (parg.prop) {
                            case PROJECT:
                                ret.addProjectProperty(parg.key, propValue);
                                break;
                            case SYSTEM:
                                ret.addSystemProperty(parg.key, propValue);
                                break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    public String getProperty(Property type, String key) {
        for (Argument arg : arguments) {
            if (arg instanceof PropertyArgument) {
                PropertyArgument parg = (PropertyArgument) arg;
                if ((parg.prop == type) && parg.key.equals(key)) {
                    return parg.value;
                }
            }
        }
        return null;
    }

    public LogLevel getLoglevel() {
        LogLevel ret = LogLevel.WARN;
        for (Argument arg : arguments) {
            if (arg instanceof FlagArgument) {
                FlagArgument farg = (FlagArgument) arg;
                switch (farg.flag) {
                    case LOG_DEBUG:
                        ret = LogLevel.DEBUG;
                        break;
                    case LOG_INFO:
                        ret = LogLevel.INFO;
                        break;
                    case LOG_QUIET:
                        ret = LogLevel.QUIET;
                        break;
                }
            }
        }
        return ret;
    }

    public void setLogLevel(LogLevel level) {
        arguments.removeAll(Arrays.asList(Flag.LOG_DEBUG, Flag.LOG_INFO, Flag.LOG_QUIET, Flag.LOG_WARN));
        switch (level) {
            case DEBUG:
                addFlag(Flag.LOG_DEBUG);
                break;
            case INFO:
                addFlag(Flag.LOG_INFO);
                break;
            case WARN:
                addFlag(Flag.LOG_WARN);
                break;
            case QUIET:
                addFlag(Flag.LOG_QUIET);
                break;
        }
    }

    public void addProjectProperty(String key, String value) {
        PropertyArgument arg = new PropertyArgument(Property.PROJECT, key, value);
        arguments.remove(arg);
        arguments.add(arg);
    }

    public void addSystemProperty(String key, String value) {
        PropertyArgument arg = new PropertyArgument(Property.SYSTEM, key, value);
        arguments.remove(arg);
        arguments.add(arg);
    }

    public StackTrace getStackTrace() {
        StackTrace ret = StackTrace.NONE;
        for (Argument arg : arguments) {
            if (arg instanceof FlagArgument) {
                FlagArgument farg = (FlagArgument) arg;
                switch (farg.flag) {
                    case STACKTRACE:
                        ret = StackTrace.SHORT;
                        break;
                    case STACKTRACE_FULL:
                        ret = StackTrace.FULL;
                        break;
                }
            }
        }
        return ret;
    }

    public void setStackTrace(StackTrace st) {
        removeFlag(Flag.STACKTRACE);
        removeFlag(Flag.STACKTRACE_FULL);
        switch (st) {
            case FULL:
                addFlag(Flag.STACKTRACE_FULL);
                break;
            case SHORT:
                addFlag(Flag.STACKTRACE);
                break;
        }
    }

    private void addGradleSettingJvmargs(File projectDir, List<String> jvmargs) {
        List<File> propFiles = new ArrayList<>();

        if (projectDir == null) {
            File gradleHome = GradleSettings.getDefault().getGradleUserHome();
            File f = new File(gradleHome, GradleFiles.GRADLE_PROPERTIES_NAME);
            if (f.exists()) {
                propFiles.add(f);
            }
        } else {
            propFiles.addAll(new GradleFiles(projectDir).getPropertyFiles());
        }

        for (File f : propFiles) {
            try (InputStream in = new FileInputStream(f)) {
                Properties props = new Properties();
                props.load(in);
                if (props.containsKey(PROP_JVMARGS)) {
                    jvmargs.addAll(Arrays.asList(props.getProperty(PROP_JVMARGS).split("\\s+"))); //NOI18N
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Cannot read property file: '" + f.getAbsolutePath() + "' as: " + ex.getMessage());
            }
        }
    }

    /**
     * Configures the given Gradle Launcher considering the options set in this
     * command line and the Java VM arguments specified by the given project
     * dir.
     *
     * @since 1.3
     * @param launcher the Launcher instance to configure.
     * @param projectDir can be {@code null} if the project properties for JVM
     * arguments shall not be evaluated.
     */
    public void configure(ConfigurableLauncher<?> launcher, File projectDir) {
        List<String> jvmargs = getArgs(EnumSet.of(SYSTEM));
        addGradleSettingJvmargs(projectDir, jvmargs);
        launcher.setJvmArguments(jvmargs);
        List<String> args = new LinkedList<>(getArgs(EnumSet.of(PARAM)));
        args.addAll(tasks);
        launcher.withArguments(args);
    }

    /**
     * Configures the given Gradle Launcher considering the options set in this
     * command line.
     *
     * @since 1.0
     * @param launcher the Launcher instance to configure.
     */
    public void configure(ConfigurableLauncher<?> launcher) {
        configure(launcher, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        List<String> cli = getFullCommandLine();
        sb.append("Gradle CommandLine:");
        for (String s : cli) {
            sb.append(' ').append(s);
        }
        return sb.toString();
    }

    public static GradleCommandLine combine(GradleCommandLine first, GradleCommandLine... layers) {
        GradleCommandLine ret = new GradleCommandLine(first);
        for (GradleCommandLine layer : layers) {
            // Compute tasks and excludes first as argument processing can mess it up
            Set<String> newExcludes = ret.getExcludedTasks();
            newExcludes.removeAll(layer.tasks);
            newExcludes.addAll(layer.getExcludedTasks());

            Set<String> newTasks = ret.getTasks();
            newTasks.removeAll(layer.getExcludedTasks());
            newTasks.addAll(layer.getTasks());

            layer.arguments.forEach((argument) -> {
                if (argument instanceof PropertyArgument) {
                    PropertyArgument parg = (PropertyArgument) argument;
                    if (parg.prop == Property.PROJECT) {
                        ret.addProjectProperty(parg.key, parg.value);
                    }
                    if (parg.prop == Property.SYSTEM) {
                        ret.addSystemProperty(parg.key, parg.value);
                    }
                } else if (argument instanceof FlagArgument) {
                    FlagArgument farg = (FlagArgument) argument;
                    for (Flag flag : farg.flag.incompatible) {
                        ret.removeFlag(flag);
                    }
                    ret.arguments.add(argument);
                } else {
                    ret.arguments.add(argument);
                }
            });

            ret.setExcludedTasks(newExcludes);
            ret.setTasks(newTasks);

        }
        return ret;
    }

    public static GradleCommandLine getDefaultCommandLine() {
        GradleSettings settings = GradleSettings.getDefault();
        GradleCommandLine ret = new GradleCommandLine();

        ret.setFlag(Flag.OFFLINE, settings.isOffline());
        ret.setFlag(Flag.CONFIGURE_ON_DEMAND, settings.isConfigureOnDemand());
        ret.setFlag(Flag.NO_REBUILD, settings.getNoRebuild());

        ret.setLogLevel(settings.getDefaultLogLevel());
        ret.setStackTrace(settings.getDefaultStackTrace());

        if (settings.skipCheck()) {
            ret.addParameter(Parameter.EXCLUDE_TASK, CHECK_TASK);
        }
        if (settings.skipTest()) {
            ret.addParameter(Parameter.EXCLUDE_TASK, TEST_TASK);
        }
        return ret;
    }

}
