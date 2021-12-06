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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.client.status;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.versionvault.Clearcase;

/**
 * Represents a version selector for a file
 * e.g /main/CHECKEDOUT
 *     /main/1  
 * 
 * @author Tomas Stupka
 */
public class FileVersionSelector {   
    
    public static long CHECKEDOUT_VERSION = -2;
    public static long INVALID_VERSION = -1;
    
    private static String CHECKEDOUT = "CHECKEDOUT";                   

    private String path;
    private long versionNumber;
    private String versionSelector;    

    private FileVersionSelector(String path, long versionNumber, String versionSelector) {
        this.path = path;
        this.versionNumber = versionNumber;
        this.versionSelector = versionSelector;
    }   
    
    private FileVersionSelector(String versionSelector) {
        this.path = null;
        this.versionNumber = INVALID_VERSION;
        this.versionSelector = versionSelector;
    }   
    
    static FileVersionSelector fromString(String versionSelector) {
        if(versionSelector == null) {
            return null;
        }
        versionSelector = versionSelector.trim();
        if(versionSelector.equals("")) {
            return null;
        }
        // rip of the version        
        int idxVer = versionSelector.lastIndexOf(File.separator); 
        if(idxVer < 0) {            
            return new FileVersionSelector(versionSelector);
        }
        long versionNumber = -1;
        String path = null;
        try {
            String versionString = versionSelector.substring(idxVer + 1);
            path = versionSelector.substring(0, idxVer);
            
            if(versionString.equals(CHECKEDOUT)) {
                versionNumber = CHECKEDOUT_VERSION;
            } else {
                versionNumber = Long.parseLong(versionString);   
            }        
            return new FileVersionSelector(path, versionNumber, versionSelector);
        } catch (Exception e) {
            Clearcase.LOG.log(Level.WARNING, "Problem parsing version from [" + versionSelector + "]", e);
            // hm, lets say the last segment in versionSelector was the LABEL
            return new FileVersionSelector(path, INVALID_VERSION, versionSelector); 
        }                        
    }
    
    public String getPath() {
        return path;
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public String getVersionSelector() {
        return versionSelector;
    }
    
    public boolean isCheckedout() {
        return versionNumber == CHECKEDOUT_VERSION;
    }
}
