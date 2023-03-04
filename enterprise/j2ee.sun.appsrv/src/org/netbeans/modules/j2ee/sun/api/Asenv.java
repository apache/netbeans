// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

package org.netbeans.modules.j2ee.sun.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities;

/**
 * Parser for asenv.conf and asenv.bat
 */
public class Asenv {
    
    private final transient java.util.Properties props = new java.util.Properties();
    
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_JAVA = "AS_JAVA";
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_NS_BIN = "AS_NSS_BIN";
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_HADB = "AS_HADB";
    /**
     * key to path of default domains in asenv file
     */
    public static final String AS_DEF_DOMAINS_PATH = "AS_DEF_DOMAINS_PATH";
    
    /**
     * Creates a new instance of Asenv
     * @param platformRoot root of the platform
     */
    public Asenv(File platformRoot) {
        String ext = (Utilities.isWindows() ? "bat" : "conf");          // NOI18N
        File asenv = new File(platformRoot,"config/asenv."+ext);        // NOI18N
        if (!asenv.canRead()) return;
        FileReader fReader = null;
        BufferedReader bReader = null;
        try {
            fReader = new FileReader(asenv);
            bReader = new BufferedReader(fReader);
            
            String line = bReader.readLine();
            while (line != null) {
                StringTokenizer strtok = new StringTokenizer(line,"=");
                if (strtok.countTokens() == 2) {
                    String key = strtok.nextToken();
                    String val = strtok.nextToken();
                    if (key.startsWith("set ")) {
                        key = key.substring(3).trim();
                    }
                    if (val.startsWith("\"")) {
                        val = val.substring(1,val.length()-1);
                    }
                    props.put(key,val);
                }
                line = bReader.readLine(); 
            } 
        } catch (FileNotFoundException ex) {
            // the file disappeared?
            Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ex);
        } catch (IOException ex) {
            Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ex);
        } finally {
            if (null != bReader) {
                try {
                    bReader.close();
                } catch (IOException ioe) {
                    Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ioe);
                }
            }
            if (null != fReader) {
                try {
                    fReader.close();
                } catch (IOException ioe) {
                    Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ioe);
                }
            }
        }
    }
    
    /**
     * Get values from asenv file
     * @param key variable defined in asenv
     * @return associated value    
     */
    public String get(final String key) {
        return (String) props.getProperty(key);
    }
    
}

