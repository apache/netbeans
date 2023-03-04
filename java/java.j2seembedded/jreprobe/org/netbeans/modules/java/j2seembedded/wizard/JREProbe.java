/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.j2seembedded.wizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Stores the JRE system properties into given properties file.
 * @author Tomas Zezula
 */
public class JREProbe {
    
    private static final String NB_PROP_PROFILE = "netbeans.java.profile";  //NOI18N
    private static final String NB_PROP_EXTENSIONS = "netbeans.java.extensions";       //NOI18N
    private static final String NB_PROP_VM = "netbeans.jvm.type";       //NOI18N
    private static final String NB_PROP_DEBUG = "netbeans.jvm.debug";       //NOI18N
    private static final String NB_PROP_TARGET = "netbeans.jvm.target";       //NOI18N
    private static final String JAVA_HOME = "java.home";    //NOI18N
    private static final String COMPACT_1 = "compact1";    //NOI18N
    private static final String COMPACT_2 = "compact2";    //NOI18N
    private static final String COMPACT_3 = "compact3";    //NOI18N
    private static final String COMPACT_2_CLASS = "java.rmi.Remote";    //NOI18N
    private static final String COMPACT_3_CLASS = "java.lang.instrument.Instrumentation";    //NOI18N
    private static final String DEFAULT_CLASS = "java.awt.Toolkit";    //NOI18N
    private static final String BOM_KEY_TARGET = "target";  //NOI18N
    private static final String BOM_KEY_VM = "vm";  //NOI18N
    private static final String BOM_KEY_EXTENSION = "extension";    //NOI18N
    private static final String BOM_KEY_DEBUG = "debug";   //NOI18N

    public static void main(String[] args) {
        final Properties p = new Properties();
        p.putAll(System.getProperties());
        final String profile = getProfile();
        if (profile != null) {
            p.setProperty(NB_PROP_PROFILE, profile);
        }
        String installDir = p.getProperty(JAVA_HOME);
        if (installDir != null) {
            p.putAll(getBOMData(new File(installDir)));
        }
        File f = new File(args[0]);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            p.store(fos, null);
            fos.close();
        } catch (Exception exc) {
            //PENDING
            exc.printStackTrace();
        }
    }

    private static String getProfile() {
        String profile = COMPACT_1;
        try {
            Class.forName(COMPACT_2_CLASS);
        } catch (ClassNotFoundException e) {
            return profile;
        }
        profile = COMPACT_2;
        try {
            Class.forName(COMPACT_3_CLASS);
        } catch (ClassNotFoundException e) {
            return profile;
        }
        profile = COMPACT_3;
        try {
            Class.forName(DEFAULT_CLASS);
        } catch (ClassNotFoundException e) {
            return profile;
        }        
        return null;
    }

    private static Map getBOMData(final File installDir) {
        final Map props = new HashMap();
        final File bomFile = new File (installDir, "bom");  //NOI18N
        if (bomFile.canRead()) {
            try {
                final BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(bomFile), StandardCharsets.UTF_8));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("#")) { //NOI18N
                            continue;
                        }
                        final StringTokenizer tk = new StringTokenizer(line,"="); //NOI18N
                        if (tk.countTokens() != 2) {
                            continue;
                        }
                        final String key = tk.nextToken().trim();
                        final String value = tk.nextToken().trim();
                        if (BOM_KEY_TARGET.equals(key)) {
                            props.put(NB_PROP_TARGET, value);
                        } else if (BOM_KEY_VM.equals(key)) {
                            props.put(NB_PROP_VM, value);
                        } else if (BOM_KEY_EXTENSION.equals(key)) {
                            props.put(NB_PROP_EXTENSIONS, parseExtensions(value));
                        } else if (BOM_KEY_DEBUG.equals(key)) {
                            props.put(NB_PROP_DEBUG, value);
                        }
                    }
                } finally {
                    in.close();
                }
            } catch (IOException ioe) {
                //pass - don't care returns {}
            }
        }
        return props;
    }

    private static String parseExtensions(String value) {
        int start = 0;
        int end = value.length();
        if (value.charAt(start) == '[') {   //NOI18N
            start++;
        }
        if (value.charAt(end-1) == ']') {   //NOI18N
            end--;
        }
        return value.substring(start, end);
    }
}
