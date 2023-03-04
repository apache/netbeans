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
package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.util.SvnUtils;

/**
 *
 * @author Ed Hillmann 
 */
public class SvnWcUtils {

    static final String ENTRIES = "entries";      // NOI18N    

    private static final String PROPS = "props";
    private static final String PROPS_BASE = "prop-base";
           
    public static File getSvnFile(File file, String svnFileName) {
        File svnFile = new File(file, SvnUtils.SVN_ADMIN_DIR + "/" + svnFileName);
        if(svnFile.canRead()) {
            return svnFile;
        }
        return null;                
    }
    
    public static File getPropertiesFile(File file, boolean base) {
        if(file.isFile()) {            
            if (base) {
                return getSvnFile(file.getParentFile(), PROPS_BASE + "/" + file.getName() + getPropFileNameSuffix(base));
            } else {
                return getSvnFile(file.getParentFile(), PROPS + "/" + file.getName() + getPropFileNameSuffix(base));
            }            
        } else {            
            return getSvnFile(file, base ? "/dir-prop-base" : "/dir-props");
        }        
    }

    private static String getPropFileNameSuffix(boolean base) {
        if (base) {
            return ".svn-base";
        } else {
            return ".svn-work";
        }        
    }
    
    public static File getTextBaseFile(File file) throws IOException {
        return getSvnFile(file.getParentFile(), "text-base/" + file.getName() + ".svn-base");
    }

    public static Date parseSvnDate(String inputValue) throws ParseException {
        Date returnValue = null;
        if (inputValue != null) {              
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            int idx = inputValue.lastIndexOf(".");            
            if(idx > 0) {
                idx = (idx + 4 > inputValue.length()) ? inputValue.length() : idx + 4; // parse as mili-, not microseconds
                inputValue = inputValue.substring(0, idx) + "Z";            
            }
            returnValue = dateFormat.parse(inputValue);
        }
        return returnValue;        
    }

    static File getEntriesFile(File file) throws IOException {
        return getSvnFile(!file.isDirectory() ? file.getParentFile() : file, ENTRIES);        
    }
    
}
