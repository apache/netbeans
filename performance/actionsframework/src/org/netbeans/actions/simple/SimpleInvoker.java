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
 * SimpleInvoker.java
 *
 * Created on January 25, 2004, 5:53 PM
 */

package org.netbeans.actions.simple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author  tim
 */
class SimpleInvoker {
    private String targetClass;
    private String targetMethod;
    private String name;
    private boolean isDirect;
    /** Creates a new instance of SimpleInvoker */
    public SimpleInvoker(String name, String targetClass, String targetMethod, boolean isDirect) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.isDirect = isDirect;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private Class clazz = null;
    private Class getTargetClass() throws ClassNotFoundException {
        if (clazz == null) {
            clazz = Class.forName(targetClass);
        }
        return clazz;
    }

    private Method method = null;
    private Method getTargetMethod() throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException {
        if (method == null) {
            Class c = getTargetClass();
            method = c.getDeclaredMethod(targetMethod, null);
            method.setAccessible(true);
        }
        return method;
    }
    
    public void invoke (Map context) {
        try {
            if (isDirect) {
                getTargetMethod().invoke(null, null);
            } else {
                Class clazz = getTargetClass();
                Object o = context.get(clazz);
                if (o == null) {
                    throw new NullPointerException ("No instance of " + clazz + " in context");
                }
                getTargetMethod().invoke(o, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String toString() {
        return getClass() + "[" + targetClass + " method " + targetMethod + " name=" + getName() + " isDirect=" + isDirect + "]";
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
    public boolean equals (Object o) {
        boolean result = false;
        if (o.getClass() == SimpleInvoker.class) {
            result = ((SimpleInvoker)o).getName().equals(toString());
        }
        return result;
    }    
    
}
