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
package org.netbeans.test.editor.suites.keybindings;

import java.awt.event.KeyEvent;
import java.util.Vector;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.editor.KeyMapOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.editor.lib.EditorTestCase;
import org.netbeans.test.editor.lib.EditorTestCase.ValueResolver;

/**
 *
 * @author Petr Dvorak Petr.Dvorak@Sun.COM
 */
public class KeyMapTest extends EditorTestCase {
    public static final String PROFILE_DEFAULT = "NetBeans";

    public static final String SRC_PACKAGES_PATH = "src";
    private static String PROJECT_NAME;
    private static EditorOperator editor;

    /** Creates a new instance of KeyMapTest
     * @param name Test name
     */
    public KeyMapTest(String name) {
        super(name);        
    }

    public void closeProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        rootNode.performPopupActionNoBlock("Delete");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        //JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        //cb.setSelected(true);
        ndo.yes();
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("----");
        System.out.println("Starting: " + getName());
        System.out.println("----");
	  openDefaultProject();
        openSourceFile("keymap", "Main.java");
        editor = new EditorOperator("Main.java");
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("----");
        System.out.println("Finished: " + getName());
        System.out.println("----");
    }

    public void testVerify() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.verify();
            kmo.ok().push();
            closed = true;
        } catch (Exception e) {
            System.out.println("ERROR: testVerify");
            e.printStackTrace(System.out);
            fail(e);
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testAddShortcut() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_G);
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
            checkListContents(shortcuts, "Ctrl+Shift+G");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            editor.setCaretPosition(55, 1);
            ValueResolver vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
        } catch (Exception e) {
            System.out.println("ERROR: testAddShortcut");
            e.printStackTrace(System.out);
            fail(e);
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testAssignAlternativeShortcut() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            // invoke keymap operator and mark it is open
            kmo = KeyMapOperator.invoke();
            closed = false;
            // select netbeans  profile
            kmo.selectProfile(PROFILE_DEFAULT);
            // assign one normal and one alternative shortcut to the "select line" action
            kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_G);
            kmo.assignAlternativeShortcutToAction("select line", true, false, true, KeyEvent.VK_M);
            // retrieve all assigned shortcuts and compare it to expected list of shortcuts
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
            checkListContents(shortcuts, "ctrl+shift+g", "ctrl+alt+m");
            // confirm Options dialog, press OK and mark that OD was closed
            kmo.ok().push();
            closed = true;
            // Wait + focus the editor
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            // Check Ctrl+Alt+M works for select line
            editor.setCaretPosition(55, 1);
            ValueResolver vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
            // Check Ctrl+Shift+G works for select line
            editor.setCaretPosition(55, 1);
            vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
        } catch (Exception e) {
            System.out.println("ERROR: testAssignAlternativeShortcut");
            e.printStackTrace(System.out);
            fail(e);
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testUnassign() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            for (int i = 0; i < 2; i++) {
                // invoke keymap operator and mark it is open
                kmo = KeyMapOperator.invoke();
                closed = false;
                // select netbeans  profile
                kmo.selectProfile(PROFILE_DEFAULT);
                // assign one normal shortcut to the "select line" action
                kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_G);
                // retrieve all assigned shortcuts and compare it to expected list of shortcuts
                Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
                checkListContents(shortcuts, "ctrl+shift+g");
                kmo.ok().push();
                closed = true;
                new EventTool().waitNoEvent(2000);
                editor.requestFocus();
                new EventTool().waitNoEvent(100);
                // Check Ctrl+Shift+G works for select line
                editor.setCaretPosition(55, 1);
                ValueResolver vr = new ValueResolver() {

                    @Override
                    public Object getValue() {
                        editor.pushKey(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                        String selected = editor.txtEditorPane().getSelectedText();
                        new EventTool().waitNoEvent(100);
                        if (selected == null) {
                            return false;
                        }
                        return selected.startsWith("public class Main {");
                    }
                };
                waitMaxMilisForValue(3000, vr, Boolean.TRUE);
                String text = editor.txtEditorPane().getSelectedText();
                assertEquals("public class Main {", text.trim());
                kmo = KeyMapOperator.invoke();
                closed = false;
                kmo.unassignAlternativeShortcutToAction("select line", "ctrl+shift+g");
                kmo.ok().push();
                closed = true;
                new EventTool().waitNoEvent(2000);
                editor.requestFocus();
                new EventTool().waitNoEvent(100);
                // Check Ctrl+Alt+M works for select line
                editor.setCaretPosition(55, 2);
                sleep(200);
                editor.setCaretPosition(55, 1);
                waitMaxMilisForValue(3000, vr, Boolean.TRUE);
                text = editor.txtEditorPane().getSelectedText();
                if (text == null) {
                    text = "";
                }
                assertNotSame("public class Main {", text.trim());
            }
        } catch (Exception e) {
            System.out.println("ERROR: testUnassign");            
            e.printStackTrace(System.out);
            fail(e);
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testAddDuplicateCancel() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
            kmo.assignShortcutToAction("select line", false, true, true, KeyEvent.VK_F9, true, false);
            shortcuts.equals(kmo.getAllShortcutsForAction("select line"));
            kmo.ok().push();
            closed = true;
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.assignShortcutToAction("select line", false, true, true, KeyEvent.VK_F9, true, true);
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            // Check ALT+Shift+G works for select line
            editor.setCaretPosition(55, 1);
            ValueResolver vr = new ValueResolver() {

                @Override
                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_F9, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
        } catch (Exception e) {
            System.out.println("ERROR: testAddDuplicateCancel");
            e.printStackTrace(System.out);
            fail(e);
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testCancelAdding() {
    }

    public void testCancelOptions() {
    }

    public void testHelp() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.help().push();
            final HelpOperator help = new HelpOperator();
            ValueResolver vr = new ValueResolver() {

                @Override
                public Object getValue() {
                    return help.getContentText().contains("Options Window: Keymap");
                }
            };
            waitMaxMilisForValue(5000, vr, Boolean.TRUE);
            boolean ok = help.getContentText().contains("Options Window: Keymap");
            if (!ok) {
                log(help.getContentText());
            }
            assertTrue("Wrong help page opened", ok);
            help.requestClose();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
            }
        }
    }

    public void testProfileSwitch() {
    }

    public void testProfileDuplicte() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        String prFrom = PROFILE_DEFAULT;
        String prTo = "NetBeans New";
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.assignShortcutToAction("select line", true, false, true, KeyEvent.VK_M);
            kmo.duplicateProfile(prFrom, "NetBeans New");
            kmo.selectProfile("NetBeans New");
            if (!kmo.getAllShortcutsForAction("select line").contains("ctrl+alt+m")) {
                fail("Profile cloning failed: " + prFrom + " -> " + prTo);
            }
            kmo.checkProfilesPresent("Eclipse", "Emacs", "NetBeans", "NetBeans New", "NetBeans 5.5");
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.ok().push();
            closed = true;
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
            }
        }
    }

    public void testProfileRestore() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("Preview Design");
            kmo.assignShortcutToAction("Preview Design", true, true, false, KeyEvent.VK_W, true, true);
            if (shortcuts.equals(kmo.getAllShortcutsForAction("Preview Design"))) {
                fail("Problem with assigning shortcut to Preview Design");
            }
            kmo.ok().push();
            closed = true;
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.actionSearchByName().setText("Preview Design");
            kmo.restoreProfile("NetBeans");
            Vector<String> sc = kmo.getAllShortcutsForAction("Preview Design");
            if (!shortcuts.equals(sc)) {
                // This test currently fails: http://www.netbeans.org/issues/show_bug.cgi?id=151254
                fail("Problem with restoring NetBeans profile (http://www.netbeans.org/issues/show_bug.cgi?id=151254) - \"Preview Design\" action: " + shortcuts.toString() + " vs. " + sc.toString());
            }
            kmo.ok().push();
            closed = true;
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
            }
        }
    }

    protected boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue) {
        int time = maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null) {
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time = 0;
            }
            time--;
        }
        return false;
    }

    protected void sleep(int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (Throwable t) {
            // Thread.sleep() failed for some reason
        }
    }

    private void checkListContents(Vector<String> scList, String... expList) {
        assertEquals("List does not contains expected number of items", expList.length, scList.size());
        for (int i = 0; i < scList.size(); i++) {
            scList.set(i, scList.get(i).toLowerCase());
        }
        for (String string : expList) {
            assertTrue(scList.contains(string.toLowerCase()));
        }
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(KeyMapTest.class)
                    //.addTest("prepareFileInEditor")
                    .addTest("testVerify")
                    .addTest("testAddDuplicateCancel")
                    .addTest("testAddShortcut")
                    .addTest("testUnassign")
                    .addTest("testAssignAlternativeShortcut")
                    //.addTest("testProfileRestore")//fails due to issue 151254
                    .addTest("testProfileDuplicte")
                    .addTest("testHelp")
                    //.addTest("closeProject")
                .enableModules(".*").clusters(".*"));
    }
}
