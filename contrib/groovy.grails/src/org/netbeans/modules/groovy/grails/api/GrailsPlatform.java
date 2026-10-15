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

package org.netbeans.modules.groovy.grails.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.WrapperProcess;
import org.netbeans.modules.groovy.grails.server.GrailsInstanceProvider;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.*;

/**
 * Class providing the access to basic Grails runtime routines.
 * The class may not be configured and the method {@link #isConfigured()} can
 * be used to find out the state.
 *
 * @author Petr Hejl
 */
// TODO instance should be always configured in future
// TODO more appropriate would be getDefault and forProject
@Messages({"MSG_GrailsNotConfigured=Grails not configured. Please go to Tools/Options/Miscellaneous/Groovy and setup your Grails home."})
public final class GrailsPlatform {

    public static final String IDE_RUN_COMMAND = "run-app"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(GrailsPlatform.class.getName());

    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath(new URL[] {});

    private static final Set<String> GUARDED_COMMANDS = new HashSet<String>();

    static {
        Collections.addAll(GUARDED_COMMANDS, "run-app", "run-app-https", "run-war", "shell"); //NOI18N
    }

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private static GrailsPlatform instance;

    private Version version;

    private ClassPath classpath;

    private GrailsPlatform() {
        super();
    }

    /**
     * Return the instance representing the IDE configured Grails runtime.
     *
     * @return the instance representing the IDE configured Grails runtime
     */
    public static synchronized GrailsPlatform getDefault() {
        if (instance == null) {
            instance = new GrailsPlatform();
            GrailsSettings.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (GrailsSettings.GRAILS_BASE_PROPERTY.equals(evt.getPropertyName())) {
                        instance.reload();
                        GrailsInstanceProvider.getInstance().runtimeChanged();
                    }
                }
            });
            instance.reload();
        }
        return instance;
    }

    /**
     * Creates the callable spawning the command (process) described
     * by the command descriptor. Usually you don't need to use this method
     * directly as most of use cases can be solved with {@link ExecutionSupport}.
     *
     * @param descriptor descriptor of the command and its environment
     * @return the callable spawning the command (process)
     * @throws IllegalStateException if the runtime is not configured
     *
     * @see #isConfigured()
     * @see ExecutionSupport
     */
    public Callable<Process> createCommand(CommandDescriptor descriptor) {
        Parameters.notNull("descriptor", descriptor);

        if (!isConfigured()) {
            Message dialogMessage = new NotifyDescriptor.Message(NbBundle.getMessage(GrailsPlatform.class, "MSG_GrailsNotConfigured"));
            if (DialogDisplayer.getDefault().notify(dialogMessage) == NotifyDescriptor.OK_OPTION) {
                return new GrailsCallable(descriptor);
            }
        }
        return new GrailsCallable(descriptor);
    }

    /**
     * Returns <code>true</code> if the runtime is configured (usable).
     *
     * @return <code>true</code> if the runtime is configured (usable)
     */
    public boolean isConfigured() {
        String grailsBase = GrailsSettings.getInstance().getGrailsBase();
        if (grailsBase == null) {
            return false;
        }

        return RuntimeHelper.isValidRuntime(new File(grailsBase));
    }

    public ClassPath getClassPath() {
        synchronized (this) {
            if (classpath != null) {
                return classpath;
            }

            if (!isConfigured()) {
                classpath = EMPTY_CLASSPATH;
                return classpath;
            }

            File grailsHome = getGrailsHome();
            if (!grailsHome.exists()) {
                classpath = EMPTY_CLASSPATH;
                return classpath;
            }

            List<File> jars = new ArrayList<File>();

            File distDir = new File(grailsHome, "dist"); // NOI18N
            File[] files = distDir.listFiles();
            if (files != null) {
                jars.addAll(Arrays.asList(files));
            }

            File libDir = new File(grailsHome, "lib"); // NOI18N
            List<File> libJars = getJarsRecursively(libDir);
            if (libJars != null) {
                jars.addAll(libJars);
            }

            List<URL> urls = new ArrayList<URL>(jars.size());

            for (File f : jars) {
                try {
                    if (f.isFile()) {
                        URL entry = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(entry)) {
                            entry = FileUtil.getArchiveRoot(entry);
                            urls.add(entry);
                        }
                    }
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }

            classpath = ClassPathSupport.createClassPath(urls.toArray(new URL[0]));
            return classpath;
        }
    }
    
    private List<File> getJarsRecursively(File parentDir) {
        List<File> jars = new ArrayList<File>();
        if (parentDir != null) {
            for (File file : parentDir.listFiles()) {
                if (file.isDirectory()) {
                    jars.addAll(getJarsRecursively(file));
                } else {
                    if (file.getName().toLowerCase().endsWith(".jar")) { // NOI18N
                        jars.add(file);
                    }
                }
            }
        }
        return jars;
    }

    // TODO not public API unless it is really needed
    public Version getVersion() {
        synchronized (this) {
            if (version != null) {
                return version;
            }

            String grailsBase = GrailsSettings.getInstance().getGrailsBase();
            try {
                if (grailsBase != null) {
                    String stringVersion = RuntimeHelper.getRuntimeVersion(new File(grailsBase));
                    if (stringVersion != null) {
                        version = Version.valueOf(stringVersion);
                    } else {
                        version = Version.VERSION_DEFAULT;
                    }
                } else {
                    version = Version.VERSION_DEFAULT;
                }
            } catch (IllegalArgumentException ex) {
                version = Version.VERSION_DEFAULT;
            }

            return version;
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Reloads the runtime instance variables.
     */
    private void reload() {
        synchronized (this) {
            version = null;
            classpath = null;
        }

        changeSupport.fireChange();
        
        // figure out the version on background
        // default executor as general purpose should be enough for this
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                synchronized (GrailsPlatform.this) {
                    if (version != null) {
                        return;
                    }

                    String grailsBase = GrailsSettings.getInstance().getGrailsBase();
                    try {
                        if (grailsBase != null) {
                            String stringVersion = RuntimeHelper.getRuntimeVersion(new File(grailsBase));
                            if (stringVersion != null) {
                                version = Version.valueOf(stringVersion);
                            } else {
                                version = Version.VERSION_DEFAULT;
                            }
                        } else {
                            version = Version.VERSION_DEFAULT;
                        }
                    } catch (IllegalArgumentException ex) {
                        version = Version.VERSION_DEFAULT;
                    }
                }
            }
        });
    }

    /**
     * Returns the grails home of the configured runtime.
     *
     * @return the grails home
     * @throws IllegalStateException if the runtime is not configured
     */
    public File getGrailsHome() {
        String grailsBase = GrailsSettings.getInstance().getGrailsBase();
        if (grailsBase == null || !RuntimeHelper.isValidRuntime(new File(grailsBase))) {
            throw new IllegalStateException("Grails not configured"); // NOI18N
        }

        return new File(grailsBase);
    }

    private static String createJvmArguments(String vmOptions, Properties properties) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        if (vmOptions != null) {
            builder.append(vmOptions);
        }

        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String key = e.nextElement().toString();
            String value = properties.getProperty(key);
            if (value != null) {
                if (i > 0 || vmOptions != null) {
                    builder.append(" "); // NOI18N
                }
                builder.append("-D").append(key); // NOI18N
                builder.append("="); // NOI18N
                builder.append(value);
                i++;
            }
        }
        return builder.toString();
    }

    private static String createCommandArguments(String[] arguments) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                builder.append(" "); // NOI18N
            }
            builder.append(arguments[i]);
        }
        return builder.toString();
    }

    private static void checkForServer(CommandDescriptor descriptor, Process process) {
        if (IDE_RUN_COMMAND.equals(descriptor.getName())) { // NOI18N
            Project project = FileOwnerQuery.getOwner(
                    FileUtil.toFileObject(descriptor.getDirectory()));
            if (project != null) {
                GrailsInstanceProvider.getInstance().serverStarted(project, process);
            }
        }
    }

    /**
     * Class describing the command to invoke and its environment.
     *
     * This class is <i>Immutable</i>.
     */
    public static final class CommandDescriptor {

        private final String name;

        private final File directory;

        private final GrailsProjectConfig config;

        private final String[] arguments;

        private final Properties props;

        private final boolean debug;

        public static CommandDescriptor forProject(String name, File directory,
                GrailsProjectConfig config, String[] arguments, Properties props) {

            return new CommandDescriptor(name, directory, config, arguments, props, false);
        }

        public static CommandDescriptor forProject(String name, File directory,
                GrailsProjectConfig config, String[] arguments, Properties props, boolean debug) {

            return new CommandDescriptor(name, directory, config, arguments, props, debug);
        }

        /**
         * Creates the full customizable command descriptor.
         *
         * @param name command name
         * @param directory working directory
         * @param env grails environment
         * @param arguments command arguments
         * @param props environment properties
         */
        private CommandDescriptor(String name, File directory, GrailsProjectConfig config,
                String[] arguments, Properties props, boolean debug) {
            this.name = name;
            this.directory = directory;
            this.config = config;
            this.arguments = arguments.clone();
            this.props = props != null ? new Properties(props) : new Properties();
            this.debug = debug;
        }

        /**
         * Returns the command name.
         *
         * @return the command name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the working directory.
         *
         * @return the working directory
         */
        public File getDirectory() {
            return directory;
        }

        public GrailsProjectConfig getProjectConfig() {
            return config;
        }

        /**
         * Returns the command arguments.
         *
         * @return the command arguments
         */
        public String[] getArguments() {
            return arguments.clone();
        }

        /**
         * Returns the environment properties.
         *
         * @return the environment properties
         */
        public Properties getProps() {
            return new Properties(props);
        }

        /**
         * Returns debugging flag.
         * 
         * @return debugging flag
         */
        public boolean isDebug() {
            return debug;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CommandDescriptor other = (CommandDescriptor) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }


    }

    public static final class Version implements Comparable<Version> {

        public static final Version VERSION_DEFAULT = new Version(1, null, null, null, null);

        public static final Version VERSION_1_1 = new Version(1, 1, null, null, null);
        
        public static final Version VERSION_2 = new Version(2, null, null, null, null);
        
        public static final Version VERSION_3 = new Version(3, null, null, null, null);
        
        private final int major;

        private final Integer minor;

        private final Integer micro;

        private final Integer update;

        private final String qualifier;

        private String asString;

        protected Version(int major, Integer minor, Integer micro, Integer update, String qualifier) {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.update = update;
            this.qualifier = qualifier;
        }

        public static Version valueOf(String version) {
            // Until version 1.3 the Grails versioning pattern was something like 1.1.1-RC1
            String[] stringParts = version.split("-"); // NOI18N

            String qualifier = null;
            if (stringParts.length > 2) {
                throw new IllegalArgumentException(version);
            }
            if (stringParts.length == 2) {
                qualifier = stringParts[1];
            }


            String[] numberParts = stringParts[0].split("\\."); // NOI18N
            if (numberParts.length < 1 || numberParts.length > 4) {
                throw new IllegalArgumentException(version);
            }

            Integer[] parsed = new Integer[4];
            // Since version 1.4 format is always either of type 2.2.0 or 2.2.0.RC1
            // Which means the fourth part is either empty or some qualifier and thats
            // why we want to parse number 3times at maximum
            for (int i = 0; i < Math.min(numberParts.length, 3); i++) {
                try {
                    parsed[i] = Integer.valueOf(numberParts[i]);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(version, ex);
                }
            }
            if (numberParts.length == 4) {
                qualifier = numberParts[3];
            }

            return new Version(parsed[0], parsed[1], parsed[2], parsed[3], qualifier);
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor == null ? 0 : minor.intValue();
        }

        public int getMicro() {
            return micro == null ? 0 : micro.intValue();
        }

        public int getUpdate() {
            return update == null ? 0 : update.intValue();
        }

        public String getQualifier() {
            return qualifier == null ? "" : qualifier; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.getMajor() != other.getMajor()) {
                return false;
            }
            if (this.getMinor() != other.getMinor()) {
                return false;
            }
            if (this.getMicro() != other.getMicro()) {
                return false;
            }
            if (this.getUpdate() != other.getUpdate()) {
                return false;
            }
            if (!this.getQualifier().equals(other.getQualifier())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.getMajor();
            hash = 71 * hash + this.getMinor();
            hash = 71 * hash + this.getMicro();
            hash = 71 * hash + this.getUpdate();
            hash = 71 * hash + this.getQualifier().hashCode();
            return hash;
        }

        @Override
        public int compareTo(Version o) {
            if (this == o) {
                return 0;
            }

            int result = this.getMajor() - o.getMajor();
            if (result != 0) {
                return result;
            }

            result = this.getMinor() - o.getMinor();
            if (result != 0) {
                return result;
            }

            result = this.getMicro() - o.getMicro();
            if (result != 0) {
                return result;
            }

            result = this.getUpdate() - o.getUpdate();
            if (result != 0) {
                return result;
            }

            return this.getQualifier().compareTo(o.getQualifier());
        }

        @Override
        public String toString() {
            if (asString == null) {
                StringBuilder builder = new StringBuilder();
                builder.append(major);

                if (minor != null || micro != null || update != null) {
                    appendSeparator(builder);
                    builder.append(minor == null ? 0 : minor);
                }
                if (micro != null || update != null) {
                    appendSeparator(builder);
                    builder.append(micro == null ? 0 : micro);
                }
                if (update != null) {
                    appendSeparator(builder);
                    builder.append(update == null ? 0 : update);
                }
                if (qualifier != null) {
                    // If we have first three numbers and no "update" version, we are
                    // probably in situation of Grails version 1.4 and higher because since
                    // then format is always either of type 2.2.0 or 2.2.0.RC1
                    if (minor != null && micro != null && update == null) {
                        builder.append('.'); // NOI18N
                    } else {
                        builder.append('-'); // NOI18N
                    }
                    builder.append(qualifier);
                }

                asString = builder.toString();
            }
            return asString;
        }

        private void appendSeparator(StringBuilder builder) {
            if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '.') { // NOI18N
                builder.append('.'); // NOI18N
            }
        }

    }

    private static class GrailsCallable implements Callable<Process> {

        // FIXME: get rid of those proxy constants as soon as some NB Proxy API is available
        private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"; // NOI18N

        private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"; // NOI18N

        private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"; // NOI18N

        private final CommandDescriptor descriptor;

        public GrailsCallable(CommandDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public Process call() throws Exception {
            
            Version platformVersion = GrailsPlatform.getDefault().getVersion();
            File grailsExecutable = null;
            //if we're still using grails 1 we have a special debug command:
            if (platformVersion.compareTo(Version.VERSION_2) < 0) {
                grailsExecutable = RuntimeHelper.getGrailsExecutable(
                    new File(GrailsSettings.getInstance().getGrailsBase()), true);
            } else {
                grailsExecutable = RuntimeHelper.getGrailsExecutable(
                    new File(GrailsSettings.getInstance().getGrailsBase()), false);
            }
            if (grailsExecutable == null || !grailsExecutable.exists()) {
                LOGGER.log(Level.WARNING, "Executable doesn''t exist: {0}", grailsExecutable.getAbsolutePath());

                return null;
            }

            LOGGER.log(Level.FINEST, "About to run: {0}", descriptor.getName());

            Properties props = new Properties(descriptor.getProps());
            GrailsEnvironment env = descriptor.getProjectConfig() != null
                    ? descriptor.getProjectConfig().getEnvironment()
                    : null;

            if (env != null && env.isCustom()) {
                props.setProperty("grails.env", env.toString()); // NOI18N
            }

            if (descriptor.getProjectConfig() != null && IDE_RUN_COMMAND.equals(descriptor.getName())) {
                String port = descriptor.getProjectConfig().getPort();
                if (port != null) {
                    props.setProperty("server.port", port); // NOI18N
                }
            }

            // XXX this is workaround for jline bug (native access to console on windows) used by grails
            props.setProperty("jline.WindowsTerminal.directConsole", "false"); // NOI18N

            String proxyString = getNetBeansHttpProxy(props);

            StringBuilder command = new StringBuilder();
            if (env != null && !env.isCustom()) {
                command.append(" ").append(env.toString());
            }
            command.append(" ").append(descriptor.getName());
            if (descriptor.isDebug()) {                
                if (platformVersion.compareTo(Version.VERSION_3) >= 0) {
                    command.append(" ").append("--debug-jvm"); // NOI18N
                } else if (platformVersion.compareTo(Version.VERSION_2) >= 0) {
                    command.append(" ").append("--debug-fork"); // NOI18N
                }
            }
            command.append(" ").append(createCommandArguments(descriptor.getArguments()));            

            String preProcessUUID = UUID.randomUUID().toString();

            LOGGER.log(Level.FINEST, "Command is: {0}", command.toString());

            NbProcessDescriptor grailsProcessDesc = new NbProcessDescriptor(
                    grailsExecutable.getAbsolutePath(), command.toString());

            String javaHome = null;
            JavaPlatform javaPlatform;
            if (descriptor.getProjectConfig() != null) {
                javaPlatform = descriptor.getProjectConfig().getJavaPlatform();
            } else {
                javaPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            }

            Collection<FileObject> dirs = javaPlatform.getInstallFolders();
            if (dirs.size() == 1) {
                File file = FileUtil.toFile(dirs.iterator().next());
                if (file != null) {
                    javaHome = file.getAbsolutePath();
                }
            }

            String vmOptions = null;
            if (descriptor.getProjectConfig() != null) {
                vmOptions = descriptor.getProjectConfig().getVmOptions();
                if (vmOptions != null && "".equals(vmOptions.trim())) {
                    vmOptions = null;
                }
            }
            String[] envp = new String[] {
                "GRAILS_HOME=" + GrailsSettings.getInstance().getGrailsBase(), // NOI18N
                "JAVA_HOME=" + javaHome, // NOI18N
                "http_proxy=" + proxyString, // NOI18N
                "HTTP_PROXY=" + proxyString, // NOI18N
                "JAVA_OPTS=" + createJvmArguments(vmOptions, props)
            };

            // no executable check before java6
            Process process = null;
            try {
                process = new WrapperProcess(
                        grailsProcessDesc.exec(null, envp, true, descriptor.getDirectory()),
                        preProcessUUID);
            } catch (IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                        NbBundle.getMessage(GrailsPlatform.class, "MSG_StartFailedIOE",
                                grailsExecutable.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(desc);
                throw ex;
            }

            checkForServer(descriptor, process);
            return process;
        }

        /**
         * FIXME: get rid of the whole method as soon as some NB Proxy API is
         * available.
         */
        private static String getNetBeansHttpProxy(Properties props) {
            String host = System.getProperty("http.proxyHost"); // NOI18N
            if (host == null) {
                return null;
            }

            String portHttp = System.getProperty("http.proxyPort"); // NOI18N
            int port;

            try {
                port = Integer.parseInt(portHttp);
            } catch (NumberFormatException e) {
                port = 8080;
            }

            Preferences prefs = NbPreferences.root().node("org/netbeans/core"); // NOI18N
            boolean useAuth = prefs.getBoolean(USE_PROXY_AUTHENTICATION, false);

            String auth = "";
            if (useAuth) {
                String username = prefs.get(PROXY_AUTHENTICATION_USERNAME, "");
                String password = prefs.get(PROXY_AUTHENTICATION_PASSWORD, "");

                auth = username + ":" + password + '@'; // NOI18N

                if (!props.contains("http.proxyUser")) { // NOI18N
                    props.setProperty("http.proxyUser", prefs.get(PROXY_AUTHENTICATION_USERNAME, "")); // NOI18N
                }
                if (!props.contains("http.proxyPassword")) { // NOI18N
                    props.setProperty("http.proxyPassword", prefs.get(PROXY_AUTHENTICATION_PASSWORD, "")); // NOI18N
                }
            }

            if (!props.contains("http.proxyHost")) { // NOI18N
                props.setProperty("http.proxyHost", host); // NOI18N
            }
            if (!props.contains("http.proxyPort")) { // NOI18N
                props.setProperty("http.proxyPort", Integer.toString(port)); // NOI18N
            }

            // Gem requires "http://" in front of the port name if it's not already there
            if (host.indexOf(':') == -1) {
                host = "http://" + auth + host; // NOI18N
            }

            return host + ":" + port; // NOI18N
        }

    }
}
