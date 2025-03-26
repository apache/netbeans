/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.MockLookup;

/**
 * Checks that stateful action support works as designed.
 * 
 * @author sdedic
 */
public class StatefulActionProcessorTest extends NbTestCase implements ContextGlobalProvider {
    MockLookup mockLookup;
    static Action instance;
    static ActionListener instance2;
    static int created;
    static ActionEvent received;
    
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public StatefulActionProcessorTest(String n) {
        super(n);
        MockLookup.init();
    }

    @Override
    protected void tearDown() throws Exception {
        created = 0;
        instance = null;
        instance2 = null;
        received = null;
        
        Field f = Utilities.class.getDeclaredField("global");
        f.setAccessible(true);
        f.set(null, null);

        super.tearDown(); 
    }
    
    private InstanceContent lookupContent;
    private AbstractLookup testLookup;
    private PL actionLookup = new PL();
    
    private static class PL extends ProxyLookup {
        void setLookupsAccessor(Lookup... lookups) {
            super.setLookups(lookups);
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        created = 0;
        instance = null;
        instance2 = null;
        received = null;
        
        reinitActionLookup();
        ClassLoader l = MockLookup.class.getClassLoader();
        MockLookup.setLookup(Lookups.fixed(this), Lookups.metaInfServices(l), Lookups.singleton(l));
    }
    
    void reinitActionLookup() {
        lookupContent = new InstanceContent();
        testLookup = new AbstractLookup(lookupContent);
        actionLookup.setLookupsAccessor(testLookup);
    }

    @Override
    public Lookup createGlobalContext() {
        return actionLookup;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    
    public static enum EnValue {
        ONE, TWO
    }
    
    static interface NonpublicListener {
        public void callback();
    }
    
    public static interface CustomListener {
        public void callback();
    }
    
    public static class ClassListener {
        
    }
    
    public static class ActionModel {
        boolean boolProp;
        boolean bool2Prop;
        Boolean boolObjectProp;
        EnValue enumProp;
        String  prop;
        Object  anyProp;
        boolean noneBoolProp;
        int intProp;
        
        PropertyChangeSupport supp = new PropertyChangeSupport(this);
        
        public boolean getBool2Prop() {
            return bool2Prop;
        }

        public boolean isBoolProp() {
            return boolProp;
        }

        public Boolean getBoolObjectProp() {
            return boolObjectProp;
        }

        public String getProp() {
            return prop;
        }

        public String getAnyProp() {
            return null;
        }
        
        int getIntProp() {
            return 0;
        }
    }
    
    public static class DefaultActionModel {
        boolean boolProp;
        boolean bool2Prop;

        PropertyChangeSupport supp = new PropertyChangeSupport(this);

        public boolean isEnabled() {
            return boolProp;
        }
        
        public void setEnabled(boolean e) {
            this.boolProp = e;
            supp.firePropertyChange("enabled", null, null);
            
        }
        public boolean getSwingSelectedKey() {
            return bool2Prop;
        }
        
        public void setSwinSelectedKey(boolean s) {
            this.bool2Prop = s;
            supp.firePropertyChange(Action.SELECTED_KEY, null, null);
        }
    }
    
    public static class ActionModel2 extends ActionModel {
        public void addPropertyChangeListener(String prop, PropertyChangeListener p) {
            supp.addPropertyChangeListener(prop, p);
        }

        public void removePropertyChangeListener(String prop, PropertyChangeListener p) {
            supp.removePropertyChangeListener(prop, p);
        }
    }

    public static class ActionModel3 extends ActionModel {
        public void addPropertyChangeListener(PropertyChangeListener p) {
            supp.addPropertyChangeListener(p);
        }

        public void removePropertyChangeListener(PropertyChangeListener p) {
            supp.removePropertyChangeListener(prop, p);
        }
    }
    
    public static class ActionModel4 extends ActionModel {
        List<ChangeListener> listeners = new ArrayList<>();
        
        public void fire() {
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : listeners) {
                l.stateChanged(e);
            }
        }
        
        public void addChangeListener(ChangeListener p) {
            listeners.add(p);
        }

        public void removeChangeListener(ChangeListener p) {
            listeners.remove(p);
        }
    }
    
    public static class ActionModel5 extends ActionModel {
        List<CustomListener> listeners = new ArrayList<>();
        
        public void fire() {
            for (CustomListener l : listeners) {
                l.callback();
            }
        }

        public void addCustomListener(CustomListener p) {
            listeners.add(p);
        }

        public void removeCustomListener(CustomListener p) {
            listeners.remove(p);
        }
    }
    
    /**
     * Checks that without type() the default is used, but the property must be specified
     */
    public void testCheckOnTypeNoProperty() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState()) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("Property must be specified")) {
            fail("Property must be specified:\n" + os);
        }
    }

    /**
     * Checks that missing getter is reported
     */
    public void testCheckOnGetterNotExist() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"rumcajs\")) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("Property rumcajs not found")) {
            fail("Property must be specified:\n" + os);
        }
    }

    /**
     * Property getter must be public
     */
    public void testCheckOnNonpublicGetter() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"intProp\")) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("must be public")) {
            fail("Property must be checked for public access:\n" + os);
        }
    }

    /**
     * Checks that boolean "isXX" getter is found
     */
    public void testCheckBooleanGetter1() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"boolProp\")) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation must be successful", r);
    }

    /**
     * Checks that boolean getXXX getter is found
     */
    public void testCheckBooleanGetter2() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"bool2Prop\")) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation must be successful", r);
    }

    /**
     * listenOn type must be an interface
     */
    public void testInvalidListenerClass() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import javax.swing.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"prop\", listenOn=ClassListener.class)) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        if (!os.toString().contains("is not an interface")) {
            fail("class as listener type must be reported" + os);
        }
    }

    /**
     * listenOn type must be public
     */
    public void testNonpublicListenerClass() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import javax.swing.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"prop\", listenOn=NonpublicListener.class)) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        String msg = os.toString();
        if (!msg.contains("cannot find symbol") && !msg.contains("is not public")) {
            fail("Nonpublic listener type must be reported" + msg);
        }
    }

    /**
     * no addXxxxListener is present for the specified type
     */
    public void testMissingAddListener() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import javax.swing.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = @ActionState(type = ActionModel2.class, property=\"prop\", listenOn=ChangeListener.class)) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        if (!os.toString().contains("Method addChangeListener not found")) {
            fail("Missing add listener must be reported" + os);
        }
    }

    /**
     * issues an error if the specified trigger method does not exist
     */
    public void testMissingListenerMethod() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionState;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import javax.swing.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "import org.netbeans.modules.openide.awt.StatefulActionProcessorTest.*;\n" +
                    
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"test.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", checkedOn = "
                    + "@ActionState(type = ActionModel5.class, property=\"prop\", listenOn=CustomListener.class, listenOnMethod=\"bubu\")) " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    " +
            "    public static class B implements ActionListener {\n" +
            "       public B(ActionModel mdl) {} \n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        if (!os.toString().contains("does not contain method bubu")) {
            fail("Missing listener method must be reported" + os);
        }
    }
    
    @ActionID(id = "test.DefEnableAction", category="Foo")
    @ActionRegistration(displayName = "TestAction", 
            enabledOn = @ActionState()
    )
    public static class DefEnableAction implements ActionListener {

        public DefEnableAction(DefaultActionModel model) {
            instance2 = this;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    /**
     * Checks that the "enabled" property is used by default if property is not specified
     * @throws Exception 
     */
    public void testDefaulPropertyEnable() throws Exception {
        Action a = Actions.forID("Foo", "test.DefEnableAction");
        assertFalse(a.isEnabled());
        
        
        DefaultActionModel mod = new DefaultActionModel();
        lookupContent.add(mod);
        
        assertFalse(a.isEnabled());
        
        mod.setEnabled(true);
        assertTrue(a.isEnabled());
    }
    
    public static class NonNullModel {
        Collection prop1;
        
        PropertyChangeSupport supp = new PropertyChangeSupport(this);
        
        public Collection getProp1() {
            return prop1;
        }
        
        public void setProp1(Collection c) {
            prop1 = c;
            supp.firePropertyChange("prop1", null, null);
        }
    }
    
    @ActionID(id = "test.NonNull", category="Foo")
    @ActionRegistration(displayName = "TestAction", 
            enabledOn = @ActionState(property = "prop1", checkedValue = ActionState.NON_NULL_VALUE)
    )
    public static class NonNullAction implements ActionListener {

        public NonNullAction(NonNullModel model) {
            instance2 = this;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    @ActionID(id = "test.Null", category="Foo")
    @ActionRegistration(displayName = "TestAction", 
            enabledOn = @ActionState(property = "prop1", checkedValue = ActionState.NULL_VALUE)
    )
    public static class NullAction extends NonNullAction {

        public NullAction(NonNullModel model) {
            super(model);
        }
    }
    
    /**
     * Checks that the action enables on null property value
     */
    public void testEnableOnNull() throws Exception {
        Action a = Actions.forID("Foo", "test.Null");
        assertNotNull(a);
        assertFalse(a.isEnabled());
        
        // now provide model with non-null set up already
        NonNullModel mdl = new NonNullModel();
        
        lookupContent.add(mdl);
        
        assertTrue("Must be enabled after model with property arrives", a.isEnabled());
        
        mdl.setProp1(new ArrayList<>());
        assertFalse("Must disable when property becomes null", a.isEnabled());
    }

    /**
     * Checks that the action enables on non-null property value
     */
    public void testEnableOnNonNull() throws Exception {
        Action a = Actions.forID("Foo", "test.NonNull");
        assertNotNull(a);
        assertFalse(a.isEnabled());
        
        // now provide model with non-null set up already
        NonNullModel mdl = new NonNullModel();
        mdl.setProp1(new ArrayList<>());
        
        lookupContent.add(mdl);
        
        assertTrue("Must be enabled after model with property arrives", a.isEnabled());
        
        mdl.setProp1(null);
        assertFalse("Must disable when property becomes null", a.isEnabled());
    }
    
    @NbBundle.Messages({
        "TestAction=Test action"
    })
    @ActionID(id = "test.InstAction", category="Foo")
    @ActionRegistration(displayName = "#TestAction", 
            enabledOn = @ActionState(property = "boolProp")
    )
    public static class InstAction implements ActionListener {
        public InstAction(ActionModel3 model) {
            created++;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    /**
     * Checks that the stateful action is instantiated only just before it is
     * invoked. Enablement should be evaluated by the framework without any
     * user code loaded.
     */
    public void testEnableActionInstantiation() {
        assertEquals("Not pre-created", 0, created);
        Action a = Actions.forID("Foo", "test.InstAction");
        assertNotNull(a);
        assertEquals("Not direcly created from layer", 0, created);

        assertFalse("No data in lookup", a.isEnabled());
        assertEquals("Not created unless data present", 0, created);
        
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        
        // still not enabled
        assertFalse("Property not sets", a.isEnabled());
        assertEquals("Not created unless guard is set", 0, created);
        
        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        assertTrue("Property not set", a.isEnabled());
        assertEquals("Not created before invocation", 0, created);

        a.actionPerformed(new ActionEvent(this, 0, "cmd"));
        assertEquals("Not created before invocation", 1, created);
        assertNotNull(received);
        assertEquals("cmd", received.getActionCommand());
    }
    
    /**
     * Checks that an action model is freed, after the actionPerformed is called,
     * and then the focus shifts so the model is not in Lookup.
     */
    public void testActionModelFreed() {
        Action a = Actions.forID("Foo", "test.InstAction");
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd"));
        
        reinitActionLookup();
        
        Reference<ActionModel3> r = new WeakReference<>(mod);
        mod = null;
        assertGC("Action model must be GCed", r);
    }
    
    @ActionID(id = "test.CustomEnableAction", category="Foo")
    @ActionRegistration(displayName = "TestAction", 
            enabledOn = @ActionState(property = "boolProp", useActionInstance = true)
    )
    public static class CustomEnableAction extends AbstractAction {
        final ActionModel3 model;
        
        public CustomEnableAction(ActionModel3 model) {
            created++;
            instance = this;
            setEnabled(false);
            this.model = model;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
            instance = this;
        }
    }
    
    /**
     * Checks that custom enable action is enabled on time, instantiated
     * only when the guard becomes true.
     */
    public void testCustomEnableAction() {
        Action a = Actions.forID("Foo", "test.CustomEnableAction");
        assertNotNull(a);
        assertFalse("No data in lookup", a.isEnabled());
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        // still not enabled
        assertFalse("Property not set", a.isEnabled());
        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        assertFalse("Action property not set", a.isEnabled());
        Action inst = instance;
        inst.setEnabled(true);
        assertTrue("Delegate must update enable", a.isEnabled());
    }
    
    /**
     * Checks that an action model is freed, after the actionPerformed is called,
     * and then the focus shifts so the model is not in Lookup.
     */
    public void testCustomActionModelFreed() {
        Action a = Actions.forID("Foo", "test.CustomEnableAction");
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd"));
        
        reinitActionLookup();
        
        Reference<ActionModel3> r = new WeakReference<>(mod);
        instance = null;
        mod = null;
        assertGC("Action model must be GCed", r);
    }
    
    /**
     * Check that action with action check enables only after the actual instance enables.
     */
    public void testCustomEnableActionInstantiation() {
        assertEquals("Not pre-created", 0, created);
        Action a = Actions.forID("Foo", "test.CustomEnableAction");
        assertNotNull(a);
        assertEquals("Not direcly created from layer", 0, created);
        a.isEnabled();
        assertEquals("Not created unless data present", 0, created);
        
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        
        // still not enabled
        assertFalse(a.isEnabled());
        assertEquals("Not created unless guard is set", 0, created);
        
        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        // should instantiate the action just because of the property change on guard,
        // now the action decides the final state.
        assertEquals("Must be created to evaluate enabled state", 1, created);
        
        Action inst = instance;
        assertNotNull(inst);
        
        inst.setEnabled(true);
        assertSame("Same instance for repeated enable", inst, instance);
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd"));
        assertSame("Same instance for invocation and enable eval", inst, instance);
        assertNotNull(received);
        assertEquals("cmd", received.getActionCommand());
    }
    
    /**
     * Checks that when the context object is changed, the old custom
     * action instance is trashed and a new one is created.
     */
    public void testCustomEnableActionChangesWithContext() {
        Action a = Actions.forID("Foo", "test.CustomEnableAction");
        ActionModel3 mod = new ActionModel3();
        mod.boolProp = true;

        lookupContent.add(mod);
        CustomEnableAction inst = (CustomEnableAction)instance;
        assertNotNull(inst);
        assertSame(mod, inst.model);
        assertFalse(a.isEnabled());
        
        inst.setEnabled(true);
        assertTrue(a.isEnabled());
        
        ActionModel3 mod2 = new ActionModel3();
        instance = null;
        lookupContent.remove(mod);
        lookupContent.add(mod2);
        assertNull(instance);

        mod2.boolProp = true;
        mod2.supp.firePropertyChange("boolProp", null, null);
        
        assertNotNull(instance);
        Action save2 = instance;
        
        assertFalse(a.isEnabled());
        ((AbstractAction)instance).setEnabled(true);
        a.actionPerformed(new ActionEvent(this, 0, "x"));
        
        assertSame(instance, save2);
        assertNotSame(instance, inst);
    }
    
    @ActionID(id = "test.ToggleAction", category="Foo")
    @ActionRegistration(displayName = "TestAction", checkedOn = @ActionState(property = "boolProp"))
    public static class ToggleAction implements ActionListener {
        
        public ToggleAction(ActionModel3 model) {
            instance2 = this;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    /**
     * Checks that toggle action carries the selected key
     */
    public void testToggleAction() throws Exception {
        Action a = Actions.forID("Foo", "test.ToggleAction");
        assertNotNull(a.getValue(Action.SELECTED_KEY));
        assertTrue(Boolean.TRUE.equals(a.getValue(Actions.ACTION_VALUE_TOGGLE)));
    }

    /**
     * Checks that the toggle action changes state according to the model
     * property. 
     * @throws Exception 
     */
    public void testToggleActionEabledStatePropChange1() throws Exception {
        Action a = Actions.forID("Foo", "test.ToggleAction");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertFalse("Action must be disabled without data", a.isEnabled());
        assertNull("Must not eagerly instantiate", instance2);
        
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);

        assertNull("Must not be created on data presence", instance2);
        assertTrue("Must be enabled when data is ready", a.isEnabled());
        assertFalse("Must not be checked unless guard is true", (Boolean)a.getValue(Action.SELECTED_KEY));

        mod.boolProp = true;
        mod.supp.firePropertyChange("bool2Prop", null, null);
        assertFalse("Unrelated property change should be ignored", (Boolean)a.getValue(Action.SELECTED_KEY));
        
        mod.supp.firePropertyChange("boolProp", null, null);
        assertTrue("Must become checked after prop change", (Boolean)a.getValue(Action.SELECTED_KEY));
        
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        
        assertNotNull(received);
        assertEquals("cmd2", received.getActionCommand());
    }
    
    /**
     * Checks that an action model is freed, after the actionPerformed is called,
     * and then the focus shifts so the model is not in Lookup.
     */
    public void testToggleActionModelFreed() {
        ActionModel3 mod = new ActionModel3();
        mod.boolProp = true;
        lookupContent.add(mod);
        
        Action a = Actions.forID("Foo", "test.ToggleAction");
        
        assertTrue("Must become checked after prop change", (Boolean)a.getValue(Action.SELECTED_KEY));
        a.actionPerformed(new ActionEvent(this, 0, "cmd"));
        
        reinitActionLookup();
        
        Reference<ActionModel3> r = new WeakReference<>(mod);
        mod = null;
        assertGC("Action model must be GCed", r);
    }
    

    @ActionID(id = "test.ToggleAction3", category="Foo")
    @ActionRegistration(displayName = "TestAction", checkedOn = @ActionState(property = "boolProp"))
    public static class ToggleAction3 implements ActionListener {
        public ToggleAction3(ActionModel4 model) {
            instance2 = this;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    /**
     * Checks that the action framework reacts uses {@code addChangeListener}
     * @throws Exception 
     */
    public void testToggleActionEabledStateChange() throws Exception {
        Action a = Actions.forID("Foo", "test.ToggleAction3");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertFalse("Action must be disabled without data", a.isEnabled());
        assertNull("Must not eagerly instantiate", instance2);
        
        ActionModel4 mod = new ActionModel4();
        lookupContent.add(mod);

        assertNull("Must not be created on data presence", instance2);
        assertTrue("Must be enabled when data is ready", a.isEnabled());
        assertFalse("Must not be checked unless guard is true", (Boolean)a.getValue(Action.SELECTED_KEY));

        mod.boolProp = true;
        mod.fire();
        assertTrue("Must become checked after prop change", (Boolean)a.getValue(Action.SELECTED_KEY));
        
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        
        assertNotNull(received);
        assertEquals("cmd2", received.getActionCommand());
    }

    @ActionID(id = "test.ToggleCustomCallback", category="Foo")
    @ActionRegistration(displayName = "TestAction", checkedOn = @ActionState(property = "boolProp", listenOn = CustomListener.class))
    public static class ToggleCustomCallback implements ActionListener {
        public ToggleCustomCallback(ActionModel5 model) {
            instance2 = this;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    /**
     * Checks that the action framework reacts uses custom listener
     * @throws Exception 
     */
    public void testToggleActionEabledCustomIface() throws Exception {
        Action a = Actions.forID("Foo", "test.ToggleCustomCallback");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertFalse("Action must be disabled without data", a.isEnabled());
        assertNull("Must not eagerly instantiate", instance2);
        
        ActionModel5 mod = new ActionModel5();
        lookupContent.add(mod);

        assertNull("Must not be created on data presence", instance2);
        assertTrue("Must be enabled when data is ready", a.isEnabled());
        assertFalse("Must not be checked unless guard is true", (Boolean)a.getValue(Action.SELECTED_KEY));

        mod.boolProp = true;
        mod.fire();
        assertTrue("Must become checked after prop change", (Boolean)a.getValue(Action.SELECTED_KEY));
        
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        
        assertNotNull(received);
        assertEquals("cmd2", received.getActionCommand());
    }


    @ActionID(id = "test.ToggleAction2", category="Foo")
    @ActionRegistration(displayName = "TestAction", checkedOn = @ActionState(property = "boolProp"))
    public static class ToggleAction2 implements ActionListener {
        
        public ToggleAction2(ActionModel2 model) {
            instance2 = this;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    /**
     * Checks that the toggle action changes state according to the model
     * property. Checks usage of {@code addPropertyChange(prop, listener)}.
     * @throws Exception 
     */
    public void testToggleActionEabledStatePropChange2() throws Exception {
        Action a = Actions.forID("Foo", "test.ToggleAction2");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertFalse("Action must be disabled without data", a.isEnabled());
        assertNull("Must not eagerly instantiate", instance2);
        
        ActionModel2 mod = new ActionModel2();
        lookupContent.add(mod);

        assertNull("Must not be created on data presence", instance2);
        assertTrue("Must be enabled when data is ready", a.isEnabled());
        assertFalse("Must not be checked unless guard is true", (Boolean)a.getValue(Action.SELECTED_KEY));

        mod.boolProp = true;
        mod.supp.firePropertyChange("bool2Prop", null, null);
        assertFalse("Unrelated property change should be ignored", (Boolean)a.getValue(Action.SELECTED_KEY));

        mod.supp.firePropertyChange("boolProp", null, null);
        assertTrue("Must become checked after prop change", (Boolean)a.getValue(Action.SELECTED_KEY));
        
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        
        assertNotNull(received);
        assertEquals("cmd2", received.getActionCommand());
    }
    
    /**
     * Checks that toggle action is not instantiated prematurely
     */
    public void testToggleActionInstantiate() throws Exception {
        Action a = Actions.forID("Foo", "test.ToggleAction");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertNull("Must not eagerly instantiate", instance2);
        
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        assertNull("Must not instantiate just on data presence", instance2);

        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        assertNull("Must not instantiate just when guard goes true", instance2);
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        
        // instantiated
        assertNotNull(instance2);
    }
    
    @ActionID(id = "test.CustomToggleAction", category="Foo")
    @ActionRegistration(displayName = "TestAction", checkedOn = @ActionState(property = "boolProp", useActionInstance = true))
    public static class CustomToggleAction extends AbstractAction {
        ActionModel3 aModel;
        
        public CustomToggleAction(ActionModel3 model) {
            created++;
            instance = this;
            setEnabled(false);
            this.aModel = model;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled(); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    /**
     * Checks that the toggle action will be queried for its checked state
     */
    public void testToggleActionCustomState() throws Exception {
        Action a = Actions.forID("Foo", "test.CustomToggleAction");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertNull("Must not eagerly instantiate", instance);
        
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        assertNull("Must not instantiate just on data presence", instance);

        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        Action save = instance;
        assertNotNull("Must instantiate for check evaluation", instance);
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        
        // still same instance
        assertNotNull(instance);
        assertSame(save, instance);
    }
    
    /**
     * Checks that the action instance changes as the context object changes,
     * similar to the test for enabling action.
     */
    public void testCustomActionChangesWithContext() throws Exception {
        Action a = Actions.forID("Foo", "test.CustomToggleAction");
        assertFalse("Action must be unchecked", (Boolean)a.getValue(Action.SELECTED_KEY));
        assertNull("Must not eagerly instantiate", instance);
        
        ActionModel3 mod = new ActionModel3();
        ActionModel3 mod2 = new ActionModel3();
        
        lookupContent.add(mod);
        assertNull("Must not instantiate just on data presence", instance);

        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);

        assertNotNull("Must instantiate for check evaluation", instance);
        assertTrue(a.isEnabled());
        
        CustomToggleAction saveTA = (CustomToggleAction)instance;
        assertSame(mod, saveTA.aModel);
        instance = null;
        lookupContent.remove(mod);
        lookupContent.add(mod2);
        // not created, the guard condition is not true yet
        assertNull(instance);

        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        // still not created, fired on old model
        assertNull(instance);
        
        mod2.boolProp = true;
        mod2.supp.firePropertyChange("boolProp", null, null);
        // now guard becomes true, action must be created
        assertNotNull(instance);
        assertNotSame(saveTA, instance);

        CustomToggleAction nowTA = (CustomToggleAction)instance;
        assertSame(mod2, nowTA.aModel);
    }
    
    /**
     * Checks how Action.isEnabled() tracks changes in the context and
     * property changes of the context objects
     */
    public void testContextActionEnableChanges() throws Exception {
        InstanceContent localContent1 = new InstanceContent();
        AbstractLookup localLookup1 = new AbstractLookup(localContent1);
        InstanceContent localContent2 = new InstanceContent();
        AbstractLookup localLookup2 = new AbstractLookup(localContent2);
        
        ActionModel3 mdlGlobal = new ActionModel3();
        ActionModel3 mdlGlobal2 = new ActionModel3();
        lookupContent.add(mdlGlobal);
        
        Action a = Actions.forID("Foo", "test.InstAction");
        assertFalse("Must be disabled before guard is set", a.isEnabled());
        
        mdlGlobal.boolProp = true;
        mdlGlobal.supp.firePropertyChange("boolProp", null, null);
        assertTrue("Must turn enabled after guard change", a.isEnabled());
        
        // adopt into local context
        localContent1.add(mdlGlobal);
        Action localA = ((ContextAwareAction)a).createContextAwareInstance(localLookup1);
        assertTrue("Context action enable must initialize", localA.isEnabled());
        
        // turn to false
        mdlGlobal.boolProp = false;
        mdlGlobal.supp.firePropertyChange("boolProp", null, null);
        assertFalse("Global action must follow guard", a.isEnabled());
        assertFalse("Context action must follow guard", localA.isEnabled());
        
        mdlGlobal.boolProp = true;
        mdlGlobal.supp.firePropertyChange("boolProp", null, null);
        assertTrue(a.isEnabled());
        assertTrue(localA.isEnabled());

        // remove/replace the model in global Lookup
        lookupContent.remove(mdlGlobal);
        assertFalse("Global action must follow its Lookup", a.isEnabled());
        assertTrue("Context action must listen on its Lookup", localA.isEnabled());
        
        lookupContent.add(mdlGlobal2);
        assertFalse(a.isEnabled());
        assertTrue(localA.isEnabled());
        
        mdlGlobal2.boolProp = true;
        mdlGlobal2.supp.firePropertyChange("boolProp", null, null);
        
        assertTrue("Global action must enbale on new global guard", a.isEnabled());
        assertTrue(localA.isEnabled());
        
        mdlGlobal.boolProp = false;
        mdlGlobal.supp.firePropertyChange("boolProp", null, null);
        assertFalse("Context action must follow remembered guard", localA.isEnabled());
        
        ActionModel3 mdl3 = new ActionModel3();
        localContent2.add(mdl3);
        
        Action localB = ((ContextAwareAction)a).createContextAwareInstance(localLookup2);
        assertFalse(localB.isEnabled());
        
        mdl3.boolProp = true;
        mdl3.supp.firePropertyChange("boolProp", null, null);
        assertTrue(localB.isEnabled());
        assertFalse(localA.isEnabled());
    }

    /**
     * Checks that the state object that goes out of lookup is not strong-held
     * by action system
     */
    public void testStateObjectWillGC() throws Exception {
        Action a = Actions.forID("Foo", "test.CustomToggleAction");
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        assertNull("Must not instantiate just on data presence", instance);

        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        assertNotNull(instance);
        assertTrue(a.isEnabled());

        lookupContent.remove(mod);
        
        mod = null;
        instance = null;

        Reference<Object> r = new WeakReference<>(mod);
        assertGC("Obsolete model object must GC", r);
    }
    
    /**
     * Checks that an obsolete custom action instance is released when
     * its context object goes away form Lookup and the action instance
     * can be GCed.
     */
    public void testOldContextActionWillGC() throws Exception {
        Action a = Actions.forID("Foo", "test.CustomToggleAction");
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        assertNull("Must not instantiate just on data presence", instance);

        mod.boolProp = true;
        mod.supp.firePropertyChange("boolProp", null, null);
        assertNotNull(instance);
        assertTrue(a.isEnabled());

        lookupContent.remove(mod);
        Reference<Object> r = new WeakReference<>(instance);
        instance = null;
        mod = null;

        assertGC("Obsolete model object must GC", r);
    }
    
    @ActionID(id = "test.CustomEnableAction2", category="Foo")
    @ActionRegistration(displayName = "TestAction", enabledOn = @ActionState(useActionInstance = true))
    public static class CustomEnableAction2 extends AbstractAction {
        final ActionModel3 model;

        public CustomEnableAction2(ActionModel3 model) {
            this.model = model;
            setEnabled(false);
            instance = this;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            instance = this;
        }
    }

    /**
     * Checks that custom action isEnable is called when no property is specified
     */
    public void testCustomEnableActionNoPropety() {
        Action a = Actions.forID("Foo", "test.CustomEnableAction2");
        assertFalse(a.isEnabled());
        
        ActionModel3 mod = new ActionModel3();
        lookupContent.add(mod);
        assertNotNull(instance);
        
        CustomEnableAction2 save = (CustomEnableAction2)instance;
        instance.setEnabled(true);
        assertTrue(a.isEnabled());
        
        a.actionPerformed(new ActionEvent(this, 0, "cmd2"));
        assertSame(save, instance);
    }
    
    @ActionID(id = "test.ListAction", category="Foo")
    @ActionRegistration(displayName = "TestAction", checkedOn = @ActionState(
            property = "minSelectionIndex", listenOn = ListSelectionListener.class, listenOnMethod="valueChanged"
    ))
    public static class ListAction implements ActionListener {
        public ListAction(ListSelectionModel model) {
            instance2 = this;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            received = e;
        }
    }
    
    public void testCustomContextAwareInstance() {
        Action a = Actions.forID("Foo", "test.ListAction");
        DefaultListSelectionModel model = new DefaultListSelectionModel();
        
        InstanceContent localContent1 = new InstanceContent();
        AbstractLookup localLookup1 = new AbstractLookup(localContent1);
        
        Action la = ((ContextAwareAction)a).createContextAwareInstance(localLookup1);
        
        assertFalse(a.isEnabled());
        assertFalse(la.isEnabled());
        
        localContent1.add(model);
        
        assertFalse(a.isEnabled());
        assertTrue(la.isEnabled());
        assertFalse((Boolean)la.getValue(Action.SELECTED_KEY));
        
        // checks that the context-bound instance changes its selected state
        // if the model changes (relevant property change event is fired)
        model.setSelectionInterval(1, 2);
        assertTrue((Boolean)la.getValue(Action.SELECTED_KEY));
    }
    
    public void testCustomListenerAction() {
        Action a = Actions.forID("Foo", "test.ListAction");
        DefaultListSelectionModel model = new DefaultListSelectionModel();
        
        assertFalse(a.isEnabled());
        assertFalse((Boolean)a.getValue(Action.SELECTED_KEY));
        
        lookupContent.add(model);
        assertTrue(a.isEnabled());
        assertFalse((Boolean)a.getValue(Action.SELECTED_KEY));
        
        model.addSelectionInterval(1, 1);
        assertTrue(a.isEnabled());
        assertTrue((Boolean)a.getValue(Action.SELECTED_KEY));
    }
}
