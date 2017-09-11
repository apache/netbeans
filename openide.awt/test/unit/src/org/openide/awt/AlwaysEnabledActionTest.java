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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import junit.framework.TestCase;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach
 */
public class AlwaysEnabledActionTest extends NbTestCase implements PropertyChangeListener {
    private FileObject folder;
    private int changeCounter;
    
    public AlwaysEnabledActionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        NbBundle.setBranding("big");
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);

        myIconResourceCounter = 0;
        myListenerCalled = 0;
        myListenerCounter = 0;
        MyAction.last = null;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testMemoryLeak() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        Action singleton = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                count.incrementAndGet();
            }
        };
        Object heavy = new Object();
        AlwaysEnabledAction always = AlwaysEnabledAction.create(
            Collections.singletonMap("delegate", singleton)
        );
        Action clone = always.createContextAwareInstance(Lookups.singleton(heavy));
        clone.actionPerformed(null);
        assertEquals(1, count.get());
        Reference<?> r = new WeakReference<Object>(heavy);
        clone = null;
        heavy = null;
        assertGC("should not leak context", r, Collections.singleton(singleton));        
    }

    public void testIconIsCorrect() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testIconIsCorrect.instance");
        
        assertNotNull("Action created", a);
        assertEquals("No myListener called", 0, myListenerCounter);
        assertEquals("No myIconURL called", 0, myIconResourceCounter);
        
        Object name = a.getValue(a.NAME);
        Object mnem = a.getValue(a.MNEMONIC_KEY);
        Object smallIcon = a.getValue(a.SMALL_ICON);
        if (smallIcon instanceof Icon) {
            Icon icon = (Icon) smallIcon;
            assertEquals("Icon height", 32, icon.getIconHeight());
            assertEquals("Icon widht", 32, icon.getIconWidth());
        } else {
            fail("Icon shall be Icon: " + smallIcon);
        }
            
        assertEquals("Right localized name", "Icon &Name Action", name);
        assertEquals("Mnemonic is N", (int)'N', mnem);
        assertNotNull("small icon present", smallIcon);

        assertEquals("once icon called", 1, myIconResourceCounter);

        
        Object base = a.getValue("iconBase"); 
        assertEquals("iconBase attribute is delegated", 2, myIconResourceCounter);
     
        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());

        a.actionPerformed(new ActionEvent(this, 0, "kuk"));

        assertEquals("Listener invoked", 1, myListenerCounter);
        
        
        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));

        assertContextAware(a);
    }

    public void testURLIconIsCorrect() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testURLIcon.instance");

        assertNotNull("Action created", a);
        Object smallIcon = a.getValue(a.SMALL_ICON);

        if (smallIcon instanceof Icon) {
            return;
        }
        fail("Icon shall be instance of Icon: " + smallIcon);
    }

    public void testNoIconIsOK() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testNoIcon.instance");

        CharSequence log = Log.enable("org.openide.awt", Level.WARNING);

        assertNotNull("Action created", a);
        Object smallIcon = a.getValue(a.SMALL_ICON);

        assertNull("No icon", smallIcon);
        assertEquals("No warnings:\n" + log, 0, log.length());
    }

    public void testIconTakenFromDelegate() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testIconDelegate.instance");

        assertNotNull("Action created", a);
        Object smallIcon = a.getValue(Action.SMALL_ICON);
        assertNotNull("Icon taken from iconBase", smallIcon);

        a.actionPerformed(new ActionEvent(this, 0, ""));

        Object newIcon = a.getValue(Action.SMALL_ICON);
        assertNotNull("Icon provided", newIcon);
        assertNull("No iconBase anymore", a.getValue("iconBase"));

        if (newIcon == smallIcon) {
            fail("Icons shall be different!");
        }

        assertSame("It is the one provided by action", newIcon, a.getValue("v"));
    }

    public static URL myURL() {
        return AlwaysEnabledActionTest.class.getResource("TestIcon.png");
    }

    private void assertContextAware(Action a) {
        assertTrue("We want context aware actions", a instanceof ContextAwareAction);
    }


    public void testDelegatesToPreviousInstanceWhenCreated() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testDelegate.instance");

        assertNotNull("Action created", a);
        assertEquals("No myListener called", 0, myListenerCounter);
        assertEquals("No myIconURL called", 0, myIconResourceCounter);

        Object name = a.getValue(a.NAME);
        Object mnem = a.getValue(a.MNEMONIC_KEY);
        Object smallIcon = a.getValue(a.SMALL_ICON);
        //Object icon = a.getValue(a.ICON)

        assertEquals("Right localized name", "Icon &Name Action", name);
        assertEquals("Mnemonic is N", (int) 'N', mnem);
        assertNotNull("small icon present", smallIcon);

        assertEquals("once icon called", 1, myIconResourceCounter);


        Object base = a.getValue("iconBase");
        assertEquals("iconBase attribute is delegated", 2, myIconResourceCounter);

        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());

        assertNull("No real action created yet", MyAction.last);
        a.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action not invoked as it is disabled", 0, myListenerCalled);
        assertNotNull("real action created", MyAction.last);
        a.addPropertyChangeListener(this);
        assertFalse("Disabled", a.isEnabled());
        MyAction.last.setEnabled(true);
        assertEquals("Change in a property delivered", 1, changeCounter);
        assertTrue("enabled now", a.isEnabled());
        a.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action invoked as no longer disabled", 1, myListenerCalled);

        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));

        assertEquals("Right localized name", "Icon &Name Action", a.getValue(Action.NAME));
        MyAction.last.putValue(MyAction.NAME, "Ahoj");
        assertEquals("Next Change in a property delivered", 2, changeCounter);
        assertEquals("Value taken from delegate", "Ahoj", a.getValue(Action.NAME));


        assertContextAware(a);
    }
    public void testContextAwareActionsShareDelegate() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testDelegate.instance");

        assertContextAware(a);
        assertNull("No real action created yet", MyAction.last);
        
        Action cca1 = ((ContextAwareAction)a).createContextAwareInstance(new AbstractLookup(new InstanceContent()));
        Action cca2 = ((ContextAwareAction)a).createContextAwareInstance(new AbstractLookup(new InstanceContent()));

        cca1.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action not invoked as it is disabled", 0, myListenerCalled);
        assertNotNull("real action created", MyAction.last);
        Action lastCca1 = MyAction.last;
        
        lastCca1.setEnabled(true);
        cca1.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action invoked as no longer disabled", 1, myListenerCalled);

        cca2.actionPerformed(new ActionEvent(this, 0, "kuk"));

        Action lastCca2 = MyAction.last;
        assertEquals("MyAction created just once", lastCca1, lastCca2);
        
        assertEquals("Action invoked as it remains enabled", 2, myListenerCalled);
    }

    public void testContextAwareDelegate() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testContextDelegate.instance");

        assertNotNull("Action created", a);
        assertEquals("No myListener called", 0, myListenerCounter);
        assertEquals("No myIconURL called", 0, myIconResourceCounter);

        Object name = a.getValue(a.NAME);
        Object mnem = a.getValue(a.MNEMONIC_KEY);
        Object smallIcon = a.getValue(a.SMALL_ICON);
        //Object icon = a.getValue(a.ICON)

        assertEquals("Right localized name", "Icon &Name Action", name);
        assertEquals("Mnemonic is N", (int) 'N', mnem);
        assertNotNull("small icon present", smallIcon);

        assertEquals("once icon called", 1, myIconResourceCounter);


        Object base = a.getValue("iconBase");
        assertEquals("iconBase attribute is delegated", 2, myIconResourceCounter);

        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());

        assertNull("No real action created yet", MyAction.last);

        InstanceContent ic = new InstanceContent();
        Lookup lkp = new AbstractLookup(ic);
        if (a instanceof ContextAwareAction) {
            a = ((ContextAwareAction)a).createContextAwareInstance(lkp);
        } else {
            fail("Should be context sensitive: " + a);
        }
        assertEquals("No clone created yet", 0, MyContextAction.clones);

        a.actionPerformed(new ActionEvent(this, 0, "kuk"));

        assertEquals("Clone created", 1, MyContextAction.clones);
        assertSame("Lookup used", lkp, MyContextAction.lkp);

        assertEquals("Action not invoked as it is disabled", 0, myListenerCalled);
        assertNotNull("real action created", MyAction.last);
        a.addPropertyChangeListener(this);
        assertFalse("Disabled", a.isEnabled());
        MyAction.last.setEnabled(true);
        assertEquals("Change in a property delivered", 1, changeCounter);
        assertTrue("enabled now", a.isEnabled());
        a.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action invoked as no longer disabled", 1, myListenerCalled);

        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));

        assertEquals("Right localized name", "Icon &Name Action", a.getValue(Action.NAME));
        MyAction.last.putValue(MyAction.NAME, "Ahoj");
        assertEquals("Next Change in a property delivered", 2, changeCounter);
        assertEquals("Value taken from delegate", "Ahoj", a.getValue(Action.NAME));

    }

    public void testEqualsNoStackOverflow() {
        ActionListener myAction = myAction();
        Action a1 = Actions.alwaysEnabled(myAction, "action1", null, true);
        Action a2 = Actions.alwaysEnabled(myAction(), "action2", null, true);
        // Tests fix of #155768 (no stack overflow)
        assertFalse("Actions not expected to be equal", a1.equals(a2));

        Action a11 = ((ContextAwareAction)a1).createContextAwareInstance(Lookup.EMPTY);
        assertTrue("Actions expected to be equal", a1.equals(a11));
    }
    
    public void testExtraPropertiesAndNamePropagation() throws Exception {
        Action a = readAction("testExtraProperties.instance");
        assertNull(MyAction.last);
        assertNotNull("Action created", a);
        a.actionPerformed(new ActionEvent(this, 0, ""));
        assertNotNull(MyAction.last);
        assertPropertyPropagated(Action.NAME, "Name1", a, MyAction.last);
        assertEquals("Short Desc1", a.getValue(Action.SHORT_DESCRIPTION));
        assertEquals("Menu Text1", a.getValue("menuText"));
        assertEquals("Popup Text1", a.getValue("popupText"));
    }

    public void testDisplayNameDiffer() throws Exception {
        Action a = readAction("testDisplayNameDiffer.instance");
        assertNull(MyAction.last);
        assertNotNull("Action created", a);
        a.actionPerformed(new ActionEvent(this, 0, ""));
        // Check LOG for warning
        assertEquals("MyNamedAction", a.getValue(Action.NAME)); // Queries the delegate
        assertEquals("MyNamedAction", MyAction.last.getValue(Action.NAME));
    }

    @RandomlyFails // NB-Core-Build #3634: Expected to be selected (#2) from testNbPreferences.instance
    public void testPreferencesAction() throws Exception {
//        checkPreferencesAction("testSystemPreferences.instance", Preferences.systemRoot());
        checkPreferencesAction("testUserPreferences.instance", "user:", Preferences.userRoot());
        checkPreferencesAction("testNbPreferences.instance", "", NbPreferences.root());
        checkPreferencesAction("testCustomPreferences.instance", "user:", Preferences.userRoot()); // customPreferences() uses "myNode" subnode
    }
    
    public void testPreferencesDefaultAction() throws Exception {
        Action a = readAction("testPreferencesDefault.instance");
        assertNotNull(a);
        Preferences prefsNode = Preferences.userRoot().node("myNode");
        prefsNode.remove("myKey");
        assertNull("Expected null as preference value", prefsNode.get("myKey", null));
        JMenuItem item = ((Presenter.Menu) a).getMenuPresenter();
        TestCase.assertTrue("Expected to be selected", item.isSelected());
    }

    private void checkPreferencesAction(String actionFileName, String preferencesNodePrefix, Preferences prefsRoot) throws Exception {
        Action a = readAction(actionFileName);
        Preferences prefsNode = prefsRoot.node("myNode");
        checkPreferencesAction(a, prefsNode);
        a = Actions.checkbox(preferencesNodePrefix + "/myNode", "myKey", null, null, false);
        checkPreferencesAction(a, prefsNode);
    }

    private void checkPreferencesAction(Action a, Preferences prefsNode) throws Exception {
        prefsNode.putBoolean("myKey", true);
        prefsNode.sync();
        class L implements PreferenceChangeListener {
            boolean notified;

            public synchronized void preferenceChange(PreferenceChangeEvent evt) {
                notified = true;
                notifyAll();
            }

            public synchronized void waitFor() throws Exception {
                while (!notified) {
                    wait();
                }
                notified = false;
            }
        }
        L listener = new L();

        // Verify value
        assertTrue("Expected true as preference value", prefsNode.getBoolean("myKey", false));

        TestCase.assertTrue("Expected to be instance of Presenter.Menu", a instanceof Presenter.Menu);
        JMenuItem item = ((Presenter.Menu) a).getMenuPresenter();
        TestCase.assertTrue("Expected to be selected", item.isSelected());
        prefsNode.addPreferenceChangeListener(listener);
        prefsNode.putBoolean("myKey", false);
        prefsNode.sync();
        listener.waitFor();
        TestCase.assertFalse("Expected to not be selected", item.isSelected());
        a.actionPerformed(null); // new ActionEvent(null, 0, ""));
        listener.waitFor();
        TestCase.assertTrue("Expected to be selected", item.isSelected());
        prefsNode.putBoolean("myKey", false);
        prefsNode.sync();
        listener.waitFor();
    }

    private static void assertPropertyPropagated(String propertyName, Object value, Action a, Action delegate) {
        assertEquals("Action's property \"" + propertyName + "\"", value, a.getValue(propertyName));
        assertEquals("Delegate's property \"" + propertyName + "\"", value, delegate.getValue(propertyName));
    }

    private static int myListenerCounter;
    private static int myListenerCalled;
    private static ActionListener myListener() {
        myListenerCounter++;
        return new MyListener();
    }
    private static ActionListener myAction() {
        myListenerCounter++;
        return new MyAction();
    }
    private static Action myNamedAction() {
        MyAction a = new MyAction();
        a.putValue(Action.NAME, "MyNamedAction");
        return a;
    }
    private static Action myIconAction() {
        MyAction a = new MyAction();
        final ImageIcon ii = new ImageIcon();
        a.putValue(MyAction.SMALL_ICON, ii);
        a.putValue("v", ii);
        return a;
    }
    private static ActionListener myContextAction() {
        myListenerCounter++;
        return new MyContextAction();
    }
    private static int myIconResourceCounter;
    private static String myIconResource() {
        myIconResourceCounter++;
        return "org/openide/awt/TestIcon.png";
    }
    
    
    private Action readAction(String fileName) throws Exception {
        FileObject fo = this.folder.getFileObject(fileName);
        assertNotNull("file " + fileName, fo);
        
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        
        if (!(obj instanceof Action)) {
            fail("Object needs to be action: " + obj);
        }
        
        return (Action)obj;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        changeCounter++;
    }

    private static class MyListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            myListenerCalled++;
        }
    }
    private static class MyAction extends AbstractAction {
        static MyAction last;

        MyAction() {
            last = this;
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            myListenerCalled++;
        }
    }
    private static class MyContextAction extends MyAction
    implements ContextAwareAction {
        static int clones;
        static Lookup lkp;

        public Action createContextAwareInstance(Lookup actionContext) {
            clones++;
            lkp = actionContext;
            return new MyContextAction();
        }
    }

    private static final class PreferencesAction extends AbstractAction {
        static PreferencesAction last;
        int performedCount;

        PreferencesAction() {
            last = this;
        }

        public void actionPerformed(ActionEvent e) {
            performedCount++;
        }
    }
    static Action preferencesAction() {
        return new PreferencesAction();
    }

    private static final Preferences customPrefs;
    
    static {
        customPrefs = Preferences.userRoot().node("/myNode");
        try {
            customPrefs.sync();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static Preferences customPreferences() {
        return customPrefs;
    }

}
