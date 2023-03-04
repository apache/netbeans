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

package org.netbeans.modules.websvc.wsstack;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;

/**
 *
 * @author mkuchtiak
 */
public  class VersionSupport {
    
    public static WSStackVersion parseVersion(String version) {
        int major = 0;
        int minor = 0;
        int micro = 0;
        int update = 0;
        
        int tokenPosition = 0;
        StringTokenizer versionTokens = new StringTokenizer(version,"."); //NOI18N
        List<Integer> versionNumbers = new ArrayList<Integer>();
        while (versionTokens.hasMoreTokens()) {
            versionNumbers.add(valueOf(versionTokens.nextToken().trim()));
        }
        switch (versionNumbers.size()) {
            case 0 : {
                break;
            }
            case 1 : {
                major = versionNumbers.get(0);
                break;
            }
            case 2 : {
                major = versionNumbers.get(0);
                minor = versionNumbers.get(1);
                break;
            } 
            case 3 : {
                major = versionNumbers.get(0);
                minor = versionNumbers.get(1);
                micro = versionNumbers.get(2);
                break;
            } 
            default: {
                major = versionNumbers.get(0);
                minor = versionNumbers.get(1);
                micro = versionNumbers.get(2);
                update = versionNumbers.get(3);
            }
        }
        return WSStackVersion.valueOf(major, minor, micro, update);
    }

    private static Integer valueOf(String versionToken) {
        int i = 0;
        StringBuffer buf = new StringBuffer();
        while (i<versionToken.length()) {
            char ch = versionToken.charAt(i);
            if (Character.isDigit(ch)) {
                buf.append(ch);
            } else {
                break;
            }
            ++i;
        }
        return buf.length() > 0 ? Integer.valueOf(buf.toString()) : 0;
    }
}
