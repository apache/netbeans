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
package org.netbeans.modules.javaee.wildfly.ide;

import static java.io.File.separatorChar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.api.extexecution.base.Environment;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;
import org.netbeans.modules.javaee.wildfly.util.WildFlyProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Kirill Sorokin
 * @author Libor Kotouc
 */
class WildflyStartRunnable implements Runnable {

    private static final int START_TIMEOUT = 300000;

    private static final String CONF_FILE_NAME = "standalone.conf.bat"; // NOI18N

    private static final String RUN_FILE_NAME = "run.bat";  // NOI18N

    private static final String JBOSS_HOME = "JBOSS_HOME";// NOI18N
    private static final String STANDALONE_SH = separatorChar + "bin" + separatorChar + "standalone.sh";// NOI18N
    private static final String STANDALONE_BAT = separatorChar + "bin" + separatorChar + "standalone.bat";// NOI18N

    private static final String CONF_BAT = separatorChar + "bin" + separatorChar + CONF_FILE_NAME;// NOI18N

    private static final String JAVA_OPTS = "JAVA_OPTS";// NOI18N

    private static final Pattern IF_JAVA_OPTS_PATTERN
            = Pattern.compile(".*if(\\s+not)?\\s+(\"x%" + JAVA_OPTS
                    + "%\"\\s+==\\s+\"x\")\\s+.*", // NOI18N
                    Pattern.DOTALL);

    private static final String NEW_IF_CONDITION_STRING
            = "\"xx\" == \"x\"";                      // NOI18N

    private static final SpecificationVersion JDK_18 = new SpecificationVersion("1.8");

    private final WildflyDeploymentManager dm;
    private final String instanceName;
    private final WildflyStartServer startServer;

    WildflyStartRunnable(WildflyDeploymentManager dm, WildflyStartServer startServer) {
        this.dm = dm;
        this.instanceName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        this.startServer = startServer;
    }

    @Override
    public void run() {

        InstanceProperties ip = dm.getInstanceProperties();

        boolean free = checkPorts(ip);
        if (!free) {
            return;
        }

        Process serverProcess = createProcess(ip);
        if (serverProcess == null) {
            return;
        }

        WildflyOutputSupport outputSupport = WildflyOutputSupport.getInstance(ip, true);
        outputSupport.start(openConsole(), serverProcess, startServer.getMode() == WildflyStartServer.MODE.PROFILE);
        startServer.setConsoleConfigured(true);

        waitForServerToStart(outputSupport);
    }

    private void setupEnvironment(final InstanceProperties ip, Environment env) {

        WildFlyProperties properties = dm.getProperties();
        // get Java platform that will run the server
        JavaPlatform platform = properties.getJavaPlatform();
        // set the JAVA_OPTS value
        String javaOpts = properties.getJavaOpts();
        StringBuilder javaOptsBuilder = new StringBuilder(javaOpts);
        if (startServer.getMode() == WildflyStartServer.MODE.PROFILE) {
            String logManagerJar = findLogManager(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
            if (!logManagerJar.isEmpty()) {
                javaOptsBuilder.append(" -Xbootclasspath/p:").append(logManagerJar)
                        .append(" -Djava.util.logging.manager=org.jboss.logmanager.LogManager");
                FileObject loggingProperties = FileUtil.toFileObject(new File(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR)
                        + separatorChar + "standalone" + separatorChar + "configuration", "logging.properties")); // NOI18N

                javaOptsBuilder.append(" -Dlogging.configuration=").append(loggingProperties.toURL());
            }
        }
        if (platform.getSpecification().getVersion().compareTo(JDK_18) < 0) {
            javaOptsBuilder.append(" -XX:MaxPermSize=256m");
        }
        if ("64".equals(platform.getSystemProperties().get("sun.arch.data.model"))) {
            javaOptsBuilder.append(" -server");
        }
        // use the IDE proxy settings if the 'use proxy' checkbox is selected
        // do not override a property if it was set manually by the user
        if (properties.getProxyEnabled()) {
            final String[] PROXY_PROPS = {
                "http.proxyHost", // NOI18N
                "http.proxyPort", // NOI18N
                "http.nonProxyHosts", // NOI18N
                "https.proxyHost", // NOI18N
                "https.proxyPort", // NOI18N
            };

            for (String prop : PROXY_PROPS) {
                if (!javaOpts.contains(prop)) {
                    String value = System.getProperty(prop);
                    if (value != null) {
                        if ("http.nonProxyHosts".equals(prop)) { // NOI18N
                            try {
                                // remove newline characters, as the value may contain them, see issue #81174
                                BufferedReader br = new BufferedReader(new StringReader(value));
                                String line;
                                StringBuilder noNL = new StringBuilder();
                                while ((line = br.readLine()) != null) {
                                    noNL.append(line);
                                }
                                value = noNL.toString().replace("<", "").replace(">", "").replace(" ", "").replace("\"", "").replace('|', ',').trim();
                            } catch (IOException ioe) {
                                Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(WildflyStartRunnable.class, "ERR_NonProxyHostParsingError"));
                                Logger.getLogger("global").log(Level.WARNING, null, ioe);
                                value = null;
                            }
                        }
                        if (value != null) {
                            javaOptsBuilder.append(" -D").append(prop).append('=').append(value); // NOI18N
                        }
                    }
                }
            }
        }
        if (startServer.getMode() == WildflyStartServer.MODE.DEBUG && !javaOptsBuilder.toString().contains("-Xverify")) { // NOI18N
            // if in debug mode and the verify options not specified manually
            javaOptsBuilder.append(" -Xverify:none"); // NOI18N

        }
        if (startServer.getMode() == WildflyStartServer.MODE.DEBUG && !javaOptsBuilder.toString().contains("-Xdebug")
                && !javaOptsBuilder.toString().contains("-agentlib:jdwp")) { // NOI18N
            // if in debug mode and the debug options not specified manually
            javaOptsBuilder.append(String.format(" -agentlib:jdwp=transport=dt_socket,address=%1s,server=y,suspend=n", dm.getDebuggingPort())); // NOI18N

        }
        if (startServer.getMode() == WildflyStartServer.MODE.PROFILE) {
            javaOptsBuilder.append(" -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager -Djava.awt.headless=true");
        } else {
            javaOptsBuilder.append(" -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true");
        }

        if(ip.getProperty(WildflyPluginProperties.PROPERTY_PORT_OFFSET) != null ) {
            int portOffSet = Integer.parseInt(ip.getProperty(WildflyPluginProperties.PROPERTY_PORT_OFFSET));
            if(portOffSet > 0 ) {
                javaOptsBuilder.append(" -Djboss.socket.binding.port-offset=").append(portOffSet);
            }
        }
        if (ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE) != null) {
            File configFile = new File(ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE));
            if (configFile.exists() && configFile.getParentFile().exists() && configFile.getParentFile().getParentFile().exists()) {
                String baseDir = configFile.getParentFile().getParentFile().getAbsolutePath();
                if (!baseDir.equals(ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR))) {
                    javaOptsBuilder.append(" -Djboss.server.base.dir=").append(baseDir);
                }
            }
        }
        if (ip.getProperty(WildflyPluginProperties.PROPERTY_ADMIN_PORT) != null) {
            try {
                int adminPort = Integer.parseInt(ip.getProperty(WildflyPluginProperties.PROPERTY_ADMIN_PORT));
                Version currentVersion = dm.getServerVersion();
                if (!currentVersion.isWidlfy() || WildflyPluginUtils.WILDFLY_8_2_0.compareTo(currentVersion) <= 0) {
                    javaOptsBuilder.append(" -Djboss.management.http.port=").append(adminPort);
                } else {
                    javaOptsBuilder.append(" -Djboss.management.native.port=").append(adminPort);
                }
            } catch (NumberFormatException ex) {
            }
        }
        if (ip.getProperty(WildflyPluginProperties.PROPERTY_PORT) != null) {
            try {
                int httpConnectorPort = Integer.parseInt(ip.getProperty(WildflyPluginProperties.PROPERTY_PORT));
                javaOptsBuilder.append(" -Djboss.http.port=").append(httpConnectorPort);
            } catch (NumberFormatException ex) {
            }
        }
        for (StartupExtender args : StartupExtender.getExtenders(
                Lookups.singleton(CommonServerBridge.getCommonInstance(ip.getProperty("url"))), getMode(startServer.getMode()))) {
            for (String singleArg : args.getArguments()) {
                javaOptsBuilder.append(' ').append(singleArg);
            }
        }

        // create new environment for server
        javaOpts = javaOptsBuilder.toString();
        Logger.getLogger("global").log(Level.INFO, JAVA_OPTS + "={0}", javaOpts);
        String javaHome = getJavaHome(platform);

        env.setVariable("JAVA", javaHome + File.separatorChar + "bin" + File.separatorChar + "java"); // NOI18N
        env.setVariable("JAVA_HOME", javaHome); // NOI18N
        env.setVariable(JBOSS_HOME, ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR)); // NOI18N
        env.setVariable(JAVA_OPTS, javaOpts); // NOI18N
        if(Utilities.isWindows()) {
            env.setVariable("NOPAUSE", "true"); // NOI18N
        }
    }

    private static StartupExtender.StartMode getMode(WildflyStartServer.MODE jbMode) {
        if (null != jbMode) {
            switch (jbMode) {
                case PROFILE:
                    return StartupExtender.StartMode.PROFILE;
                case DEBUG:
                    return StartupExtender.StartMode.DEBUG;
                default:
                    return StartupExtender.StartMode.NORMAL;
            }
        }
        return StartupExtender.StartMode.NORMAL;
    }

    private boolean checkPorts(final InstanceProperties ip) {
        try {
            String strHTTPConnectorPort = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
            int httpConnectorPort = Integer.parseInt(strHTTPConnectorPort);
            if (httpConnectorPort <= 0) {
                // server will complain hopefully
                return true;
            }
            if (!WildflyPluginUtils.isPortFree(httpConnectorPort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_HTTP_PORT_IN_USE", strHTTPConnectorPort));
                return false;
            }
        } catch (NumberFormatException nfe) {
            // continue and let server to report the problem
        }

        return true;
    }

    private org.netbeans.api.extexecution.base.ProcessBuilder setupBinary(InstanceProperties ip,
            org.netbeans.api.extexecution.base.ProcessBuilder builder) {
        // fix for BZ#179961 -  [J2EE] No able to start profiling JBoss 5.1.0
        String serverRunFileName = getRunFileName(ip);
        if (!new File(serverRunFileName).exists()) {
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_FNF"));//NOI18N
            return null;
        }
        List<String> args = new LinkedList<>();
        if(Utilities.isWindows()) {
            builder.setExecutable("cmd"); // NOI18N
            args.add("/c");  // NOI18N
            args.add(serverRunFileName);
        } else {
            builder.setExecutable(serverRunFileName); // NOI18N
        }
        if (ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE) != null && !"".equals(ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE))) {
            String configFile = ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE);
            args.add("-c"); // NOI18N
            args.add(configFile.substring(configFile.lastIndexOf(File.separatorChar) + 1));
        }
        builder.setArguments(args);
        return builder;
    }

    private String getRunFileName(InstanceProperties ip) {
        SpacesInPathFix fix = new SpacesInPathFix(ip);
        return fix.getRunFileName();
    }

    private static String getJavaHome(JavaPlatform platform) {
        FileObject fo = (FileObject) platform.getInstallFolders().iterator().next();
        return FileUtil.toFile(fo).getAbsolutePath();
    }

    private String createProgressMessage(final String resName) {
        return createProgressMessage(resName, null);
    }

    private String createProgressMessage(final String resName, final String param) {
        return NbBundle.getMessage(WildflyStartRunnable.class, resName, instanceName, param);
    }

    private String getServerRunFileName(final String serverLocation) {
        if(Utilities.isWindows()) {
            return serverLocation + STANDALONE_BAT;
        }
        return serverLocation + STANDALONE_SH;
    }

    private Process createProcess(InstanceProperties ip) {
        org.netbeans.api.extexecution.base.ProcessBuilder builder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        setupEnvironment(ip, builder.getEnvironment());

        builder = setupBinary(ip, builder);
        if (builder == null) {
            return null;
        }

        try {
            File rootFile = null;
            String rootDir = ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR);
            if (rootDir != null) {
                rootFile = new File(rootDir, "bin"); // NOI18N
            }
            if (rootFile != null && !rootFile.isDirectory()) {
                rootFile = null;
            }
            builder.setWorkingDirectory(rootFile.getAbsolutePath());
            return builder.call();
        } catch (java.io.IOException ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
            final String serverLocation = ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR);
            final String serverRunFileName = getServerRunFileName(serverLocation);
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_PD", serverRunFileName));

            return null;
        }
    }

    private InputOutput openConsole() {
        InputOutput io = UISupport.getServerIO(dm.getUrl());
        if (io == null) {
            return null; // finish, it looks like this server instance has been unregistered
        }

        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            // no op
        }
        io.select();
        startServer.setConsoleConfigured(true);
        return io;
    }

    private void fireStartProgressEvent(StateType stateType, String msg) {
        startServer.fireHandleProgressEvent(null, new WildflyDeploymentStatus(ActionType.EXECUTE, CommandType.START, stateType, msg));
    }

    private void waitForServerToStart(WildflyOutputSupport outputSupport) {
        fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS"));

        try {
            boolean result = outputSupport.waitForStart(START_TIMEOUT);

            // reset the need restart flag
            dm.setNeedsRestart(false);

            if (result) {
                fireStartProgressEvent(StateType.COMPLETED, createProgressMessage("MSG_SERVER_STARTED"));
            } else {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED"));
            }
        } catch (TimeoutException ex) {
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_StartServerTimeout"));
        } catch (InterruptedException ex) {
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_StartServerInterrupted"));
            Thread.currentThread().interrupt();
        }

    }

    private String findLogManager(String serverDirPath) {
        StringBuilder logManagerPath = new StringBuilder(serverDirPath);
        logManagerPath.append(File.separatorChar).append("modules").append(File.separatorChar).append("system");
        logManagerPath.append(File.separatorChar).append("layers").append(File.separatorChar).append("base");
        logManagerPath.append(File.separatorChar).append("org").append(File.separatorChar).append("jboss");
        logManagerPath.append(File.separatorChar).append("logmanager").append(File.separatorChar).append("main");
        FileObject logManagerDir = FileUtil.toFileObject(new File(logManagerPath.toString()));
        for (FileObject child : logManagerDir.getChildren()) {
            if (child.isData() && "jar".equalsIgnoreCase(child.getExt())) {
                return child.getPath();
            }
        }
        return "";
    }

    // Fix for BZ#179961 -  [J2EE] No able to start profiling JBoss 5.1.0
    private class SpacesInPathFix {

        SpacesInPathFix(InstanceProperties ip) {
            myProps = ip;
            needChange = runFileNeedChange();
        }

        String getRunFileName() {
            String serverLocation = getProperties().getProperty(
                    WildflyPluginProperties.PROPERTY_ROOT_DIR);
            String serverRunFileName = getServerRunFileName(serverLocation);
            if (needChange) {
                String contentRun = readFile(serverRunFileName);
                String contentConf = readFile(serverLocation + CONF_BAT);
                Matcher matcherRun = IF_JAVA_OPTS_PATTERN.matcher(contentRun);
                Matcher matcherConf = contentConf != null
                        ? IF_JAVA_OPTS_PATTERN.matcher(contentConf)
                        : null;

                boolean needChangeRun = matcherRun.matches();
                boolean needChangeConf = matcherConf != null && matcherConf.matches();
                try {
                    if (needChangeRun || needChangeConf) {
                        File startBat = Files.createTempFile(RUN_FILE_NAME, ".bat").toFile(); // NOI18N
                        File confBat = null;
                        if (contentConf != null) {
                            confBat = Files.createTempFile(// NOI18N
                                    startBat.getParentFile().toPath(), CONF_FILE_NAME, ".bat").toFile(); // NOI18N
                        }
                        startBat.deleteOnExit();
                        contentRun = replaceJavaOpts(contentRun, matcherRun);
                        if (confBat != null) {
                            contentRun = contentRun.replace(CONF_FILE_NAME, confBat.getName());
                        }
                        writeFile(startBat, contentRun);

                        if (confBat != null) {
                            confBat.deleteOnExit();
                            contentConf = replaceJavaOpts(contentConf, matcherConf);
                            writeFile(confBat, contentConf);
                        }
                        return startBat.getAbsolutePath();
                    }
                } catch (IOException e) {
                    Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(
                            WildflyStartRunnable.class, "ERR_WriteError"));          // NOI18N
                    Logger.getLogger("global").log(Level.WARNING, null, e);     // NOI18N
                }
            }
            return serverRunFileName;
        }

        private String replaceJavaOpts(String content, Matcher matcher) {
            String result = content;
            int start = 0;
            List<String> replacementString = new ArrayList<String>(1);
            while (matcher.find(start)) {
                if (matcher.groupCount() <= 1) {
                    continue;
                }
                start = matcher.end(2);
                replacementString.add(matcher.group(2));
            }
            for (String replace : replacementString) {
                result = result.replace(replace, NEW_IF_CONDITION_STRING);
            }
            return result;
        }

        private void writeFile(File file, String content) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(content);
            } catch (IOException e) {
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(
                        WildflyStartRunnable.class, "ERR_WriteError"));              // NOI18N
                Logger.getLogger("global").log(Level.WARNING, null, e);     // NOI18N
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    Logger.getLogger("global").log(Level.WARNING, null, e); // NOI18N
                }
            }
        }

        private String readFile(String file) {
            StringBuilder builder = null;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new FileReader(new File(file)));
                builder = new StringBuilder();

                String line = "";
                do {
                    builder.append(line);
                    builder.append("\r\n");     // NOI18N
                    line = reader.readLine();
                } while (line != null);
            } catch (IOException e) {
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(
                        WildflyStartRunnable.class, "ERR_ReadError"));       // NOI18N
                Logger.getLogger("global").log(Level.WARNING, null, e); // NOI18N
                return null;
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    Logger.getLogger("global").log(Level.WARNING, null, e);// NOI18N
                }
            }
            return builder.toString();
        }

        private InstanceProperties getProperties() {
            return myProps;
        }

        private boolean runFileNeedChange() {
            WildFlyProperties properties = dm.getProperties();
            if (properties.isVersion(WildflyPluginUtils.JBOSS_7_0_0)) {
                return false;
            }
            return false;
        }

        private InstanceProperties myProps;
        private boolean needChange;
    }

}
