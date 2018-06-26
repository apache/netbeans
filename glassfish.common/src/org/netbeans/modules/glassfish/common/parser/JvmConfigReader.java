/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.glassfish.common.parser;

import org.netbeans.modules.glassfish.common.utils.Util;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.glassfish.spi.Utils;
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
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: Server profile defined by " + serverConfigName); // NOI18N
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
                    Logger.getLogger("glassfish").finer("DOMAIN.XML: Reading JVM options from server profile " + serverConfigName); // NOI18N
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
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: argument name = " + name + ", value = " + value); // NOI18N
                        argMap.put(name, value);
                    }
                }
            } else if (option.startsWith("-X")) { // NOI18N
                option = doSub(option);
                int splitIndex = option.indexOf('=');
                if (splitIndex != -1) {
                    String name = option.substring(0, splitIndex);
                    String value = option.substring(splitIndex + 1);
                    Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + name + " = " + value); // NOI18N
                    optList.add(name + '=' + Util.quote(value));
                } else {
                    Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + option); // NOI18N
                    optList.add(option);
                }
            } else if (option.startsWith("-")) {
                Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + option); // NOI18N
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
            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
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
