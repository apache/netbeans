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

package org.netbeans.insane.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 *
 * @author nenik
 */
public abstract class Root {

    private Root() {
    }
    
    public abstract String describe();
    public abstract Object getObject();
    
    public static Root createNamed(String name, Object ref) {
        return new Named(name, ref);
    }
    
    public static Root createStatic(Field f, Object ref) {
        return new Static(f, ref);
        
    }
    
    private static class Named extends Root {
        private String name;
        private Object ref;
        
        Named(String name, Object ref) {
            if (ref == null) new Exception().printStackTrace();
            this.ref = ref;
            this.name = name;
        }
        
        public Object getObject() {
            return ref;
        }
        
        public String describe() {
            return name;
        }
    }
    
    private static class Static extends Root {
        private Class cls;
        private int i;
        
        Static(Field f, Object ref) {
            assert ((f.getModifiers() & Modifier.STATIC) != 0);
            
            cls = f.getDeclaringClass();
            i = Arrays.asList(cls.getDeclaredFields()).indexOf(f);
            assert (i >= 0);
            assert cls.getDeclaredFields()[i].equals(f);
        }
        
        public String describe() {
            return getField().toString();
        }

        public Object getObject() {
            try {
                return getField().get(null);
            } catch (Exception e) {
                return null;
            }
        }

        private Field getField() {
            return cls.getDeclaredFields()[i];
        }
    }
    
}
