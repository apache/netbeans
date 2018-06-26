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

package org.netbeans.modules.j2ee.ddloaders.web;


import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import java.io.*;
import org.xml.sax.*;
//import java.util.*;


/**
 *
 * @author  mkuchtiak
 */
public class DDUtils {

    private static final String EXCEPTION_PREFIX="version:"; //NOI18N   
    
    /** Finds a name similar to requested that uniquely identifies 
     *  element between the other elements of the same name.
     *
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return a free element name
     */
    public static String findFreeName (CommonDDBean[] elements, String identifier, String name) {
        if (checkFreeName (elements, identifier, name)) {
            return name;
        }
        for (int i = 1;;i++) {
            String destName = name + "_"+i; // NOI18N
            if (checkFreeName (elements, identifier, destName)) {
                return destName;
            }
        }
    }
    
    /** Test if given name is free in given context.
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return true, if such name does not exists
     */
    private static boolean checkFreeName (CommonDDBean [] elements, String identifier, Object o) {
        for (int i=0; i<elements.length; i++) {
            Object val = elements[i].getValue (identifier);
            if (val != null && val.equals (o)) {
                return false;
            }
        }
        return true;
    }

    /**  Convenient method for getting the BaseBean object from CommonDDBean object
    */
    public static BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) return (BaseBean)bean;
        else if (bean instanceof WebAppProxy) return (BaseBean) ((WebAppProxy)bean).getOriginal();
        return null;
    }
  
}
