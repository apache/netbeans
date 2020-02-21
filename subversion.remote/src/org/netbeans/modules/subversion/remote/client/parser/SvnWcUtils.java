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
package org.netbeans.modules.subversion.remote.client.parser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 *  
 */
public class SvnWcUtils {

    static final String ENTRIES = "entries";      // NOI18N    

    private static final String PROPS = "props"; //NOI18N
    private static final String PROPS_BASE = "prop-base"; //NOI18N
           
    public static VCSFileProxy getSvnFile(VCSFileProxy file, String svnFileName) {
        VCSFileProxy svnFile = VCSFileProxy.createFileProxy(file, SvnUtils.SVN_ADMIN_DIR + "/" + svnFileName); //NOI18N
        if(VCSFileProxySupport.canRead(svnFile)) {
            return svnFile;
        }
        return null;                
    }
    
    public static VCSFileProxy getPropertiesFile(VCSFileProxy file, boolean base) {
        if(file.isFile()) {            
            if (base) {
                return getSvnFile(file.getParentFile(), PROPS_BASE + "/" + file.getName() + getPropFileNameSuffix(base)); //NOI18N
            } else {
                return getSvnFile(file.getParentFile(), PROPS + "/" + file.getName() + getPropFileNameSuffix(base)); //NOI18N
            }            
        } else {            
            return getSvnFile(file, base ? "dir-prop-base" : "dir-props"); //NOI18N
        }        
    }

    private static String getPropFileNameSuffix(boolean base) {
        if (base) {
            return ".svn-base"; //NOI18N
        } else {
            return ".svn-work"; //NOI18N
        }        
    }
    
    public static VCSFileProxy getTextBaseFile(VCSFileProxy file) throws IOException {
        return getSvnFile(file.getParentFile(), "text-base/" + file.getName() + ".svn-base"); //NOI18N
    }

    public static Date parseSvnDate(String inputValue) throws ParseException {
        Date returnValue = null;
        if (inputValue != null) {              
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //NOI18N
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); //NOI18N
            int idx = inputValue.lastIndexOf('.'); //NOI18N
            if(idx > 0) {
                idx = (idx + 4 > inputValue.length()) ? inputValue.length() : idx + 4; // parse as mili-, not microseconds
                inputValue = inputValue.substring(0, idx) + "Z"; //NOI18N
            }
            returnValue = dateFormat.parse(inputValue);
        }
        return returnValue;        
    }

    static VCSFileProxy getEntriesFile(VCSFileProxy file) throws IOException {
        return getSvnFile(!file.isDirectory() ? file.getParentFile() : file, ENTRIES);        
    }
    
}
