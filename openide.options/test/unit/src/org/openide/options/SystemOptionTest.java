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

package org.openide.options;

import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.util.SharedClassObject;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

/** Test system options (their serialization and deserialization specifically).
 * @author Jesse Glick
 */
public class SystemOptionTest extends NbTestCase {

    public SystemOptionTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(SystemOptionTest.class));
    }
    
    /** Test manipulation of a SystemOption in memory.
     */
    @RandomlyFails // http://deadlock.netbeans.org/job/NB-Core-Build/9882/testReport/
    public void testBasicUsage() throws Exception {
        assertNull(SharedClassObject.findObject(SimpleOption.class, false));
        SimpleOption o = (SimpleOption)SharedClassObject.findObject(SimpleOption.class, true);
        assertEquals(3, o.getX());
        assertEquals("hello", o.getY());
        o.setX(5);
        o.setY("nue");
        assertEquals(5, o.getX());
        assertEquals("nue", o.getY());
    }
    
    /** Test deserializability of a simple option.
     * Uses both SCO properties and static vars for storage; SystemOption
     * should treat them the same because the vars match the property types.
     */
    public void testDeserialization() throws Exception {
        InputStream is = getClass().getResourceAsStream("simpleOption2.ser");
        assertNotNull("simpleOption2.ser exists", is);
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            SimpleOption2 o = (SimpleOption2)ois.readObject();
            assertEquals(4, o.getX());
            assertEquals(4, o.getX2());
            assertEquals("four", o.getY());
            assertEquals("four", o.getY2());
        } finally {
            is.close();
        }
    }
    
    /** Test deserializability of an option which stores props in a special way.
     * Note that it keeps properties but they are not assignable to the property type.
     * I.e. the property type is for client use only, not for storage.
     * The Cell's are actually stored; we keep track of the "natural state"; the .ser
     * was stored with saveNatural turned on, so we check that it is really deserializing
     * the Cell's and not just calling the getters with the public values.
     */
    public void testMixedTypeDeserialization() throws Exception {
        InputStream is = getClass().getResourceAsStream("mixedTypeOption.ser");
        assertNotNull("mixedTypeOption.ser exists", is);
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            MixedTypeOption o = (MixedTypeOption)ois.readObject();
            assertEquals("25", o.getX());
            assertEquals(25, o.getY());
            // Makes a network connection: assertEquals(new URL("http://openide.netbeans.org/"), o.getZ());
            assertEquals("openide.netbeans.org", o.getZ().getHost());
            assertTrue(o.isNatural("x"));
            assertTrue(o.isNatural("y"));
            assertTrue(o.isNatural("z"));
            o.setY(26);
            assertEquals(26, o.getY());
            assertFalse(o.isNatural("y"));
        } finally {
            is.close();
        }
    }
    
    //
    // Implements reset to default values
    //
    
    public void testSimpleResetToOldValuesWhenTheyWereInitializedInInitialize () throws Exception {
        SimpleOption s = (SimpleOption)SimpleOption.findObject (SimpleOption.class, true);
        
        s.setX (-10);
        s.setY ("-10");
        
        s.reset ();
        
        assertEquals ("Was 3 in initialize", 3, s.getX ());
        assertEquals ("Was hello", "hello", s.getY ());
    }
    
    public void testSimpleResetEvenWhenWeHaveStaticInitialValues () throws Exception {
        SimpleOption2 s = (SimpleOption2)SimpleOption.findObject (SimpleOption2.class, true);
        
        class PL implements java.beans.PropertyChangeListener {
            public int cnt;
            public String name;
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                cnt++;
                name = ev.getPropertyName ();
            }
            
            public void assertChange (String name, int cnt) {
                if (name != null) {
                    assertEquals ("The this property had to change", name, this.name);
                    this.name = null;
                }
                if (cnt != -1) {
                    assertEquals ("This number of times", cnt, this.cnt);
                    this.cnt = 0;
                }
            }
        }
        
        PL pl = new PL ();
        s.addPropertyChangeListener (pl);
        
        s.setX (-10);
        pl.assertChange ("x", 1);
        s.setX (-9);
        pl.assertChange ("x", 1);
        s.setY ("-10");
        pl.assertChange ("y", 1);
        s.setX2 (7777);
        s.setY2 ("-4444");
        
        s.reset ();
        
        assertEquals ("Was 3 in initialize", 3, s.getX ());
        assertEquals ("Was hello", "hello", s.getY ());
        assertEquals ("2 Was 3 in initialize", 3, s.getX2 ());
        assertEquals ("2 Was hello", "hello", s.getY2 ());
        
        
    }
    
    public void testTheProblemWithI18NOptionsAsDiscoveredRecentlyWithIssue20962 () throws Exception {
        I18NLikeOption s = (I18NLikeOption)I18NLikeOption.findObject (I18NLikeOption.class, true);

        assertFalse ("Not set by default", s.isAdvancedWizard ());
        s.setAdvancedWizard (true);
        assertTrue ("Changes to true", s.isAdvancedWizard ());
        s.reset ();
        
        assertFalse ("Is cleared", s.isAdvancedWizard ());
        
        s.setAdvancedWizard (true);
        assertTrue ("yes again", s.isAdvancedWizard ());
        s.setAdvancedWizard (false);
        assertFalse ("no again", s.isAdvancedWizard ());
        s.reset ();
        assertFalse ("still no", s.isAdvancedWizard ());
    }
    
    // XXX test that serialization works and matches deserialization
    // (hint: use MaskingURLClassLoader from SharedClassObjectTest)
    
    // XXX test that SharedClassObject.find(optionClass, true) asks Lookup for the singleton
    
    // XXX test that the BeanInfo property descriptors are really used to determine
    // property names and writability (and that these can override getter/setter name munging)
    
    // XXX test that isReadExternal/isWriteExternal work
    
    // XXX test that read-only properties are not stored
    
    // XXX test that deser failure of one property does not break others
    
    // XXX ContextSystemOptionTest: test that child options are stored correctly, bean context
    // works, serialization stores all of them together
    
    // XXX VetoSystemOptionTest: test that you can veto some property changes
    
    public static final class SimpleOption extends SystemOption {
        public String displayName() {
            return "SimpleOption";
        }
        protected void initialize() {
            super.initialize();
            setX(3);
            setY("hello");
        }
        public int getX() {
            return ((Integer)getProperty("x")).intValue();
        }
        public void setX(int x) {
            putProperty("x", new Integer(x), true);
        }
        public String getY() {
            return (String)getProperty("y");
        }
        public void setY(String y) {
            putProperty("y", y, true);
        }
        
        public void reset () {
            super.reset ();
        }
    }
    
    public static final class SimpleOption2 extends SystemOption {
        private static final long serialVersionUID = 2456964509644026223L;
        public String displayName() {
            return "SimpleOption2";
        }
        private static int x2 = 3;
        private static String y2 = "hello";
        protected void initialize() {
            super.initialize();
            setX(3);
            setY("hello");
        }
        public int getX() {
            return ((Integer)getProperty("x")).intValue();
        }
        public void setX(int x) {
            putProperty("x", new Integer(x), true);
        }
        public String getY() {
            return (String)getProperty("y");
        }
        public void setY(String y) {
            putProperty("y", y, true);
        }
        public int getX2() {
            return x2;
        }
        public void setX2(int nue) {
            int old = x2;
            x2 = nue;
            firePropertyChange("x2", new Integer(old), new Integer(nue));
        }
        public String getY2() {
            return y2;
        }
        public void setY2(String nue) {
            String old = y2;
            y2 = nue;
            firePropertyChange("y2", old, nue);
        }
        public void reset () {
            super.reset ();
        }
    }
    
    public static class MixedTypeOption extends SystemOption {
        private static final class Cell implements Serializable {
            private static final long serialVersionUID = -2882494319143608556L;
            public final Object o;
            public final boolean natural;
            public Cell(Object o, boolean natural) {
                this.o = o;
                this.natural = natural;
            }
        }
        private static final long serialVersionUID = 262688904041933263L;
        public static boolean saveNatural = false;
        public boolean isNatural(String prop) {
            return ((Cell)getProperty(prop)).natural;
        }
        public String displayName() {
            return "MixedTypeOption";
        }
        protected void initialize() {
            super.initialize();
            putProperty("x", new Cell(new Integer(12), true));
            putProperty("y", new Cell("12", true));
            putProperty("z", new Cell("http://www.netbeans.org/", true));
        }
        public String getX() {
            return ((Integer)((Cell)getProperty("x")).o).toString();
        }
        public void setX(String x) {
            putProperty("x", new Cell(new Integer(x), saveNatural));
        }
        public int getY() {
            return Integer.parseInt((String)((Cell)getProperty("y")).o);
        }
        public void setY(int i) {
            putProperty("y", new Cell(String.valueOf(i), saveNatural));
        }
        public URL getZ() {
            try {
                return new URL((String)((Cell)getProperty("z")).o);
            } catch (MalformedURLException mfue) {
                throw new IllegalStateException(mfue.toString());
            }
        }
        public void setZ(URL z) {
            putProperty("z", new Cell(z.toString(), saveNatural));
        }
    }

    public static class I18NLikeOption extends SystemOption {
        public static final String PROP_ADVANCED_WIZARD = "advancedWizard"; 
        
        /** Implements superclass abstract method. */
        public String displayName() {
            return "I18NLikeOption";
        }
        
        /** Getter for init advanced wizard property. */
        public boolean isAdvancedWizard() {
            // Lazy init.
            if(getProperty(PROP_ADVANCED_WIZARD) == null)
                return false;

            return ((Boolean)getProperty(PROP_ADVANCED_WIZARD)).booleanValue();
        }

        /** Setter for init advanced wizard property. */
        public void setAdvancedWizard(boolean generateField) {
            // Stores in class-wide state and fires property changes if needed:
            putProperty(PROP_ADVANCED_WIZARD, generateField ? Boolean.TRUE : Boolean.FALSE, true);
        }
        
    }
}
