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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
// </editor-fold>

package org.netbeans.modules.payara.eecommon.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigureProfilerHelper {
    
    
    private static final String ASENV_INSERTION_POINT_WIN_STRING    = "set AS_JAVA";
    private static final String ASENV_INSERTION_POINT_NOWIN_STRING  = "AS_JAVA";
    
    
    // replaces the AS_JAVA item in asenv.bat/conf
    static public boolean modifyAsEnvScriptFile( File irf, String targetJavaHomePath) {
        
        String ext = (isUnix() ? "conf" : "bat");
        //File irf = ((SunDeploymentManagerInterface)dm).getPlatformRoot();
        if (null == irf || !irf.exists()) {
            Logger.getLogger(ConfigureProfilerHelper.class.getName()).log(Level.FINER,"installRoot issue");
            return false;
        }
        String installRoot = irf.getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        String asEnvScriptFilePath  = installRoot+"/config/asenv." + ext;
        File asEnvScriptFile = new File(asEnvScriptFilePath);
        if (!asEnvScriptFile.canWrite()) {
            Logger.getLogger(ConfigureProfilerHelper.class.getName()).log(Level.FINER,"asenv issue");
            return false;
        }
        String lineBreak = System.getProperty("line.separator");
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            
            String line;
            FileReader fr = new FileReader(asEnvScriptFile);
            br = new BufferedReader(fr);
            StringBuilder buffer = new StringBuilder(Math.min(asEnvScriptFilePath.length(), 60000));
            
            String asJavaString = (isUnix() ? ASENV_INSERTION_POINT_NOWIN_STRING : ASENV_INSERTION_POINT_WIN_STRING);
            
            // copy config file from disk into memory buffer and modify line containing AS_JAVA definition
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(asJavaString)) {
                    buffer.append(asJavaString);
                    buffer.append('=');
                    buffer.append(targetJavaHomePath);
                } else {
                    buffer.append(line);
                }
                buffer.append(lineBreak);
            }
            //br.close();
            
            // flush modified config file from memory buffer back to disk
            fw = new FileWriter(asEnvScriptFile);
            fw.write(buffer.toString());
            fw.flush();
            //fw.close();
            
            if (isUnix()) {
                Runtime.getRuntime().exec("chmod a+r " + asEnvScriptFile.getAbsolutePath()); //NOI18N
            }
            
            return true;
            
        } catch (RuntimeException re) {
            Logger.getLogger(ConfigureProfilerHelper.class.getName()).log(Level.FINER,"",re);
            return false;
        } catch (Exception ex) {
            Logger.getLogger(ConfigureProfilerHelper.class.getName()).log(Level.FINER,"",ex);
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    Logger.getLogger(ConfigureProfilerHelper.class.getName()).log(Level.FINEST,"",ioe);
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ioe) {
                    Logger.getLogger(ConfigureProfilerHelper.class.getName()).log(Level.FINEST,"",ioe);
                }
            }
        }
        
    }
    
    static boolean isUnix() {
        return File.separatorChar == '/';
    }    
}
