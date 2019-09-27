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
package org.netbeans.modules.payara.tooling.server.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.payara.tooling.data.JDKVersion;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser.NodeListener;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class JvmConfigReader extends NodeListener implements XMLReader {

    private static String JVM_OPTIONS_TAG = "jvm-options";

    private String serverName;
    /**
     * Holds all values found in <jvm-options> tags
     */
    private List<JvmOption> jvmOptions = new ArrayList<>();
    /**
     * Holds all key-value pairs representing attributes of jvm-config tag.
     * These are used for computing the classpath.
     */
    private HashMap<String, String> propMap = new HashMap<>();
    private boolean isMonitoringEnabled = false;
    private String serverConfigName;
    private boolean readConfig = false;
    private StringBuilder b = new StringBuilder();

    public JvmConfigReader(String serverName) {
        this.serverName = serverName;
    }

    public TreeParser.NodeListener getServerFinder() {
        return new TreeParser.NodeListener() {

            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                <server lb-weight="100" name="server" config-ref="server-config">
                if (serverConfigName == null || serverConfigName.length() == 0) {
                    if (serverName.equals(attributes.getValue("name"))) {        // NOI18N
                        serverConfigName = attributes.getValue("config-ref");   // NOI18N
                        //Logger.getLogger("payara").finer("DOMAIN.XML: Server profile defined by " + serverConfigName); // NOI18N
                    }
                }
            }
        };
    }

    public TreeParser.NodeListener getConfigFinder() {
        return new TreeParser.NodeListener() {

            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                <config name="server-config" dynamic-reconfiguration-enabled="true">
                if (serverConfigName != null && serverConfigName.equals(attributes.getValue("name"))) { // NOI18N
                    readConfig = true;
                    //Logger.getLogger("payara").finer("DOMAIN.XML: Reading JVM options from server profile " + serverConfigName); // NOI18N
                }
            }

            @Override
            public void endNode(String qname) throws SAXException {
                if ("config".equals(qname)) {
                    readConfig = false;
                }
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
        if (readConfig) {
            int attrLen = attributes.getLength();
            for (int i = 0; i < attrLen; i++) {
                String name = attributes.getLocalName(i);
                // seems that sometimes from uknown reasons
                // getLocalName returns empty string...
                if ((name == null) || name.isEmpty()) {
                    name = attributes.getQName(i);
                }
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
        if (readConfig && JVM_OPTIONS_TAG.equals(qname)) {
            b.append(ch, start, length);
        }
    }

    @Override
    public void endNode(String qname) throws SAXException {
        if (readConfig && JVM_OPTIONS_TAG.equals(qname)) {
            jvmOptions.add(new JvmOption(b.toString()));
            b.delete(0, b.length());
        }
    }

    public TreeParser.NodeListener getMonitoringFinder() {
        return new TreeParser.NodeListener() {

            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
                //                <monitoring-service [monitoring-enabled="false"] 
                if (readConfig) {
                    isMonitoringEnabled = !"false".equals(attributes.getValue("monitoring-enabled"));
//    				if (monitoringAgent.exists()) {
//    					if (!"false".equals(attributes.getValue("monitoring-enabled"))) {  // NOI18N
//    						//optList.add("-javaagent:"+Utils.quote(monitoringAgent.getAbsolutePath())+"=unsafe=true,noServer=true"); // NOI18N
//    						isMonitoringEnabled = true;
//    					}
//    				}
                }
            }
        };
    }

//    private NodeListener getOptionsReader() {
//        return new NodeListener() {
//
//            
//            @Override
//            public void endNode(String qname) throws SAXException {
//                if (readJvmConfig) {
//                    optList.add(b.toString());
//                    b.delete(0, b.length());
//                }
//            }
//
//        };
//    }
//    
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new Path("/domain/servers/server", getServerFinder()));
        paths.add(new Path("/domain/configs/config", getConfigFinder()));
        paths.add(new Path("/domain/configs/config/java-config", this));
        paths.add(new Path("/domain/configs/config/monitoring-service", getMonitoringFinder()));
        return paths;
    }

    public List<JvmOption> getJvmOptions() {
        return jvmOptions;
    }

    public Map<String, String> getPropMap() {
        return propMap;
    }

    public boolean isMonitoringEnabled() {
        return isMonitoringEnabled;
    }

    public static class JvmOption {

        public final String option;
        public final Optional<JDKVersion> minVersion;
        public final Optional<JDKVersion> maxVersion;

        // splits the versioned JVM option pattern into three groups:
        //     Gr1  Gr2 Gr3
        //      <>  <>  <------------>
        // Ex: [1.7|1.8]-XX:MyJvmOption (both min and max version present)
        // Below examples have missing verisions, with is also OK
        // Ex: [|1.8]-XX:MyJvmOption (only max version present)
        // Ex: [1.7|]-XX:MyJvmOption (only min version present)
        // Gr1 or Gr2 can be null (optional)
        private static final Pattern PATTERN = Pattern.compile("^\\[(.*)\\|(.*)\\](.*)");

        public JvmOption(String option) {
            Matcher matcher = PATTERN.matcher(option);
            if (matcher.matches()) {
                this.minVersion = Optional.ofNullable(JDKVersion.toValue(matcher.group(1)));
                this.maxVersion = Optional.ofNullable(JDKVersion.toValue(matcher.group(2)));
                this.option = matcher.group(3);
            } else {
                this.option = option;
                this.minVersion = Optional.empty();
                this.maxVersion = Optional.empty();
            }
        }

        public JvmOption(String option, String minVersion, String maxVersion) {
            this.option = option;
            this.minVersion = Optional.ofNullable(JDKVersion.toValue(minVersion));
            this.maxVersion = Optional.ofNullable(JDKVersion.toValue(maxVersion));
        }

        public static boolean hasVersionPattern(String option) {
            return PATTERN.matcher(option).matches();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.option);
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
            final JvmOption other = (JvmOption) obj;
            if (!Objects.equals(this.option, other.option)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            if (!minVersion.isPresent() && !maxVersion.isPresent()) {
                return option;
            }
            return String.format("[%s|%s]%s", minVersion.isPresent() ? minVersion.get() : "", maxVersion.isPresent() ? maxVersion.get() : "", option);
        }
    }

}
