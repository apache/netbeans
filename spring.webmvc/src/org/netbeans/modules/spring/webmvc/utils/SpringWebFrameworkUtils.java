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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
