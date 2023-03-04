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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node.Property;
import org.openide.util.RequestProcessor;



/**
 * @author Some Czech
 */
public class PropertiesTest extends NbTestCase {

    public PropertiesTest(String name) {
        super(name);
    }

    public void testReflection() throws Exception {

        Node.Property np = null;

        // Test normal property
        TestBean tb = new TestBean();
        np = new PropertySupport.Reflection( tb, int.class, "number" );        
        assertEquals( "Value", np.getValue(), new Integer( 1 ) );
        assertEquals("number", np.getName());
        
        // Test setter only of type String
        NoSuchMethodException thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, String.class, "setterOnlyString" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        // Test setter only of type boolean
        thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, boolean.class, "setterOnlyBoolean" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        // Test no boolean with is
        thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, long.class, "isSetLong" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        
        // Test get/set boolean
        np = new PropertySupport.Reflection( tb, boolean.class, "getSetBoolean" );        
        assertEquals( "Value", np.getValue(), Boolean.TRUE ); 
        assertEquals("getSetBoolean", np.getName());
                
        // Test is/set boolean
        np = new PropertySupport.Reflection( tb, boolean.class, "isSetBoolean" );        
        assertEquals( "Value", np.getValue(), Boolean.TRUE ); 

        // Test names with just one getter or setter
        np = new PropertySupport.Reflection<Integer>(tb, int.class, "getNumber", null);
        assertTrue(np.canRead());
        assertFalse(np.canWrite());
        assertEquals("number", np.getName());
        np = new PropertySupport.Reflection<Integer>(tb, int.class, null, "setNumber");
        assertFalse(np.canRead());
        assertTrue(np.canWrite());
        assertEquals("number", np.getName());
    }
    
    public static class TestBean {
        
        public int getNumber() {
            return 1;
        }
        
        public void setNumber( int number ) {
        }
        
        public void setSetterOnlyString( String text ) {
        }
        
        public void setSetterOnlyBoolean( boolean value ) {
        }
        
        public long isIsSetLong() {
            return 10L;
        }
        
        public void setIsSetLong( long value ) {
        }
        
        public boolean getGetSetBoolean() {
            return true;
        }
        
        public void setGetSetBoolean( boolean value ) {
        }
        
        
        public boolean isIsSetBoolean() {
            return true;
        }
        
        public void setIsSetBoolean( boolean value ) {
        }
    }

    public void testHashCodeDoesNotThrowNPE() {
        Node.PropertySet ps = new Node.PropertySet() {
            @Override
            public Property<?>[] getProperties() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        assertEquals("Zero", 0, ps.hashCode());
            
    }

    public void testNeverSharePropertyEditorBetweenTwoThreads() {
        final Node.Property p = new PropertySupport.ReadOnly<String>("name", String.class, "display", "short") {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return "Hello";
            }
        };
        
        PropertyEditor p1 = p.getPropertyEditor();
        
        class R implements Runnable {
            PropertyEditor p2;

            @Override
            public void run() {
                p2 = p.getPropertyEditor();
            }
        }
        R r = new R();
        RequestProcessor.getDefault().post(r).waitFinished();
        
        assertNotNull("Find 1", p1);
        assertNotNull("Find 2", r.p2);
        if (p1 == r.p2) {
            fail("Editors obtained from different threads should be different! was " + p1 + " and " + r.p2);
        }
    }
}
