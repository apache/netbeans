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
 * SimpleConstraint.java
 *
 * Created on January 25, 2004, 5:52 PM
 */

package org.netbeans.actions.simple;

import java.util.Arrays;
import java.util.Map;
import org.xml.sax.SAXException;

/**
 *
 * @author  Tim Boudreau
 */
class SimpleConstraint {
    private String name;
    private SimpleKey[] includeKeys;
    private SimpleKey[] excludeKeys;
    private boolean enabledType;

    /** Creates a new instance of SimpleConstraint */
    public SimpleConstraint(String name, SimpleKey[] includeKeys, SimpleKey[] excludeKeys, boolean enabledType) throws SAXException {
        this.name = name;
        this.includeKeys = includeKeys;
        this.excludeKeys = excludeKeys;
        this.enabledType = enabledType;
        if (name == null) {
            throw new SAXException ("Name may not be null");
        }
        if (includeKeys.length == 0 && excludeKeys.length ==0) {
            throw new SAXException ("Constraint has no keys");
        }
        System.err.println("Constraint: " + name + " includeKeys " + Arrays.asList(includeKeys) + " excludeKeys: " + Arrays.asList(excludeKeys) + " enabledType: " + enabledType);
    }
    
    public boolean isEnabledType() {
        return enabledType;
    }
    
    public boolean test (Map context) {
        boolean result = true;
//        System.err.println("Test constraint " + name);
        for (int i=0; i < excludeKeys.length; i++) {
            result &= !context.containsKey (excludeKeys[i]);
//            System.err.println(" check must not contain " + excludeKeys[i]);
            if (result && excludeKeys[i].mustTest()) {
                result &= excludeKeys[i].test(context);
//                System.err.println("    Must test: " + result);
            }
            if (!result) {
//                System.err.println(" FOUND AN INCLUDED KEY IN EXCLUSION SET FOR " + name + " returning false");
                return result;
            }
        }
        for (int i=0; i < includeKeys.length; i++) {
            result &= context.containsKey (includeKeys[i]);
//            System.err.println(" check must contain " + includeKeys[i] + " contained? " + result);
            if (result && includeKeys[i].mustTest()) {
                result &= includeKeys[i].test(context);
//                System.err.println("  must test it - got " + result);
            }
            if (!result) {
                return result;
            }
        }
        return result;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
    public boolean equals (Object o) {
        boolean result = false;
        if (o.getClass() == SimpleConstraint.class) {
            result = o.toString().equals(toString());
        }
        return result;
    }
}
