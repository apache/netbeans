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
