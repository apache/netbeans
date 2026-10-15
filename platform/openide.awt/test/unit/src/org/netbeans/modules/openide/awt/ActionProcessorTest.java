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

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import java.io.IOException;
import org.openide.util.test.AnnotationProcessorTestUtils;
import java.util.Collections;
import java.util.List;
import org.openide.awt.ActionID;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JSeparator;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ActionProcessorTest extends NbTestCase {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public ActionProcessorTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testHeadlessCompilationWorks() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A",
                  "import org.openide.awt.ActionRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "import org.openide.awt.ActionReference;\n"
                + "import java.awt.event.*;\n"
                + "@ActionID(category=\"Tools\",id=\"my.action\")"
                + "@ActionRegistration(displayName=\"AAA\") "
                + "@ActionReference(path=\"Shortcuts\", name=\"C-F2 D-A\")"
                + "public class A implements ActionListener {\n"
                + "    public void actionPerformed(ActionEvent e) {}"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation has to succeed:\n" + os, r);
    }

    @ActionRegistration(
        displayName="#AlwaysOn"
    )
    @ActionID(
        id="my.test.Always", category="Tools"
    )
    @ActionReference(path="My/Folder", position=333, name="D-F6")        
    public static final class Always implements ActionListener {
        static int created;

        public Always() {
            created++;
        }

        static int cnt;
        @Override
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }
    
    @ActionReferences({
        @ActionReference(
            path="Loaders/text/x-my/Actions", 
            id=@ActionID(category="System", id="org.openide.actions.OpenAction"),
            position=100, 
            separatorAfter=200
        )
    })
    public void testAlwaysEnabledAction() throws Exception {
        assertEquals("Not created yet", 0, Always.created);
        Action a = Actions.forID("Tools", "my.test.Always");
        assertNotNull("action found", a);
        assertEquals("Still not created", 0, Always.created);

        assertEquals("I am always on!", a.getValue(Action.NAME));
        assertEquals("Not even now created", 0, Always.created);
        a.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Created now!", 1, Always.created);

        assertEquals("Action called", 300, Always.cnt);
        
        FileObject shad = FileUtil.getConfigFile(
            "My/Folder/D-F6.shadow"
        );
        assertNotNull("Shadow created", shad);
        assertEquals("Right position", 333, shad.getAttribute("position"));
        assertEquals("Proper link", "Actions/Tools/my-test-Always.instance", shad.getAttribute("originalFile"));
    }
    
    public void testVerifyReferencesInstalledViaPackageInfo() {
        FileObject one = FileUtil.getConfigFile("pkg/one/action-one.shadow");
        assertNotNull("Found", one);
        assertEquals("Actions/Fool/action-one.instance", one.getAttribute("originalFile"));
        
        FileObject two = FileUtil.getConfigFile("pkg/two/action-two.shadow");
        assertNotNull("Found", two);
        assertEquals("Actions/Pool/action-two.instance", two.getAttribute("originalFile"));
    }

    public static final class AlwaysByMethod {
        private AlwaysByMethod() {}
        static int created, cnt;
        @ActionRegistration(displayName="#AlwaysOn")
        @ActionID(id="my.test.AlwaysByMethod", category="Tools")
        @ActionReferences({
            @ActionReference(path="Kuk/buk", position=1, separatorAfter=2),
            @ActionReference(path="Muk/luk", position=11, separatorBefore=10)
        })
        public static ActionListener factory() {
            created++;
            return new ActionListener() {
                public @Override void actionPerformed(ActionEvent e) {
                    cnt += e.getID();
                }
            };
        }
    }

    public void testAlwaysEnabledActionByMethod() throws Exception {
        assertEquals("Not created yet", 0, AlwaysByMethod.created);
        Action a = Actions.forID("Tools", "my.test.AlwaysByMethod");
        assertNotNull(a);
        assertEquals("Still not created", 0, AlwaysByMethod.created);
        assertEquals("I am always on!", a.getValue(Action.NAME));
        assertEquals("Not even now created", 0, AlwaysByMethod.created);
        a.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Created now!", 1, AlwaysByMethod.created);
        assertEquals("Action called", 300, AlwaysByMethod.cnt);

        {
            FileObject shad = FileUtil.getConfigFile(
                "Kuk/buk/my-test-AlwaysByMethod.shadow"
            );
            assertNotNull("Shadow created", shad);
            assertEquals("Right position", 1, shad.getAttribute("position"));
            assertEquals("Proper link", "Actions/Tools/my-test-AlwaysByMethod.instance", shad.getAttribute("originalFile"));
            FileObject sep = FileUtil.getConfigFile(
                "Kuk/buk/my-test-AlwaysByMethod-separatorAfter.instance"
            );
            assertNotNull("Separator generated", sep);
            assertEquals("Position 2", 2, sep.getAttribute("position"));
            Object instSep = sep.getAttribute("instanceCreate");
            assertTrue("Right instance " + instSep, instSep instanceof JSeparator);
        }
        {
            FileObject shad = FileUtil.getConfigFile(
                "Muk/luk/my-test-AlwaysByMethod.shadow"
            );
            assertNotNull("Shadow created", shad);
            assertEquals("Right position", 11, shad.getAttribute("position"));
            assertEquals("Proper link", "Actions/Tools/my-test-AlwaysByMethod.instance", shad.getAttribute("originalFile"));
            FileObject sep = FileUtil.getConfigFile(
                "Muk/luk/my-test-AlwaysByMethod-separatorBefore.instance"
            );
            assertNotNull("Separator generated", sep);
            assertEquals("Position ten", 10, sep.getAttribute("position"));
            Object instSep = sep.getAttribute("instanceCreate");
            assertTrue("Right instance " + instSep, instSep instanceof JSeparator);
        }
        
    }

    @ActionRegistration(
        displayName="#Key",
        key="klic"
    )
    @ActionID(
        category="Tools",
        id = "my.action"
    )
    public static final class Callback implements ActionListener {
        static int cnt;
        @Override
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testCallbackAction() throws Exception {
        Callback.cnt = 0;
        ContextAwareAction a = (ContextAwareAction) Actions.forID("Tools", "my.action");

        class MyAction extends AbstractAction {
            int cnt;
            @Override
            public void actionPerformed(ActionEvent e) {
                cnt += e.getID();
            }
        }
        MyAction my = new MyAction();
        ActionMap m = new ActionMap();
        m.put("klic", my);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(m);

        assertEquals("I am context", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Local Action called", 300, my.cnt);
        assertEquals("Global Action not called", 0, Callback.cnt);

        ic.remove(m);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Local Action stays", 300, my.cnt);
        assertEquals("Global Action ncalled", 200, Callback.cnt);
    }
    
    
    @ActionRegistration(
        displayName = "#Key",
        iconBase="org/openide/awt/TestIcon.png"
    )
    @ActionID(
        category = "Edit",
        id = "my.field.action"
    )
    public static final String ACTION_MAP_KEY = "my.action.map.key";
    
    public void testCallbackOnFieldAction() throws Exception {
        Callback.cnt = 0;
        
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Edit/my-field-action.instance"
        );
        assertNotNull("File found", fo);
        Object icon = fo.getAttribute("iconBase");
        assertEquals("Icon found", "org/openide/awt/TestIcon.png", icon);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        class MyAction extends AbstractAction {
            int cnt;
            @Override
            public void actionPerformed(ActionEvent e) {
                cnt += e.getID();
            }
        }
        MyAction my = new MyAction();
        ActionMap m = new ActionMap();
        m.put(ACTION_MAP_KEY, my);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(m);

        assertEquals("I am context", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Local Action called", 300, my.cnt);
        assertEquals("Global Action not called", 0, Callback.cnt);

        ic.remove(m);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Local Action stays", 300, my.cnt);
        assertEquals("Global Action not called, there is no fallback", 0, Callback.cnt);
    }

    public static final class NumberLike {
        final int x;
        NumberLike(int x) {
            this.x = x;
        }
    }

    @ActionID(category = "Tools", id = "on.int")
    @ActionRegistration(displayName = "#OnInt")
    public static final class Context implements ActionListener {
        private final int context;
        
        public Context(NumberLike context) {
            this.context = context.x;
        }

        static int cnt;

        @Override
        public void actionPerformed(ActionEvent e) {
            cnt += context;
        }

    }

    public void testContextAction() throws Exception {
        Action a = Actions.forID("Tools", "on.int");
        assertTrue("It is context aware action", a instanceof ContextAwareAction);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = ((ContextAwareAction) a).createContextAwareInstance(lkp);
        NumberLike ten = new NumberLike(10);
        ic.add(ten);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 10, Context.cnt);

        ic.remove(ten);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Global Action stays same", 10, Context.cnt);
    }

    @ActionRegistration(
        displayName="#OnInt"
    )
    @ActionID(
        category="Tools",
        id="on.numbers"
    )
    public static final class MultiContext implements ActionListener {
        private final List<NumberLike> context;

        public MultiContext(List<NumberLike> context) {
            this.context = context;
        }

        static int cnt;

        @Override
        public void actionPerformed(ActionEvent e) {
            for (NumberLike n : context) {
                cnt += n.x;
            }
        }

    }

    public void testMultiContextAction() throws Exception {
        ContextAwareAction a = (ContextAwareAction) Actions.forID("Tools", "on.numbers");

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        NumberLike ten = new NumberLike(10);
        NumberLike three = new NumberLike(3);
        ic.add(ten);
        ic.add(three);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 13, MultiContext.cnt);

        ic.remove(ten);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Adds 3", 16, MultiContext.cnt);

        ic.remove(three);
        assertFalse("It is disabled", clone.isEnabled());
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("No change", 16, MultiContext.cnt);
    }
    
    @ActionRegistration(displayName="somename", surviveFocusChange=true)
    @ActionID(category="Windows", id="my.survival.action")
    public static final String SURVIVE_KEY = "somekey";

    public void testSurviveFocusChangeBehavior() throws Exception {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            @Override
            public boolean isEnabled() {
                cntEnabled++;
                return true;
            }
            
            @Override
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        MyAction myAction = new MyAction();
        
        ActionMap disable = new ActionMap();
        ActionMap enable = new ActionMap();
        
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        
        ContextAwareAction temp = (ContextAwareAction) Actions.forID("Windows", "my.survival.action");
        Action a = temp.createContextAwareInstance(al);
        
        enable.put(SURVIVE_KEY, myAction);
        
        ic.add(enable);
        assertTrue("MyAction is enabled", a.isEnabled());
        ic.set(Collections.singletonList(disable), null);
        assertTrue("Remains enabled on other component", a.isEnabled());
        ic.remove(disable);
    }

    public void testSubclass() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\") " +
            "public class A {\n" +
            "  public A(Integer i) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("ActionListener")) {
            fail(os.toString());
        }
    }
    
    public void testNoConstructorIsFine() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\") " +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation has to succeed:\n" + os, r);
    }

    @ActionID(category="eager", id="direct.one")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct extends AbstractAction implements Presenter.Menu {
        static int cnt;
        public Direct() {
            cnt++;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getMenuPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    @ActionID(category="eager", id="direct.two")
    @ActionRegistration(displayName="Direct Action")
    @ActionReference(path="Shortcuts", name="C-F2 D-A")
    public static class Direct2 extends AbstractAction implements Presenter.Toolbar {
        static int cnt;
        public Direct2() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public Component getToolbarPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    @ActionID(category="eager", id="direct.three")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct3 extends AbstractAction implements Presenter.Popup {
        static int cnt;
        public Direct3() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getPopupPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    @ActionID(category="eager", id="direct.four")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct4 extends AbstractAction implements ContextAwareAction {
        static int cnt;
        public Direct4() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
    @ActionID(category="eager", id="direct.five")
    @ActionRegistration(displayName="Direct Action")
    public static ContextAwareAction direct5() {return new Direct5();}
    private static class Direct5 extends AbstractAction implements ContextAwareAction {
        static int cnt;
        public Direct5() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
    @ActionID(category="eager", id="direct.six")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct6 extends AbstractAction implements DynamicMenuContent {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public JComponent[] getMenuPresenters() {
            return null;
        }
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return null;
        }
    }
    @ActionID(category="eager", id="direct.seven")
    @ActionRegistration(displayName="Direct Action", lazy=false)
    public static class Direct7 extends AbstractAction {
        @Override public void actionPerformed(ActionEvent e) {}
    }
    @ActionID(category="eager", id="direct.eight")
    @ActionRegistration(displayName="Direct Action", lazy=true)
    public static class Direct8 extends AbstractAction implements ContextAwareAction {
        @Override public void actionPerformed(ActionEvent e) {}
        @Override public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
    
    @ActionID(category="menutext", id="namedaction")
    @ActionRegistration(displayName="This is an Action", menuText="This is a Menu Action", popupText="This is a Popup Action")
    public static class NamedAction extends AbstractAction {
        public NamedAction() { }
        @Override
        public void actionPerformed(ActionEvent e) { }
    }

    public void testPopupAndMenuText() throws Exception {
        Action a = Actions.forID("menutext", "namedaction");
        assertEquals("This is an Action", a.getValue(Action.NAME));
        JMenuItem item = new JMenuItem();
        Actions.connect(item, a, false);
        assertEquals("This is a Menu Action", item.getText());
        item = new JMenuItem();
        Actions.connect(item, a, true);
        assertEquals("This is a Popup Action", item.getText());
    }
    
    public void testDirectInstanceIfImplementsMenuPresenter() throws Exception {
        assertEquals("Direct class is created", Direct.class, Actions.forID("eager", "direct.one").getClass());
    }
    public void testDirectInstanceIfImplementsToolbarPresenter() throws Exception {
        assertEquals("Direct class is created", Direct2.class, Actions.forID("eager", "direct.two").getClass());
    }
    public void testDirectInstanceIfImplementsPopupPresenter() throws Exception {
        assertEquals("Direct class is created", Direct3.class, Actions.forID("eager", "direct.three").getClass());
    }
    public void testDirectInstanceIfImplementsContextAwareAction() throws Exception {
        assertEquals("Direct class is created", Direct4.class, Actions.forID("eager", "direct.four").getClass());
    }
    public void testDirectInstanceIfImplementsContextAwareActionByMethod() throws Exception {
        assertEquals("Direct class is created", Direct5.class, Actions.forID("eager", "direct.five").getClass());
    }
    public void testDirectInstanceIfImplementsDynamicMenuContent() throws Exception {
        assertEquals("Direct class is created", Direct6.class, Actions.forID("eager", "direct.six").getClass());
    }
    public void testDirectInstanceIfRequested() throws Exception {
        assertEquals("Direct class is created", Direct7.class, Actions.forID("eager", "direct.seven").getClass());
    }
    public void testIndirectInstanceIfRequested() throws Exception {
        assertNotSame("Direct class is not created", Direct8.class, Actions.forID("eager", "direct.eight").getClass());
    }
    
    public void testNoKeyForDirects() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "public class A extends AbstractAction implements Presenter.Menu {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    public void testListWithNoType() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "public class A extends AbstractAction {\n" +
            "    public A(List wrongCnt) {}\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testArray() throws Exception {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A",
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "public class A implements ActionListener {\n" +
            "    public A(Integer[] params) {}\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os));
        assertTrue("correct message:\n" + os, os.toString().contains("Integer[]"));
    }
    
    public void testNoActionIDInReferences() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"manka\", position=11, id=@ActionID(category=\"Cat\",id=\"x.y.z\"))" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testPackageInfoNeedsActionID() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.package-info", 
            "@ActionReferences({\n" +
            "  @ActionReference(path=\"manka\", position=11)\n" +
            "})\n" +
            "package test;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionReferences;\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testNoReferenceWithoutRegistration() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.ActionReferences;\n" +
            "import java.awt.event.*;\n" +
            "@ActionReference(path=\"manka\", position=11, id=@ActionID(category=\"Cat\",id=\"x.y.z\"))" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testNoReferencesWithoutRegistrationExceptOnPackage() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.ActionReferences;\n" +
            "import java.awt.event.*;\n" +
            "@ActionReferences({\n" +
            "  @ActionReference(path=\"manka\", position=11)" +
            "})\n" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    
    public void testCheckSyntaxInShortcutsNoName() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.*;\n" +
            "import java.awt.event.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"Shortcuts\")" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        assertTrue("Contains hint", os.toString().contains("Utilities.stringToKey"));
    }

    public void testCheckSyntaxInShortcuts() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.*;\n" +
            "import java.awt.event.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"Shortcuts\", name=\"silly\")" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        assertTrue("Contains hint", os.toString().contains("Utilities.stringToKey"));
    }
    
    public void testReferenceWithoutPosition() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Shortcuts/C-F2 D-A.shadow");
        assertNotNull(fo);
        assertEquals("Actions/eager/direct-two.instance", fo.getAttribute("originalFile"));
        assertEquals(null, fo.getAttribute("position"));
    }

    public void testSeparatorBeforeIsBefore() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"manka\", position=11, separatorBefore=13)" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    public void testSeparatorAfterIsAfter() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"manka\", position=11, separatorAfter=7)" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testWrongPointerToIcon() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\", iconBase=\"does/not/exist.png\") " +
            "@ActionReference(path=\"manka\", position=11)" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("does/not/exist.png")) {
            fail("Shall contain warning about does/not/exist.png resource:\n" + os);
        }
    }

    public void testErrorOnNonStaticInnerclasses() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "public class A {\n" +
            "    @ActionID(category=\"Tools\",id=\"my.action\")" +
            "    @ActionRegistration(displayName=\"AAA\", key=\"K\", iconBase=\"does/not/exist.png\") " +
            "    @ActionReference(path=\"manka\", position=11)" +
            "    public class B implements ActionListener {\n" +
            "      public void actionPerformed(ActionEvent e) {}\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("has to be static")) {
            fail("B has to be static:\n" + os);
        }
    }

}
