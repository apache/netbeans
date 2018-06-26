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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.awt.event.ItemEvent;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import javax.swing.SwingUtilities;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;


/**
 *
 * @author vkraemer
 * @auther Peter Williams
 */
public class Utils implements org.netbeans.modules.j2ee.sun.share.Constants {

    private Utils() {
    }

    public static boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }

    public static boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }

    public static boolean strEquals(String one, String two) {
        boolean result = false;

        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }

    public static boolean strEquivalent(String one, String two) {
        boolean result = false;

        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }

        return result;
    }

    public static int strCompareTo(String one, String two) {
        int result;

        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }

        return result;
    }


    public static boolean hasTrailingSlash(String path) {
        return (path.charAt(path.length()-1) == '/');
    }

    public static boolean containsWhitespace(String data) {
        boolean result = false;

        if(notEmpty(data)) {
            for(int i = 0, datalength = data.length(); i < datalength; i++) {
                if(Character.isSpaceChar(data.charAt(i))) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isJavaIdentifier(final String id) {
        boolean result = true;

        if(!notEmpty(id) || !Character.isJavaIdentifierStart(id.charAt(0))) {
            result = false;
        } else {
            for(int i = 1, idlength = id.length(); i < idlength; i++) {
                if(!Character.isJavaIdentifierPart(id.charAt(i))) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isJavaPackage(final String pkg) {
        boolean result = false;

        if(notEmpty(pkg)) {
            int state = 0;
            for(int i = 0, pkglength = pkg.length(); i < pkglength && state < 2; i++) {
                switch(state) {
                case 0:
                    if(Character.isJavaIdentifierStart(pkg.charAt(i))) {
                        state = 1;
                    } else {
                        state = 2;
                    }
                    break;
                case 1:
                    if(pkg.charAt(i) == '.') {
                        state = 0;
                    } else if(!Character.isJavaIdentifierPart(pkg.charAt(i))) {
                        state = 2;
                    }
                    break;
                }
            }

            if(state == 1) {
                result = true;
            }
        }

        return result;
    }

    public static boolean isJavaClass(final String cls) {
        return isJavaPackage(cls);
    }

//    public static CommonDDBean [] listToArray(List list, Class targetClass) {
//        CommonDDBean [] result = null;
//        if(list != null) {
//            int size = list.size();
//            if(size != 0) {
//                result = (CommonDDBean []) Array.newInstance(targetClass, size);
//                for(int i = 0; i < size; i++) {
//                    CommonDDBean property = (CommonDDBean) list.get(i);
//                    result[i] = (CommonDDBean) property.clone();
//                }
//            }
//        }
//        return result;
//    }
//
//    public static CommonDDBean [] listToArray(List list, Class targetClass, String newVersion) {
//        CommonDDBean [] result = null;
//        if(list != null) {
//            int size = list.size();
//            if(size != 0) {
//                result = (CommonDDBean []) Array.newInstance(targetClass, size);
//                for(int i = 0; i < size; i++) {
//                    CommonDDBean property = (CommonDDBean) list.get(i);
//                    result[i] = (CommonDDBean) property.cloneVersion(newVersion);
//                }
//            }
//        }
//        return result;
//    }
//
//    public static List arrayToList(CommonDDBean[] beans) {
//        List result = null;
//
//        if(beans != null && beans.length > 0) {
//            result = new ArrayList(beans.length+3);
//            for(int i = 0; i < beans.length; i++) {
//                result.add(beans[i]);
//            }
//        }
//
//        return result;
//    }

    private static final String [] booleanStrings = {
        "0", "1",           // NOI18N
        "false", "true",    // NOI18N
        "no", "yes",        // NOI18N
        "off", "on"         // NOI18N
    };

    public static boolean booleanValueOf(String val) {
        boolean result = false;
        int valueIndex = -1;

        if(val != null && val.length() > 0) {
            val = val.trim();
            for(int i = 0; i < booleanStrings.length; i++) {
                if(val.compareToIgnoreCase(booleanStrings[i]) == 0) {
                    valueIndex = i;
                    break;
                }
            }
        }

        if(valueIndex >= 0) {
            if(valueIndex%2 == 1) {
                result = true;
            }
        }

        return result;
    }
    
    public static String encodeUrlField(String url) {
        String encodedUrl = url;
        
        // Change spaces to underscores - this step might be redundant now, considering
        // the UTF8 encoding being done now.
        if(encodedUrl != null) {
            encodedUrl = encodedUrl.replace (' ', '_'); //NOI18N
        }
        
        // For each url element, do UTF encoding of that element.
        if(encodedUrl != null) { // see bug 56280
            try {
                StringBuffer result = new StringBuffer(encodedUrl.length() + 10);
                String s[] = encodedUrl.split("/"); // NOI18N
                for(int i = 0; i < s.length; i++) {
                    result.append(URLEncoder.encode(s[i], "UTF-8")); // NOI18N
                    if(i != s.length - 1) {
                        result.append("/"); // NOI18N
                    }
                }
                encodedUrl = result.toString();
            } catch (Exception ex) {
                // log this
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return encodedUrl;
    }

    public static URL getResourceURL(String resource, Class relatedClass) {
        URL result = null;
        ClassLoader classLoader = relatedClass.getClassLoader();

        if(classLoader instanceof java.net.URLClassLoader) {
            URLClassLoader urlClassLoader = (java.net.URLClassLoader) classLoader;
            result = urlClassLoader.findResource(resource);
        } else {
            result = classLoader.getResource(resource);
        }

        return result;
    }

    public static void invokeHelp(String helpId) {
        invokeHelp(new HelpCtx(helpId));
    }

    public static void invokeHelp(final HelpCtx helpCtx) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                helpCtx.display();
            }
        });
    }

    public static boolean interpretCheckboxState(ItemEvent e) {
        boolean state = false;

        if(e.getStateChange() == ItemEvent.SELECTED) {
            state = true;
        } else if(e.getStateChange() == ItemEvent.DESELECTED) {
            state = false;
        }

        return state;
    }

}
