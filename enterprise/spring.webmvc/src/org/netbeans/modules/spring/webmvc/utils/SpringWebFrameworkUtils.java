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

package org.netbeans.modules.spring.webmvc.utils;

import java.util.regex.Pattern;

/**
 *
 * @author John Baker
 */
public class SpringWebFrameworkUtils {

    private static final char[] INVALID_CHARS = {'<', '>', '*', '\\',  ':', '\"',  '/', '%', '|', '?'}; // NOI18N
    
    public static boolean isDispatcherServletConfigFilenameValid(String name) {
        boolean isNameValid = true;
        for (char c : INVALID_CHARS) {
            if (name.indexOf(c) != -1) {
                isNameValid = false;
                break;
            }
        }
        return isNameValid;
    }
    
    public static boolean isDispatcherMappingPatternValid(String pattern){
        // mapping validation based on the Servlet 2.4 specification,section SRV.11.2
        if (pattern.startsWith("*.")){ // NOI18N
            String p = pattern.substring(2);
            return Pattern.matches("\\w+",p); // NOI18N
        }
        
        if ((pattern.length() > 3) && pattern.endsWith("/*") && pattern.startsWith("/") && !pattern.contains(" ")) // NOI18N
            return true;
        
        if (pattern.matches("/")){ // NOI18N
            return true;
        }
               
        return false;
    }

    /**
     * Instantiates a servlet mapping pattern into a concrete URL. If the mapping
     * contains a wildcard, it will be replaced by the page name. Otherwise, if
     * the mapping is <code>/</code>, the page name will be returned.
     *
     * @param  dispatcherMapping the mapping pattern.
     * @param  page the page name to instantiate the pattern for.
     * @return the concrete URL based on the pattern.
     */
    public static String instantiateDispatcherMapping(String dispatcherMapping, String page) {
        String result;
        if (dispatcherMapping.equals("/")) { // NOI18N
            result = page;
        } else {
            result = dispatcherMapping.replace("*", page); // NOI18N
        }
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    /**
     * Returns the last part of a servlet URL. For example, if the URL
     * is of the form <code>"/app/index"</code>, this method will return <code>"index"</code>.
     *
     * @param fullDispatcherURL a full servlet URL.
     * @return the last part of the URL.
     */
    public static String getSimpleDispatcherURL(String fullDispatcherURL) {
        int lastSlash = fullDispatcherURL.lastIndexOf('/');
        if (lastSlash >= 0) {
            return fullDispatcherURL.substring(lastSlash + 1);
        }
        return fullDispatcherURL;
    }
}
