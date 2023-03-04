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

package org.netbeans.modules.web.debug.util;

import java.util.Vector;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;

public class JspNameUtil {

    public static String getServletResourcePath(JSPServletFinder finder, String moduleContextPath, String jspResourcePath) {
        return getServletPackageName(finder, jspResourcePath).replace('.', '/') + '/' +
            getServletClassName(jspResourcePath) + ".java";
    }

    // After Apache code donation, reuse org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    private static String getServletPackageName(JSPServletFinder finder, String jspUri) {
        String basePackage = finder.getServletBasePackageName();
        if (basePackage == null) {
            basePackage = "org/apache/jsp";//NOI18N
        }
        int iSep = jspUri.lastIndexOf('/');
        String packageName = (iSep > 0) ? jspUri.substring(0, iSep) : "";//NOI18N
        if (packageName.length() == 0) {
            return basePackage;
        }
        return basePackage + "/" + packageName.substring(1);//NOI18N
    }

    // After Apache code donation, reuse org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    private static String getServletClassName(String jspUri) {
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
}
