/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * SimpleKey.java
 *
 * Created on January 31, 2004, 6:28 PM
 */

package org.netbeans.actions.simple;

import java.lang.reflect.Method;
import java.util.Map;
import org.xml.sax.SAXException;

/**
 *
 * @author  Tim Boudreau
 */
public class SimpleKey {
    private String value;
    private String method;
    private String clazz;
    private boolean mustContain;
    private String mustMatch;
    /** Creates a new instance of SimpleKey */
    public SimpleKey(String value, String method, String clazz,
        boolean mustContain, String mustMatch) throws SAXException {
        this.value = value;
        this.method = method;
        this.clazz = clazz;
        this.mustContain = mustContain;
        this.mustMatch = mustMatch;
        if ((clazz == null) != (method == null)) {
            throw new SAXException ("Key must define both a class and a " +
            "method to call on it if it defines one.  Class: " + clazz + " method " + method + " name " + value);
        }
        if (clazz != null && mustMatch != null) {
            throw new SAXException("A key may define a method to call, or a value to match from the map, not both. Key:" + value);
        }
        System.err.println("SimpleKey: " + value + " mustContain " + mustContain + " mustMatch " + mustMatch + " class " + clazz + " method " + method);
    }
    
    public boolean mustTest () {
        return clazz != null || mustMatch != null;
    }
    
    public boolean isSimpleTest() {
        return clazz == null;
    }
    
    public Object getValue() {
        return value;
    }
    
    public boolean isMustContain() {
        return mustContain;
    }
    
    public boolean test (Map m) {
//        System.err.println("TEST CONSTRAINT " + value);
        String s = (String) m.get(value);
        boolean result = s != null;
        if (!mustContain) {
            result = !result;
        } else if (result) {
            if (mustMatch != null) {
//                System.err.println("TESTING " + value + " MUSTMATCH " + mustMatch + " value is " + s);
                result = s.equals (mustMatch);
            }
        }
//        System.err.println("   SimpleKey test " + value + " result=" + result);
        if (result && clazz != null) {
            result = invokeMethod (m, method, clazz);
//            System.err.println("   Invoked method - result " + result);
        }
        return result;
    }
    
    public String toString() {
        return value;
    }
    
    /** Asymmetric impl of equals & hashcode, but works for a quick demo */
    public boolean equals (Object o) {
        if (o instanceof SimpleKey) {
            SimpleKey oth = (SimpleKey) o;
            return oth.method == method && oth.clazz == clazz && oth.mustContain ==
                mustContain && oth.mustMatch == mustMatch && oth.value == value;
        } else if (o instanceof String) {
            return ((String)o).equals(value);
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return value.hashCode();
    }
    
    private boolean invokeMethod (Map m, String method, String clazz) {
        if (theClass == null) {
            try {
                theClass = Class.forName (clazz);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        Object o = m.get(value);
        if (o == null) {
            return false;
        }
        if (theMethod == null) {
            try {
            theMethod = theClass.getDeclaredMethod (method, null);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            
            if (theMethod.getReturnType() != Boolean.class && 
                theMethod.getReturnType() != Boolean.TYPE) {
                    throw new IllegalArgumentException ("Method " + method + 
                    " on " + clazz + " must return boolean or Boolean and " +
                    "take no arguments"); //NOI18N
            }
        }
        Boolean result = Boolean.FALSE;
        try {
            result = (Boolean) theMethod.invoke(o, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.booleanValue();
    }
    
    private Class theClass = null;
    private Method theMethod = null;
}
