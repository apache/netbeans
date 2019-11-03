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
package org.netbeans.modules.payara.common.parser;

import org.netbeans.modules.payara.common.utils.Util;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.payara.spi.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reader for the jvm-config fields of domain.xml.  Uses simulated stream
 * reader based on SAX model since XPP is not universally available.
 *
 * @author Peter Williams
 */
public class JvmConfigReader extends TreeParser.NodeReader {

    private static final String SERVER_NAME = "server"; // NOI18N

    private final Map<String, String> argMap;
    private final Map<String, String> varMap;
    private final Map<String, String> propMap;
    private final List<String> optList;
    private String serverConfigName;
    private boolean readJvmConfig = false;

    public JvmConfigReader(List<String> optList, Map<String, String> argMap,
            Map<String, String> varMap, Map<String, String> propMap) {
        this.optList = optList;
        this.argMap = argMap;
        this.varMap = varMap;
        this.propMap = propMap;
    }

    public TreeParser.NodeReader getServerFinder() {
        return new TreeParser.NodeReader() {
            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                <server lb-weight="100" name="server" config-ref="server-config">
                if (serverConfigName == null || serverConfigName.length() == 0) {
                    if (SERVER_NAME.equals(attributes.getValue("name"))) {        // NOI18N
                        serverConfigName = attributes.getValue("config-ref");   // NOI18N
                        Logger.getLogger("payara").finer("DOMAIN.XML: Server profile defined by " + serverConfigName); // NOI18N
                    }
                }
            }
        };
    }

    public TreeParser.NodeReader getConfigFinder() {
        return new TreeParser.NodeReader() {
            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                <config name="server-config" dynamic-reconfiguration-enabled="true">
                if (serverConfigName != null && serverConfigName.equals(attributes.getValue("name"))) { // NOI18N
                    readJvmConfig = true;
                    Logger.getLogger("payara").finer("DOMAIN.XML: Reading JVM options from server profile " + serverConfigName); // NOI18N
                }
            }

            @Override
            public void endNode(String qname) throws SAXException {
                readJvmConfig = false;
            }
        };
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws SAXException {
//        <java-config
//            classpath-prefix="CP-PREFIX"
//            classpath-suffix="CP-SUFFIX"
//            debug-enabled="false"
//            debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009"
//            env-classpath-ignored="false"
//            java-home="${com.sun.aas.javaRoot}"
//            javac-options="-g"
//            native-library-path-prefix="NATIVE-LIB-PREFIX"
//            native-library-path-suffix="NATIVE-LIB-SUFFIX"
//            rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g"
//            server-classpath="SERVER-CLASSPATH"
//            system-classpath="SYSTEM-CLASSPATH">
        if (readJvmConfig) {
            int attrLen = attributes.getLength();
            for (int i = 0; i < attrLen; i++) {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);
                if (name != null && name.length() > 0 && value != null && value.length() > 0) {
                    propMap.put(name, value);
                }
            }
        }
    }

    @Override
    public void readCData(String qname, char[] ch, int start, int length) throws SAXException {
//        <jvm-options>-client</jvm-options>
//        <jvm-options>-Djava.endorsed.dirs=${com.sun.aas.installRoot}/lib/endorsed</jvm-options>
        if (readJvmConfig) {
            String option = new String(ch, start, length);
            if (option.startsWith("-D")) { // NOI18N
                int splitIndex = option.indexOf('=');
                if (splitIndex != -1) {
                    String name = option.substring(2, splitIndex);
                    String value = doSub(option.substring(splitIndex + 1));
                    if (name.length() > 0) {
                        Logger.getLogger("payara").finer("DOMAIN.XML: argument name = " + name + ", value = " + value); // NOI18N
                        argMap.put(name, value);
                    }
                }
            } else if (option.startsWith("-X")) { // NOI18N
                option = doSub(option);
                int splitIndex = option.indexOf('=');
                if (splitIndex != -1) {
                    String name = option.substring(0, splitIndex);
                    String value = option.substring(splitIndex + 1);
                    Logger.getLogger("payara").finer("DOMAIN.XML: jvm option: " + name + " = " + value); // NOI18N
                    optList.add(name + '=' + Util.quote(value));
                } else {
                    Logger.getLogger("payara").finer("DOMAIN.XML: jvm option: " + option); // NOI18N
                    optList.add(option);
                }
            } else if (option.startsWith("-")) {
                Logger.getLogger("payara").finer("DOMAIN.XML: jvm option: " + option); // NOI18N
                optList.add(option);
            }
        }
    }
    private Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}"); // NOI18N

    private String doSub(String value) {
        try {
            Matcher matcher = pattern.matcher(value);
            boolean result = matcher.find();
            if (result) {
                StringBuffer sb = new StringBuffer(value.length() * 2);
                do {
                    String key = matcher.group(1);
                    String replacement = varMap.get(key);
                    if (replacement == null) {
                        replacement = System.getProperty(key);
                        if (replacement != null) {
                            replacement = Utils.escapePath(replacement);
                        } else {
                            replacement = "\\$\\{" + key + "\\}"; // NOI18N
                        }
                    }
                    matcher.appendReplacement(sb, replacement);
                    result = matcher.find();
                } while (result);
                matcher.appendTail(sb);
                value = sb.toString();
            }
        } catch (Exception ex) {
            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
        }
        return value;
    }

    public TreeParser.NodeReader getMonitoringFinder(final File btraceJar) {
        return new TreeParser.NodeReader() {
            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                <monitoring-service [monitoring-enabled="false"] 
                if (readJvmConfig) {
                    if (!"false".equals(attributes.getValue("monitoring-enabled"))) {  // NOI18N
                        optList.add("-javaagent:"+Util.quote(btraceJar.getAbsolutePath())+"=unsafe=true,noServer=true"); // NOI18N
                    }
                }
            }
        };
    }
}
