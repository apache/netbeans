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
import java.nio.file.Path;
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
import java.util.HashSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gradle.tooling.ConfigurableLauncher;
import org.openide.util.NbBundle;
import static org.netbeans.modules.gradle.api.execute.GradleCommandLine.Argument.Kind.*;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleVersionRange;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.util.Utilities;

/**
 * Object representation of a Gradle command line. This object can be used to
 * add remove different flags, options and properties in a Gradle command line.
 * It can be used to merge and subtract command lines.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class GradleCommandLine implements Serializable {

    private static final String GRADLE_PROJECT_PROPERTY = "gradle-project"; //NOI18N
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
     * 
     * @since 2.23
     */
    public interface GradleOptionItem {
        /**
         * Shall return {@code true} if the IDE supports this option item.
         *
         * @return {@code true} if this option is supported by the IDE
         */
        boolean isSupported();

        /**
         * Shall return {@code true} if this option is supported by the provided
         * GradleDistribution.
         * 
         * @param dist the GradleDistribution to check.
         * @return {@code true} if the provided {@link GradleDistribution} supports this option.
         */
        boolean supportsGradle(GradleDistribution dist);
        List<String> getFlags();
        String getDescription();
    }


    /**
     * Gradle command line flags
     */
    public enum Flag implements GradleOptionItem {
        BUILD_CACHE(PARAM, "--build-cache"),
        CONFIGURATION_CACHE(PARAM, GradleVersionRange.from("6.5"), "--configuration-cache"),
        CONFIGURE_ON_DEMAND(PARAM, "--configure-on-demand"),
        CONTINUE(PARAM, "--continue"),
        CONTINUOUS(PARAM, "--continuous", "-t"),
        DAEMON(UNSUPPORTED, "--daemon"),
        DRY_RUN(PARAM, "-m", "--dry-run"),
        EXPORT_KEYS(PARAM, GradleVersionRange.from("6.2"), "--export-keys"),
        FOREGROUND(UNSUPPORTED, "--foreground"),
        GUI(UNSUPPORTED, GradleVersionRange.until("4.0"), "--gui"),
        HELP(UNSUPPORTED, "--help", "-h", "-?"),
        LOG_DEBUG(PARAM, "-d", "--debug"),
        LOG_INFO(PARAM, "-i", "--info"),
        LOG_QUIET(PARAM, "-q", "--quiet"),
        LOG_WARN(PARAM, "-w", "--warn"),
        NO_BUILD_CACHE(PARAM, "--no-build-cache"),
        NO_CONFIGURATION_CACHE(PARAM, GradleVersionRange.from("6.5"), "--no-configuration-cache"),
        NO_CONFIGURE_ON_DEMAND(PARAM, "--no-configure-on-demand"),
        NO_DAEMON(UNSUPPORTED, "--no-daemon"),
        NO_PARALLEL(PARAM, "--no-parallel"),
        NO_REBUILD(PARAM, "-a", "--no-rebuild"),
        NO_SCAN(PARAM, GradleVersionRange.from("4.3"), "--no-scan"),
        NO_SEARCH_UPWARD(UNSUPPORTED, GradleVersionRange.until("5.0"), "--no-search-upward", "-u"),
        NO_WATCH_FS(PARAM, GradleVersionRange.from("6.7"), "--no-watch-fs"),
        OFFLINE(PARAM, "--offline"),
        PARALLEL(PARAM, "--parallel"),
        PROFILE(PARAM, "--profile"),
        RECOMPILE_SCRIPTS(UNSUPPORTED, GradleVersionRange.until("5.0"), "--recompile-scripts"),
        REFRESH_DEPENDENCIES(PARAM, "-U", "--refresh-dependencies"),
        REFRESH_KEYS(PARAM, GradleVersionRange.from("6.2"), "--refresh-keys"),
        RERUN_TASKS(PARAM, "--rerun-tasks"),
        SCAN(PARAM, GradleVersionRange.from("4.3"), "--scan"),
        SHOW_VERSION(PARAM, GradleVersionRange.from("7.5"), "-V", "--show-version"),
        STACKTRACE(PARAM, "-s", "--stacktrace"),
        STACKTRACE_FULL(PARAM, "-S", "--full-stacktrace"),
        STATUS(UNSUPPORTED, "--status"),
        STOP(UNSUPPORTED, "--stop"),
        UPDATE_LOCKS(PARAM, GradleVersionRange.from("4.8"), "--update-locks"),
        VERSION(UNSUPPORTED, "--version", "-v"),
        WATCH_FS(PARAM, GradleVersionRange.from("6.7"), "--watch-fs"),
        WRITE_LOCKS(PARAM, GradleVersionRange.from("4.8"),"--write-locks");

        private Set<Flag> incompatible = Collections.emptySet();
        private final Argument.Kind kind;
        private final List<String> flags;
        private final GradleDistributionManager.GradleVersionRange supportedRange;

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

            CONFIGURATION_CACHE.incompatibleWith(NO_CONFIGURATION_CACHE);
            NO_CONFIGURATION_CACHE.incompatibleWith(CONFIGURATION_CACHE);
            
            CONFIGURE_ON_DEMAND.incompatibleWith(NO_CONFIGURE_ON_DEMAND);
            NO_CONFIGURE_ON_DEMAND.incompatibleWith(CONFIGURE_ON_DEMAND);

            BUILD_CACHE.incompatibleWith(NO_BUILD_CACHE);
            NO_BUILD_CACHE.incompatibleWith(BUILD_CACHE);

            PARALLEL.incompatibleWith(NO_PARALLEL);
            NO_PARALLEL.incompatibleWith(PARALLEL);
            
            WATCH_FS.incompatibleWith(NO_WATCH_FS);
            NO_WATCH_FS.incompatibleWith(WATCH_FS);
        }

        private Flag(Argument.Kind kind, String... flags) {
            this(kind, GradleDistributionManager.GradleVersionRange.UNBOUNDED, flags);
        }
        
        private Flag(Argument.Kind kind, GradleDistributionManager.GradleVersionRange supportedRange, String... flags) {
            this.kind = kind;
            this.flags = Arrays.asList(flags);
            this.supportedRange = supportedRange;
        }

        private void incompatibleWith(Flag first, Flag... rest) {
            incompatible = Collections.unmodifiableSet(EnumSet.of(first, rest));
        }

        @Override
        public boolean isSupported() {
            return kind != UNSUPPORTED;
        }

        @Override
        public List<String> getFlags() {
            return flags;
        }

        @Override
        public final String getDescription() {
            return NbBundle.getMessage(GradleCommandLine.class, this.name() + "_DSC");
        }

        @Override
        public boolean supportsGradle(GradleDistribution dist) {
            return supportedRange.contains(dist.version);
        }
    }

    public enum Property implements GradleOptionItem {
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

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public List<String> getFlags() {
            return Collections.singletonList(flag);
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(GradleCommandLine.class, this.name() + "_DSC");
        }

        @Override
        public boolean supportsGradle(GradleDistribution dist) {
            return true;
        }

    }

    public enum Parameter implements GradleOptionItem {

        BUILD_FILE(UNSUPPORTED, "-b", "--build-file"),
        CONFIGURATION_CACHE_PROBLEMS(PARAM, GradleVersionRange.from("6.5"), argValues("fail", "warn"), "--configuration-cache-problems"),
        CONSOLE(UNSUPPORTED, argValues("plain", "auto", "rich", "verbose"), "--console"),
        DEPENDENCY_VERIFICATION(PARAM, argValues("strict", "lenient", "off"), "-F", "--dependency-verification"),
        EXCLUDE_TASK(PARAM, "-x", "--exclude-task"),
        GRADLE_USER_HOME(UNSUPPORTED, "-g", "--gradle-user-home"),
        INIT_SCRIPT(PARAM, "-I", "--init-script"),
        @Deprecated
        IMPORT_BUILD(UNSUPPORTED),
        INCLUDE_BUILD(PARAM, GradleVersionRange.from("3.1"), "--include-build"),
        MAX_WORKER(PARAM, "--max-worker"),
        PRIORITY(PARAM, argValues("normal", "low"), "--priority"),
        PROJECT_CACHE_DIR(UNSUPPORTED, "--project-cache-dir"),
        PROJECT_DIR(PARAM, "-p", "--project-dir"),
        @Deprecated
        SETTINGS_FILE(UNSUPPORTED, "-c", "--settings-file"),
        WARNING_MODE(PARAM, argValues("all", "fail", "summary", "none"),"--warning-mode"),
        WRITE_VERIFICATION_METADATA(PARAM, GradleVersionRange.from("6.2"), "-M", "write-verification-metadata");

        final Argument.Kind kind;
        final GradleDistributionManager.GradleVersionRange supportedRange;
        final List<String> flags;
        final Argument.Values values;

        Parameter(Argument.Kind kind, String... flags) {
            this(kind, Argument.Values.ANY, flags);
        }
        
        Parameter(Argument.Kind kind, GradleDistributionManager.GradleVersionRange supportedRange, String... flags) {
            this(kind, supportedRange, Argument.Values.ANY, flags);
        }

        Parameter(Argument.Kind kind, Argument.Values values, String... flags) {
            this(kind, GradleDistributionManager.GradleVersionRange.UNBOUNDED, values, flags);
        }
        
        Parameter(Argument.Kind kind, GradleDistributionManager.GradleVersionRange supportedRange, Argument.Values values, String... flags) {
            this.kind = kind;
            this.values = values;
            this.flags = Arrays.asList(flags);
            this.supportedRange = supportedRange;
        }

        private static Argument.Values argValues(String... values) {
            return new Argument.Values(values);
        }

        @Override
        public boolean isSupported() {
            return kind != UNSUPPORTED;
        }

        @Override
        public List<String> getFlags() {
            return flags;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(GradleCommandLine.class, this.name() + "_DSC");
        }

        @Override
        public boolean supportsGradle(GradleDistribution dist) {
            return supportedRange.contains(dist.version);
        }
    }

    //<editor-fold desc="Argument processing internals" defaultstate="collapsed"    >
    interface Argument {

        enum Kind {
            PARAM, SYSTEM, UNSUPPORTED
        }

        static final class Values {
            public static final Values ANY = new Values();
            final String[] values;
            
            private Values(String... values) {
                this.values = values;
            }
        }
        
        Kind getKind();

        List<String> getArgs();

        boolean supportsGradle(GradleDistribution dist);
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
            return Collections.singletonList(toString());
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

        @Override
        public boolean supportsGradle(GradleDistribution dist) {
            return flag.supportsGradle(dist);
        }

        @Override
        public String toString() {
            return flag.flags.get(0);
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
            return Collections.singletonList(toString());
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

        @Override
        public boolean supportsGradle(GradleDistribution dist) {
            return prop.supportsGradle(dist);
        }

        @Override
        public String toString(){
            return prop.prefix + key + "=" + value;
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

        @Override
        public boolean supportsGradle(GradleDistribution dist) {
            return param.supportsGradle(dist);
        }

        @Override
        public String toString() {
            return param.flags.get(0) + " " + value;
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

    final GradleDistribution dist;
    final Set<Argument> arguments = new LinkedHashSet<>();
    final Set<String> tasks = new LinkedHashSet<>();

    /**
    /**
     * Creates a copy instance of the provided GradleCommandLine, but with for a
     * specified GradleDistribution.
     *
     * @param dist the GradleDistribution for compatibility checks. {@code null}
     *             can be used for unspecified.
     * @param cmd the command line to copy
     * @since 2.23
     */
    public GradleCommandLine(GradleDistribution dist, GradleCommandLine cmd) {
        this.dist = dist;
        arguments.addAll(cmd.arguments);
        tasks.addAll(cmd.tasks);
    }

    /**
     * Creates a copy instance of the provided GradleCommandLine.
     *
     * @param cmd the command line to copy
     */
    public GradleCommandLine(GradleCommandLine cmd) {
        this(cmd.dist, cmd);
    }

    /**
     * Creates a command line form the specified arguments with compatibility
     * constraint on a specified Gradle distribution.
     *
     * @param dist the GradleDistribution for compatibility checks. {@code null}
     *             can be used for unspecified.
     * @param args the command line parameters
     * @since 2.23
     */
    public GradleCommandLine(GradleDistribution dist, String... args) {
        this.dist = dist;
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

    public GradleCommandLine(String... args) {
        this(null, args);
    }

    public GradleCommandLine(GradleDistribution dist, CharSequence argLine) {
        this(dist, Utilities.parseParameters(argLine.toString()));
    }

    public GradleCommandLine(CharSequence argLine) {
        this(null, Utilities.parseParameters(argLine.toString()));
    }

    private List<String> getArgs(Set<Argument.Kind> kinds) {
        List<String> ret = new LinkedList<>();
        for (Argument arg : arguments) {
            if (kinds.contains(arg.getKind())) {
                if ((dist == null) || arg.supportsGradle(dist)) {
                    ret.addAll(arg.getArgs());
                }
                if ((dist != null) && !arg.supportsGradle(dist)) {
                    LOGGER.log(Level.INFO, "'{0}' is not supported by Gradle {1}, so it will be omitted.", new Object[]{arg.toString(), dist.getVersion()});
                }
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

    public boolean canAdd(Flag flag) {
        return canAdd((GradleOptionItem) flag);
    }
    
    public boolean canAdd(GradleOptionItem item) {
        Set<GradleOptionItem> reserved = new HashSet<>();
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg instanceof FlagArgument) {
                FlagArgument farg = (FlagArgument) arg;
                reserved.add(farg.flag);
                reserved.addAll(farg.flag.incompatible);
            }
        }
        return !reserved.contains(item);
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

    static final void addGradleSettingJvmargs(File rootDir, List<String> jvmargs) {
        List<File> propFiles = new ArrayList<>();
        propFiles.add(new File(GradleSettings.getDefault().getGradleUserHome(), GradleFiles.GRADLE_PROPERTIES_NAME));

        if (rootDir != null) {
            propFiles.addAll(new GradleFiles(rootDir).getPropertyFiles());
        }
        //TODO: Theoretically the Gradle Distribution dir can have a gradle.properties
        //      however computing that is not really easy, at the moment we do
        //      not support that one.
        for (File f : propFiles) {
            if (f.canRead()) {
                try (InputStream in = new FileInputStream(f)) {
                    Properties props = new Properties();
                    props.load(in);
                    if (props.containsKey(PROP_JVMARGS)) {
                        List<String> args = Arrays.asList(Utilities.parseParameters(props.getProperty(PROP_JVMARGS)));
                        jvmargs.addAll(args);
                        break;
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Cannot read property file: '" + f.getAbsolutePath() + "' as: " + ex.getMessage());
                }
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
     * @param rootDir can be {@code null} if the project properties for JVM
     * arguments shall not be evaluated.
     */
    public void configure(ConfigurableLauncher<?> launcher, File rootDir) {
        List<String> jvmargs = getArgs(EnumSet.of(SYSTEM));
        addGradleSettingJvmargs(rootDir, jvmargs);
        launcher.setJvmArguments(jvmargs);
        List<String> args = new LinkedList<>(getArgs(EnumSet.of(PARAM)));
        configureGradleHome(launcher);
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
        ret.setFlag(Flag.CONFIGURATION_CACHE, settings.getUseConfigCache());

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


    /**
     * For testing purposesl. Use {@link #setHomeProvider} to set. Never null, possibly no op
     */
    private static Supplier<Path> gradleHomeProvider = () -> null;

    /**
     * Testing support: allows to operate in a separate directory, i.e. without downloaded jars etc. In order to  use,
     * make an accessor in the test sources, in the same package (no accessor is in production sources).
     * @param homeProvider 
     */
    static void setHomeProvider(Supplier<Path> homeProvider) {
        if (homeProvider == null) {
            homeProvider = () -> null;
        }
        gradleHomeProvider = homeProvider;
    }
    
    /**
     * Configures the launcher with parameters.
     * @param launcher
     */
    static ConfigurableLauncher<?> configureGradleHome(ConfigurableLauncher<?> launcher) {
        Path home = gradleHomeProvider.get();
        if (home != null) {
            return launcher.withArguments("--gradle-user-home", home.toString());
        } else {
            return launcher;
        }
    }
}
