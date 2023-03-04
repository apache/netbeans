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

package org.netbeans.modules.uihandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
final class BuildInfo {
    protected static final String BUILD_INFO_FILE = "build_info"; //NOI18N
    
    private static final String[] order = {"Number", "Date", "Branding", "Branch", "Tag", "Hg ID"}; //NOI18N
    
    private static final Pattern linePattern = Pattern.compile("(.+):\\s+((.+)\\z)"); //NOI18N

    static LogRecord logBuildInfoRec(){
        LogRecord rec = new LogRecord(Level.CONFIG, BUILD_INFO_FILE);
        List<String> buildInfo = logBuildInfo();
        if (buildInfo != null){
            rec.setParameters(buildInfo.toArray());
        }
        return rec;
    }
    /** Gets build informations
     * @return list build informations in this order: number, date, branding, branch, tag, hg id, or null if the info is not available 
     */
    static List<String> logBuildInfo() {
        List<String> lr = null;
        File f = InstalledFileLocator.getDefault().locate(BUILD_INFO_FILE, null, false);
        if (f != null) {
            lr = logBuildInfo(f);
        }
        return lr;
    }

    private static List<String> logBuildInfo(File f) {
        ArrayList<String> params = null;
        FileReader fr = null;
        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            Map<String, String> map = new Hashtable<String, String> ();
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = linePattern.matcher(line);
                if ((m.matches()) && (m.groupCount() > 2)) {
                    map.put(m.group(1), m.group(2));
                }
            }
            params = new ArrayList<String>();
            for (int i = 0; i < order.length; i++) {
                String param = map.get(order[i]);
                if (param != null) {
                    params.add(param);
                } else {
                    params.add("");
                }
                
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return params;
    }

}
