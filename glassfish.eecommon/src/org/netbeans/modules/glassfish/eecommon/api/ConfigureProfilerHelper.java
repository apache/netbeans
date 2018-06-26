// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
// </editor-fold>

package org.netbeans.modules.glassfish.eecommon.api;

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
