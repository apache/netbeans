/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport.Reflection;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class PropertySupportTest extends NbTestCase {
    private Bean bean = new Bean();
    private PropertyDescriptor[] descriptors;
    
    public PropertySupportTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        BeanInfo res = Introspector.getBeanInfo(Bean.class);
        descriptors = res.getPropertyDescriptors();
    }
    
    
    
    public void testIntrospectPrimitiveBoolean() {
        assertProperty("b", boolean.class);
    }
    public void testSupportPrimitiveBoolean() throws Exception {
        Object b = new PropertySupport.Reflection<Boolean>(bean, boolean.class, "b");
        assertNotNull("Support found", b);
    }
    public void testIntrospectBoolean() {
        assertProperty("bigB", null);
    }
    public void testSupportIntrospectBoolean() {
        try {
            Object no = new PropertySupport.Reflection<Boolean>(bean, Boolean.class, "bigB");
            fail("There is no getBigB method");
        } catch (NoSuchMethodException ex) {
            // OK
            assertTrue("contains error about getBigB", ex.getMessage().contains("getBigB"));
        }
        
    }
    public void testHasIsNotPrefix() {
        assertProperty("c", null);
    }
    public void testWrapperPropertyFound() {
        assertProperty("wrapper", Boolean.class);
    }
    public void testSupportWrapperProperty() throws Exception {
        Reflection<Boolean> ps = new Reflection<Boolean>(bean, Boolean.class, "wrapper");
        assertEquals("Getter returns true", Boolean.TRUE, ps.getValue());
    }
    

    private void assertProperty(String name, Class<Boolean> type) {
        StringBuilder sb = new StringBuilder();
        for (PropertyDescriptor pd : this.descriptors) {
            sb.append(pd.getName()).append("\n");
            if (name.equals(pd.getName())) {
                if (type == null) {
                    fail("Unexpected property found " + name);
                }
                assertEquals(type, pd.getReadMethod().getReturnType());
                return;
            }
        }
        if (type != null) {
            fail("No property " + name + " found:\n" + sb);
        }
    }
    
    
    public static final class Bean {
        public boolean isB() {
            return true;
        }
        public void setB(boolean b) {
        }
        
        public boolean hasC() {
            return true;
        }
        
        public Boolean isBigB() {
            return true;
        }
        
        public Boolean getWrapper() {
            return true;
        }
        public void setWrapper(Boolean b) {
        }
    }
}
