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
