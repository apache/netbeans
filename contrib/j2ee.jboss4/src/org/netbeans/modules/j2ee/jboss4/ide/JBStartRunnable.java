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

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.openide.execution.NbProcessDescriptor;
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
class JBStartRunnable implements Runnable {
    
    private static final int START_TIMEOUT = 300000;
    
    private static final String CONF_FILE_NAME = 
            "run.conf.bat";                             // NOI18N
    
    private static final String RUN_FILE_NAME = 
            "run.bat";                                  // NOI18N
    
    private static final String JBOSS_HOME 
            = "JBOSS_HOME";                             // NOI18N

    private static final String STARTUP_SH = File.separator + 
            "bin" + File.separator + "run.sh";          // NOI18N
    private static final String STANDALONE_SH = File.separator + 
            "bin" + File.separator + "standalone.sh";          // NOI18N
    private static final String STARTUP_BAT = File.separator + 
            "bin" + File.separator + RUN_FILE_NAME;     // NOI18N
    private static final String STANDALONE_BAT = File.separator + 
            "bin" + File.separator + "standalone.bat";     // NOI18N
                             
    private static final String CONF_BAT = File.separator + 
            "bin" + File.separator + CONF_FILE_NAME;    // NOI18N
    
    private static final String JAVA_OPTS = "JAVA_OPTS";// NOI18N   
    
    private static final Pattern IF_JAVA_OPTS_PATTERN =
        Pattern.compile(".*if(\\s+not)?\\s+(\"x%"+JAVA_OPTS+
                "%\"\\s+==\\s+\"x\")\\s+.*",            // NOI18N 
                Pattern.DOTALL);
    
    private static final String NEW_IF_CONDITION_STRING = 
                "\"xx\" == \"x\"";                      // NOI18N 
    
    private static final SpecificationVersion 
        JDK_14 = new SpecificationVersion("1.4");       // NOI18N

    private static final Logger LOGGER = Logger.getLogger(JBStartRunnable.class.getName());

    private JBDeploymentManager dm;
    private String instanceName;
    private JBStartServer startServer;

    JBStartRunnable(JBDeploymentManager dm, JBStartServer startServer) {
        this.dm = dm;
        this.instanceName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        this.startServer = startServer;
    }

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

        JBOutputSupport outputSupport = JBOutputSupport.getInstance(ip, true);
        outputSupport.start(openConsole(), serverProcess, startServer.getMode() == JBStartServer.MODE.PROFILE);
        
        waitForServerToStart(outputSupport);
    }

    private String[] createEnvironment(final InstanceProperties ip) {
        
        JBProperties properties = dm.getProperties();

        // set the JAVA_OPTS value
        String javaOpts = properties.getJavaOpts();
        StringBuilder javaOptsBuilder = new StringBuilder(javaOpts);

        boolean version5 = properties.isVersion(JBPluginUtils.JBOSS_5_0_0);

        if (!version5) {   // if  JB version 4.x
            // use the IDE proxy settings if the 'use proxy' checkbox is selected
            // do not override a property if it was set manually by the user
            if (properties.getProxyEnabled()) {
                final String[] PROXY_PROPS = {
                "http.proxyHost",       // NOI18N
                "http.proxyPort",       // NOI18N
                "http.nonProxyHosts",   // NOI18N
                "https.proxyHost",      // NOI18N
                "https.proxyPort",      // NOI18N
                };

                for (String prop : PROXY_PROPS) {
                    if (javaOpts.indexOf(prop) == -1) {
                        String value = System.getProperty(prop);
                        if (value != null) {
                            if ("http.nonProxyHosts".equals(prop)) { // NOI18N
                                try {
                                    // remove newline characters, as the value may contain them, see issue #81174
                                    BufferedReader br = new BufferedReader(new StringReader(value));
                                    String line = null;
                                    StringBuilder noNL = new StringBuilder();
                                    while ((line = br.readLine()) != null) {
                                        noNL.append(line);
                                    }
                                    value = noNL.toString();

                                    // enclose the host list in double quotes because it may contain spaces
                                    value = "\"" + value + "\""; // NOI18N
                            }
                            catch (IOException ioe) {
                                Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(JBStartRunnable.class, "ERR_NonProxyHostParsingError"));
                                Logger.getLogger("global").log(Level.WARNING, null, ioe);
                                    value = null;
                                }
                            }
                            if (value != null) {
                            javaOptsBuilder.append(" -D").append(prop).append("=").append(value); // NOI18N
                            }
                        }
                    }
                }
            }
        }

        // get Java platform that will run the server
        JavaPlatform platform = getJavaPlatform(properties);

        if (startServer.getMode() == JBStartServer.MODE.DEBUG && !haveDebugOptions(javaOptsBuilder.toString())) {
            // if in debug mode and the debug options not specified manually
            if (platform.getSpecification().getVersion().compareTo(JDK_14) <= 0) {
                javaOptsBuilder.append(" -classic");
                javaOptsBuilder.append(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="). // NOI18N
                                append(dm.getDebuggingPort()).
                                append(",server=y,suspend=n"); // NOI18N
            } else {
                javaOptsBuilder.append(" -agentlib:jdwp=transport=dt_socket,address="). // NOI18N
                                append(dm.getDebuggingPort()).
                                append(",server=y,suspend=n"); // NOI18N
            }

        } else if (startServer.getMode() == JBStartServer.MODE.PROFILE) {
            if (properties.isVersion(JBPluginUtils.JBOSS_7_0_0)) {
                javaOptsBuilder.append(" ").append("-Djava.util.logging.manager=org.jboss.logmanager.LogManager");
            } else if (properties.isVersion(JBPluginUtils.JBOSS_6_0_0)) {
                javaOptsBuilder.append(" ").append("-Djboss.platform.mbeanserver")
                        .append(" ").append("-Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl");
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
        String javaHome = getJavaHome(platform);

        String envp[] = new String[] {
            "JAVA=" + javaHome + File.separator +"bin" + File.separator + "java",   // NOI18N
            "JAVA_HOME=" + javaHome,            // NOI18N
            JBOSS_HOME+"="+ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR),    // NOI18N
            JAVA_OPTS+"=" + javaOpts,            // NOI18N
        };
        return envp;
    }

    private static boolean haveDebugOptions(String opts) {
        if (opts.contains("-Xdebug")) {             // NOI18N
            return true;
        }
        String jdwpAgent = "-agentlib:jdwp";        // NOI18N
        int jdwpAgentIndex = opts.indexOf(jdwpAgent);
        if (jdwpAgentIndex < 0) {
            return false;
        }
        int i = jdwpAgentIndex + jdwpAgent.length();
        if (i >= opts.length()) {
            return true;
        }
        char c = opts.charAt(i);
        return Character.isWhitespace(c) || c == '=';
    }

    private static StartupExtender.StartMode getMode(JBStartServer.MODE jbMode) {
        if (JBStartServer.MODE.PROFILE.equals(jbMode)) {
            return StartupExtender.StartMode.PROFILE;
        } else if (JBStartServer.MODE.DEBUG.equals(jbMode)) {
            return StartupExtender.StartMode.DEBUG;
        } else {
            return StartupExtender.StartMode.NORMAL;
        }
    }

    private JavaPlatform getJavaPlatform(JBProperties properties) {
        JavaPlatform platform = properties.getJavaPlatform();
        if (platform.getInstallFolders().size() <= 0) {
            LOGGER.log(Level.INFO, "The Java Platform used by JBoss is broken; using the default one");
            return JavaPlatform.getDefault();
        }
        return platform;
    }

    private boolean checkPorts(final InstanceProperties ip) {

        try {
            String serverName = ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);

            String strHTTPConnectorPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
            int httpConnectorPort = Integer.parseInt(strHTTPConnectorPort);
            if (httpConnectorPort <= 0) {
                // server will complain hopefully
                return true;
            }
            if (!JBPluginUtils.isPortFree(httpConnectorPort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_HTTP_PORT_IN_USE", strHTTPConnectorPort));
                return false;
            }

            String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);

            // port -1 means the service is not binded at all

            String strJNPServicePort = JBPluginUtils.getJnpPort(serverDir);
            int JNPServicePort = Integer.parseInt(strJNPServicePort);
            if (JNPServicePort >= 0 && !JBPluginUtils.isPortFree(JNPServicePort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_JNP_PORT_IN_USE", strJNPServicePort));//NOI18N
                return false;
            }

            String strRMINamingServicePort = JBPluginUtils.getRMINamingServicePort(serverDir);
            int RMINamingServicePort = Integer.parseInt(strRMINamingServicePort);
            if (RMINamingServicePort >= 0 && !JBPluginUtils.isPortFree(RMINamingServicePort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_RMI_PORT_IN_USE", strRMINamingServicePort));//NOI18N
                return false;
            }

            String server = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
            if (!"minimal".equals(server)) { // NOI18N
                String strRMIInvokerPort = JBPluginUtils.getRMIInvokerPort(serverDir);
                int RMIInvokerPort = Integer.parseInt(strRMIInvokerPort);
                if (RMIInvokerPort >= 0 && !JBPluginUtils.isPortFree(RMIInvokerPort)) {
                    fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_INVOKER_PORT_IN_USE", strRMIInvokerPort));//NOI18N
                    return false;
                }
            }

        } catch (NumberFormatException nfe) {
            // continue and let server to report the problem
        }
        
        return true;
    }

    private NbProcessDescriptor createProcessDescriptor(InstanceProperties ip, 
            String[] envp ) 
    {
        // fix for BZ#179961 -  [J2EE] No able to start profiling JBoss 5.1.0
        String serverRunFileName = getRunFileName(ip, envp);
        if (!new File(serverRunFileName).exists()){
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_FNF"));//NOI18N
            return null;
        }

        final String instanceName = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
        String args = ("all".equals(instanceName) ? "-b 127.0.0.1 " : "") + "-c " + instanceName; // NOI18N
        return new NbProcessDescriptor(serverRunFileName, isJBoss7()? "" : args);
    }
    
    private String getRunFileName( InstanceProperties ip, String[] envp ){
        SpacesInPathFix fix = new SpacesInPathFix( ip , envp );
        return fix.getRunFileName();
    }

    private static String getJavaHome(JavaPlatform platform) {
        FileObject fo = (FileObject)platform.getInstallFolders().iterator().next();
        return FileUtil.toFile(fo).getAbsolutePath();
    }
 
    private String createProgressMessage(final String resName) {
        return createProgressMessage(resName, null);
    }
    
    private String createProgressMessage(final String resName, final String param) {
        return NbBundle.getMessage(JBStartRunnable.class, resName, instanceName, param);
    }

    private Process createProcess(InstanceProperties ip) {
        String envp[] = createEnvironment(ip);
        
        NbProcessDescriptor pd = createProcessDescriptor(ip, envp);
        if (pd == null) {
            return null;
        }

        try {
            File rootFile = null;
            String rootDir = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            if (rootDir != null) {
                rootFile = new File(rootDir, "bin"); // NOI18N
            }
            if (rootFile != null && !rootFile.isDirectory()) {
                rootFile = null;
            }
            return pd.exec(null, envp, true, rootFile);
        } catch (java.io.IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);

            final String serverLocation = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            final String serverRunFileName = serverLocation + (isJBoss7() ? Utilities.isWindows() ? STANDALONE_BAT : STANDALONE_SH : Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH);
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_PD", serverRunFileName));

            return null;
        }
    }
    
    private boolean isJBoss7() {
        return dm.getProperties().isVersion(JBPluginUtils.JBOSS_7_0_0);
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
        
        return io;
    }            

    private void fireStartProgressEvent(StateType stateType, String msg) {
        startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, stateType, msg));
    }
    
    private void waitForServerToStart(JBOutputSupport outputSupport) {
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
    
    // Fix for BZ#179961 -  [J2EE] No able to start profiling JBoss 5.1.0
    private class SpacesInPathFix {
        
        SpacesInPathFix( InstanceProperties ip, String[] envp ) {
            myProps = ip;
            needChange = runFileNeedChange(envp);
        }
        
        String getRunFileName(){
            String serverLocation = getProperties().getProperty(
                    JBPluginProperties.PROPERTY_ROOT_DIR);
            String serverRunFileName = serverLocation + 
                    (isJBoss7() ? Utilities.isWindows() ? STANDALONE_BAT : STANDALONE_SH : Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH);
            if ( needChange ){
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
                            JBStartRunnable.class, "ERR_WriteError"));          // NOI18N
                    Logger.getLogger("global").log(Level.WARNING, null, e);     // NOI18N
                }
            }
            return serverRunFileName;
        }
        
        private String replaceJavaOpts( String content, Matcher matcher ) {
            String result = content;
            int start = 0;
            List<String> replacementString = new ArrayList<String>(1);
            while( matcher.find(start)){
                if ( matcher.groupCount() <=1 ){
                    continue;
                }
                start = matcher.end( 2 );
                replacementString.add( matcher.group(2));
            }
            for( String replace : replacementString ){
                result = result.replace(replace, NEW_IF_CONDITION_STRING );
            }
            return result;
        }
        
        private void writeFile(File file , String content ){
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter( new FileWriter( file ));
                writer.write( content );
            }
            catch (IOException e ){
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(
                    JBStartRunnable.class, "ERR_WriteError"));              // NOI18N
                Logger.getLogger("global").log(Level.WARNING, null, e);     // NOI18N
            }
            finally {
                try {
                    if ( writer!= null ){
                        writer.close();
                    }
                }
                catch (IOException e ){
                    Logger.getLogger("global").log(Level.WARNING, null, e); // NOI18N
                }
            }
        }

        private String readFile( String file ) 
        {
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
                }
                while ( line != null);
            }
            catch (IOException e ){
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(
                        JBStartRunnable.class, "ERR_ReadError"));       // NOI18N
                Logger.getLogger("global").log(Level.WARNING, null, e); // NOI18N
                return null;
            }
            finally {
                try {
                    if ( reader!= null ){
                        reader.close();
                    }
                }
                catch (IOException e ){
                    Logger.getLogger("global").log(Level.WARNING, null, e);// NOI18N
                }
            }
            return builder.toString();
        }
        
        private InstanceProperties getProperties(){
            return myProps;
        }
        
        private boolean runFileNeedChange( String[] envp ){
            JBProperties properties = dm.getProperties();
            if (properties.isVersion(JBPluginUtils.JBOSS_7_0_0)) {
                return false;
            }
            if ( properties.isVersion(JBPluginUtils.JBOSS_5_0_1) && 
                    Utilities.isWindows()) {
                for( String env : envp ){
                    if ( env.startsWith(JAVA_OPTS+"=")){
                        return env.indexOf('"')>=0;
                    }
                }
            }
            return false;
        }
        
        private InstanceProperties myProps;
        private boolean needChange;
    }
    
}
    
