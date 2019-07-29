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

import org.openide.util.Parameters;

public class FindJSPServletHelper {

    /** Creates a new instance of FindJSPServletImpl */
    private FindJSPServletHelper() {
    }
        
    static public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        Parameters.notWhitespace("jspResourcePath", jspResourcePath);
        String s = getServletPackageName(jspResourcePath) + '/' +
            getServletClassName(jspResourcePath) + ".java";// NOI18N
        return s;
    }

    // After Apace donation, should use org.apache.jasper utilities in 
    // JspUtil and JspCompilationContext
    static private String getServletPackageName(String jspUri) {
        String jspBasePackageName = "org/apache/jsp";//NOI18N
        int iSep = jspUri.lastIndexOf('/');
        String packageName = (iSep > 0) ? jspUri.substring(0, iSep) : "";//NOI18N
        if (packageName.length() == 0) {
            return jspBasePackageName;
        }
        return jspBasePackageName + "/" + packageName.substring(1);//NOI18N

    }
    
    // After Apace donation, should use org.apache.jasper utilities in 
    // JspUtil and JspCompilationContext
    static private String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        String className = jspUri.substring(iSep);
        StringBuilder modClassName = new StringBuilder("");//NOI18N
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (c == '.') {
                modClassName.append('_');
            } else {
                modClassName.append(c);
            }
        }
        return modClassName.toString();
    }
    
    public static String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }
        
}
