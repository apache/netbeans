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
package org.netbeans.modules.glassfish.tooling.server;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.admin.CommandStartDAS;
import org.netbeans.modules.glassfish.tooling.admin.ResultProcess;
import org.netbeans.modules.glassfish.tooling.admin.ServerAdmin;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.StartupArgs;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.server.parser.JvmConfigReader;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser;
import org.netbeans.modules.glassfish.tooling.utils.JavaUtils;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.tooling.utils.Utils;

/**
 * This class should contain task methods for GF server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerTasks {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    public enum StartMode {
        /** Regular server start. */
        START,
        /** Start server in debug mode. */
        DEBUG,
        /** Start server in profiling mode. */
        PROFILE;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerTasks.class);

    /** Default name of the DAS server. */
    private static final String DAS_NAME = "server";

    /**
     * Build server variables map.
     * <p/>
     * @param server   GlassFish server entity
     * @param javaHome Java SE JDK home used to run Glassfish.
     */
    private static Map varMap(GlassFishServer server, String javaHome) {
        HashMap<String, String> varMap = new HashMap<>();
        varMap.put(ServerUtils.GF_HOME_PROPERTY, server.getServerHome());
        varMap.put(ServerUtils.GF_DOMAIN_ROOT_PROPERTY,
                ServerUtils.getDomainPath(server));
        varMap.put(ServerUtils.GF_JAVA_ROOT_PROPERTY, javaHome);
        varMap.put(ServerUtils.GF_DERBY_ROOT_PROPERTY,
                ServerUtils.getDerbyRoot(server));
        return varMap;
    }

    /**
     * Add java agents into server options.
     * <p/>
     * @param server          GlassFish server entity.
     * @param jvmConfigReader Contains <code>jvm-options</code>
     *                        from <code>domain.xwl</code>.
     */
    private static void addJavaAgent(GlassFishServer server,
            JvmConfigReader jvmConfigReader) {
        List<String> optList = jvmConfigReader.getOptList();
        File serverHome = new File(server.getServerHome());
        File btrace = new File(serverHome, "lib/monitor/btrace-agent.jar");
        File flight = new File(serverHome, "lib/monitor/flashlight-agent.jar");
        if (jvmConfigReader.isMonitoringEnabled()) {
            if (btrace.exists()) {
                optList.add("-javaagent:" + Utils.
                        quote(btrace.getAbsolutePath())
                        + "=unsafe=true,noServer=true"); // NOI18N
            } else if (flight.exists()) {
                optList.add("-javaagent:"
                        + Utils.quote(flight.getAbsolutePath()));
            }
        }
    }

    /**
     * Adds server variables from variables map into Java VM options
     * for server startup.
     * <p/>
     * @param javaOpts Java VM options {@link StringBuilder} instance.
     * @param varMap Server variables map.
     */
    private static void appendVarMap(
            StringBuilder javaOpts, Map<String, String> varMap) {
        for (Map.Entry<String, String> entry : varMap.entrySet()) {
            javaOpts.append(' ');
            JavaUtils.systemProperty(
                    javaOpts, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Starts local GF server.
     * <p/>
     * The own start is done by calling CommandStartDAS. This method prepares
     * command-line arguments that need to be provided for the command.
     * The parameters come from domain.xml and from parameter <code>args</code>
     * provided by caller.
     * <p/>
     * @param server GlassFish server entity.
     * @param args   Startup arguments provided by caller.
     * @param mode   Mode which we are starting GF in.
     * @return ResultProcess returned by CommandStartDAS to give caller
     *         opportunity to monitor the start process.
     * @throws GlassFishIdeException
     */
    public static ResultProcess startServer(GlassFishServer server,
            StartupArgs args, StartMode mode) throws GlassFishIdeException {
        final String METHOD = "startServer";
        // reading jvm config section from domain.xml
        JvmConfigReader jvmConfigReader = new JvmConfigReader(DAS_NAME);
        String domainAbsolutePath = server.getDomainsFolder() + File.separator
                + server.getDomainName();
        String domainXmlPath = domainAbsolutePath + File.separator + "config"
                + File.separator + "domain.xml";
        if (!TreeParser.readXml(new File(domainXmlPath), jvmConfigReader)) {
            // retry with platform default
            LOGGER.log(Level.INFO, "Retrying with {0} encoding", Charset.defaultCharset());
            jvmConfigReader = new JvmConfigReader(DAS_NAME);
            if (!TreeParser.readXml(new File(domainXmlPath), Charset.defaultCharset(), jvmConfigReader)) {
                throw new GlassFishIdeException(LOGGER.excMsg(
                        METHOD, "readXMLerror"), domainXmlPath);
            }
        }
        List<String> optList = jvmConfigReader.getOptList();
        Map<String, String> propMap = jvmConfigReader.getPropMap();
        addJavaAgent(server, jvmConfigReader);
        // try to find bootstraping jar - usually glassfish.jar
        File bootstrapJar = ServerUtils.getJarName(server.getServerHome(),
                ServerUtils.GFV3_JAR_MATCHER);
        if (bootstrapJar == null) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "noBootstrapJar"));
        }
        // compute classpath using properties from jvm-config element of
        // domain.xml
        String classPath = computeClassPath(propMap,
                new File(domainAbsolutePath), bootstrapJar);

        StringBuilder javaOpts = new StringBuilder(1024);
        StringBuilder glassfishArgs = new StringBuilder(256);
        // preparing variables to replace placeholders in options
        Map<String, String> varMap = varMap(server, args.getJavaHome());
        // Add debug parameters read from domain.xml.
        // It's important to add them before java options specified by user
        // in case user specified it by himslef.
        if (mode == StartMode.DEBUG) {
            String debugOpts = propMap.get("debug-options");
            String[] debugOptsSplited = debugOpts.split("\\s+(?=-)");
            optList.addAll(Arrays.asList(debugOptsSplited));
        }
        // add profile parameters
        if (mode == StartMode.PROFILE) {
        }
        // appending IDE specified options after the ones got from domain.xml
        // IDE specified are takind precedence this way
        if (args.getJavaArgs() != null) {
            optList.addAll(args.getJavaArgs());
        }
        appendOptions(javaOpts, optList, varMap);
        appendVarMap(javaOpts, varMap);
        if (args.getGlassfishArgs() != null) {
            appendGlassfishArgs(glassfishArgs, args.getGlassfishArgs());
        }
        // starting the server using command
        CommandStartDAS startCommand = new CommandStartDAS(args.getJavaHome(),
                classPath, javaOpts.toString(), glassfishArgs.toString(),
                domainAbsolutePath);
        Future<ResultProcess> future =
                ServerAdmin.<ResultProcess>exec(server, startCommand);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD, "failed"), e);
        }
    }

    /**
     * Convenient method to start glassfish in START mode.
     * <p/>
     * @param server GlassFish server entity.
     * @param args   Startup arguments provided by caller.
     * @return ResultProcess returned by CommandStartDAS to give caller
     *         opportunity to monitor the start process.
     * @throws GlassFishIdeException
     */
    public static ResultProcess startServer(GlassFishServer server,
            StartupArgs args) throws GlassFishIdeException {
        return startServer(server, args, StartMode.START);
    }

    /**
     * Computing class path for <code>-cp</code> option of java.
     * <p/>
     * @param propMap      Attributes of <code>jvm-config</code> element
     *                     of <code>domain.xml</code>.
     * @param domainDir    Relative paths will be added to this directory.
     * @param bootstrapJar Bootstrap jar will be also added to class path.
     * @return Class path for <code>-cp</code> option of java.
     */
    private static String computeClassPath(Map<String, String> propMap,
            File domainDir, File bootstrapJar) {
        final String METHOD = "computeClassPath";
        String result = null;
        List<File> prefixCP = Utils.classPathToFileList(
                propMap.get("classpath-prefix"), domainDir);
        List<File> suffixCP = Utils.classPathToFileList(
                propMap.get("classpath-suffix"), domainDir);
        boolean useEnvCP = "false".equals(
                propMap.get("env-classpath-ignored"));
        List<File> envCP = Utils.classPathToFileList(
                useEnvCP ? System.getenv("CLASSPATH") : null, domainDir);
        List<File> systemCP = Utils.classPathToFileList(
                propMap.get("system-classpath"), domainDir);

        if (prefixCP.size() > 0 || suffixCP.size() > 0 || envCP.size() > 0
                || systemCP.size() > 0) {
            List<File> mainCP = Utils.classPathToFileList(
                    bootstrapJar.getAbsolutePath(), null);

            if (mainCP.size() > 0) {
                List<File> completeCP = new ArrayList<>(32);
                completeCP.addAll(prefixCP);
                completeCP.addAll(mainCP);
                completeCP.addAll(systemCP);
                completeCP.addAll(envCP);
                completeCP.addAll(suffixCP);

                // Build classpath in proper order - prefix / main / system 
                // / environment / suffix
                // Note that completeCP should always have at least 2 elements
                // at this point (1 from mainCP and 1 from some other CP
                // modifier)
                StringBuilder classPath = new StringBuilder(1024);
                Iterator<File> iter = completeCP.iterator();
                classPath.append(Utils.quote(iter.next().getPath()));
                while (iter.hasNext()) {
                    classPath.append(File.pathSeparatorChar);
                    classPath.append(Utils.quote(iter.next().getPath()));
                }
                result = classPath.toString();
            } else {
                LOGGER.log(Level.WARNING, METHOD, "cpError");
            }
        }
        return result;
    }

    /**
     * Takes an list of java options and produces a valid string that can be put
     * on command line.
     * <p/>
     * There are two kinds of options that can be found in option list:
     * <code>key=value</code> and simple options not containing
     * <code>=</code>.
     * In the list there are both options from domain.xml and users options.
     * Thus some of them can be there more than once. For <code>key=value</code>
     * ones we can detect it and only the latest one in list will be appended to
     * command-line. For simple once maybe some duplicate detection will be
     * added in the future.
     * <p/>
     * @param argumentBuf Returned string.
     * @param optList     List of java options.
     * @param varMap      Map to be used for replacing place holders, Contains
     *                    <i>place holder</i> - <i>place holder</i> value pairs.
     */
    private static void appendOptions(StringBuilder argumentBuf,
            List<String> optList, Map<String, String> varMap) {
        final boolean isWindows = OsUtils.isWin();
        final String METHOD = "appendOptions"; // NOI18N
        // override the values that are found in the domain.xml file.
        // this is totally a copy/paste from StartTomcat...
        final Map<String,String> PROXY_PROPS = new HashMap<>();
        for(String s: new String[] {
            "http.proxyHost", // NOI18N
            "http.proxyPort", // NOI18N
            "http.nonProxyHosts", // NOI18N
            "https.proxyHost", // NOI18N
            "https.proxyPort", // NOI18N
        }) {
            PROXY_PROPS.put(JavaUtils.systemPropertyName(s), s);
            PROXY_PROPS.put("-D\"" + s, s); // NOI18N
            PROXY_PROPS.put("-D" + s, s); // NOI18N
        }

        // first process optList aquired from domain.xml 
        for (String opt : optList) {
            String name, value;
            // do placeholder substitution
            opt = Utils.doSub(opt.trim(), varMap);
            int splitIndex = opt.indexOf('=');
            // && !opt.startsWith("-agentpath:") is a temporary hack to
            // not touch already quoted -agentpath. Later we should handle it
            // in a better way.
            if (splitIndex != -1 && !opt.startsWith("-agentpath:")) { // NOI18N
                // key=value type of option
                name = opt.substring(0, splitIndex);
                value = Utils.quote(opt.substring(splitIndex + 1));
                LOGGER.log(Level.FINER, METHOD,
                        "jvmOptVal", new Object[] {name, value});

            } else {
                name = opt;
                value = null;
                LOGGER.log(Level.FINER, METHOD, "jvmOpt", name); // NOI18N
            }

            if(PROXY_PROPS.containsKey(name)) {
                String sysValue = System.getProperty(PROXY_PROPS.get(name));
                if (sysValue != null && sysValue.trim().length() > 0) {
                    if (isWindows && "http.nonProxyHosts".equals(PROXY_PROPS.get(name))) { // NOI18N
                        // enclose in double quotes to escape the pipes separating
                        // the hosts on windows
                        sysValue = "\"" + value + "\""; // NOI18N
                    }
                    name = JavaUtils.systemPropertyName(PROXY_PROPS.get(name));
                    value = sysValue;
                }
            }

            argumentBuf.append(' ');
            argumentBuf.append(name);
            if(value != null) {
                argumentBuf.append("="); // NOI18N
                argumentBuf.append(value);
            }
        }
    }

    /**
     * Append GlassFish startup arguments to given {@link StringBuilder}.
     * <p/>
     * @param glassfishArgs     Target {@link StringBuilder} to append arguments.
     * @param glassfishArgsList Arguments to be appended.
     */
    private static void appendGlassfishArgs(StringBuilder glassfishArgs,
            List<String> glassfishArgsList) {
        for (String arg : glassfishArgsList) {
            glassfishArgs.append(' ');
            glassfishArgs.append(arg);
        }
        // remove the first space
        if (glassfishArgs.length() > 0) {
            glassfishArgs.deleteCharAt(0);
        }
    }
}
