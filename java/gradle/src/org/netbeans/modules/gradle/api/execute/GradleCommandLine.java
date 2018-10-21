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
import org.netbeans.modules.gradle.spi.execute.GradleLauncherConfigurator;
import java.util.ArrayList;
import org.gradle.tooling.ConfigurableLauncher;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleCommandLine implements GradleLauncherConfigurator {

    public enum LogLevel {DEBUG, INFO, NORMAL, QUIET}
    public enum StackTrace {NONE, SHORT, FULL}

    public enum Flag {
        NO_REBUILD(Argument.Kind.PARAM, "-a", "--no-rebuild"),
        CONFIGURE_ON_DEMAND(Argument.Kind.PARAM, "--configure-on-demand"),
        CONTINUE(Argument.Kind.PARAM, "--continue"),
        DRY_RUN(Argument.Kind.PARAM, "-m", "--dry-run"),
        OFFLINE(Argument.Kind.PARAM, "--offline"),
        PARALLEL(Argument.Kind.PARAM, "--parallel"),
        RECOMPILE_SCRIPTS(Argument.Kind.PARAM, "--recompile-scripts"),
        REFRESH_DEPENDENCIES(Argument.Kind.PARAM, "--refresh-dependencies"),
        RERUN_TASKS(Argument.Kind.PARAM, "--rerun-tasks"),

        LOG_DEBUG(Argument.Kind.PARAM, "-d", "--debug"),
        LOG_INFO(Argument.Kind.PARAM, "-i", "--info"),
        LOG_QUIET(Argument.Kind.PARAM, "-q", "--quiet"),

        STACKTRACE(Argument.Kind.PARAM, "-s", "--stacktrace"),
        STACKTRACE_FULL(Argument.Kind.PARAM, "-S", "--full-stacktrace"),

        DAEMON(Argument.Kind.UNSUPPORTED, "--no-daemon"),
        NO_DAEMON(Argument.Kind.UNSUPPORTED, "--daemon"),
        HELP(Argument.Kind.UNSUPPORTED, "--help", "-h", "-?"),
        FOREGROUND(Argument.Kind.UNSUPPORTED, "--foreground"),
        GUI(Argument.Kind.UNSUPPORTED, "--gui"),
        PROFILE(Argument.Kind.UNSUPPORTED, "--profile"),
        STATUS(Argument.Kind.UNSUPPORTED, "--status"),
        STOP(Argument.Kind.UNSUPPORTED, "--stop"),
        CONTINUOUS(Argument.Kind.UNSUPPORTED, "--continuous", "-t"),
        NO_SEARCH_UPWARD(Argument.Kind.UNSUPPORTED, "--no-search-upward", "-u"),
        VERSION(Argument.Kind.UNSUPPORTED, "--version", "-v");

        private final Argument.Kind kind;
        private final List<String> flags;

        private Flag(Argument.Kind kind, String... flags) {
            this.kind = kind;
            this.flags = Arrays.asList(flags);
        }
        
        public final String getDescription() {
            return NbBundle.getMessage(GradleCommandLine.class, this.name() + "_DSC"); 
        }
    }

    public enum Property {
        PROJECT(Argument.Kind.PARAM, "-P", "--project-prop"),
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

        SETTINGS_FILE(Argument.Kind.UNSUPPORTED, "-c", "--settings-file"),
        CONSOLE(Argument.Kind.UNSUPPORTED, "--console"),
        GRADLE_USER_HOME(Argument.Kind.UNSUPPORTED, "-g", "--gradle-user-home"),
        INIT_SCRIPT(Argument.Kind.PARAM, "-I", "--init-script"),
        MAX_WORKER(Argument.Kind.PARAM, "--max-worker"),
        PROJECT_DIR(Argument.Kind.PARAM, "-p", "--project-dir"),
        PROJECT_CACHE_DIR(Argument.Kind.UNSUPPORTED, "--project-cache-dir"),
        EXCLUDE_TASK(Argument.Kind.PARAM, "-x", "--exclude-task"),
        IMPORT_BUILD(Argument.Kind.PARAM, "--import-build");

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

        public FlagArgument(Flag flag) {
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
            hash = 79 * hash + Objects.hashCode(this.prop);
            hash = 79 * hash + Objects.hashCode(this.key);
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
            PARSERS.add(new FlagArgument(flag));
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
            for (ArgumentParser parser : PARSERS) {
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
        for(int i = 0; i < cli.length(); i++) {
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

    public List<String> getSupportedCommandLine() {
        List<String> ret = getArgs(EnumSet.of(Argument.Kind.PARAM, Argument.Kind.SYSTEM));
        ret.addAll(tasks);
        return ret;
    }

    public List<String> getFullCommandLine() {
        List<String> ret = getArgs(EnumSet.allOf(Argument.Kind.class));
        ret.addAll(tasks);
        return ret;
    }

    public List<String> getTasks() {
        return new LinkedList<>(tasks);
    }

    public void setTasks(List<String> tasks) {
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
        return arguments.contains(new FlagArgument(flag));
    }

    public void addFlag(Flag flag) {
        arguments.add(new FlagArgument(flag));
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

    public void setExcludedTasks(List<String> excluded) {
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

    public String getProperty(Property type, String key) {
        for (Argument arg : arguments) {
            if (arg instanceof PropertyArgument) {
                PropertyArgument parg = (PropertyArgument) arg;
                if ((parg.prop == type) && parg.key.equals(key)){
                    return parg.value;
                }
            }
        }
        return null;
    }

    public LogLevel getLoglevel() {
        LogLevel ret = LogLevel.NORMAL;
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
        arguments.removeAll(Arrays.asList(Flag.LOG_DEBUG, Flag.LOG_INFO, Flag.LOG_QUIET));
        switch(level) {
            case DEBUG:
                addFlag(Flag.LOG_DEBUG);
                break;
            case INFO:
                addFlag(Flag.LOG_INFO);
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

    @Override
    public void configure(ConfigurableLauncher launcher) {
        launcher.setJvmArguments(getArgs(EnumSet.of(Argument.Kind.SYSTEM)));
        List<String> args = new LinkedList<>(getArgs(EnumSet.of(Argument.Kind.PARAM)));
        args.addAll(tasks);
        launcher.withArguments(args);
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
}
