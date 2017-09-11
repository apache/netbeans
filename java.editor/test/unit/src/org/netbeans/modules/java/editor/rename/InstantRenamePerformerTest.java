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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.rename;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.JavaDataObject;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author lahvac
 */
public class InstantRenamePerformerTest extends NbTestCase {
    
    public InstantRenamePerformerTest(String testName) {
        super(testName);
    }            

    protected void setUp() throws Exception {
        MockMimeLookup.setInstances(
                MimePath.parse("text/x-java"),
                new JavaKit());
        MockMimeLookup mml = new MockMimeLookup();
        Class.forName(SourceUtilsTestUtil.class.getName(), true, SourceUtilsTestUtil.class.getClassLoader());
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[] {new DefaultPool(), mml, JavaDataLoader.findObject(JavaDataLoader.class, true)});
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LifecycleManager.getDefault().saveAll();
    }
    
    public void testSimple1() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 80 - 22, ke, "package test; public class Test { public void test() {int axxx = 0; int y = axxx; } }", true);
    }

    public void testSimple2() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 84 - 22 - 1, ke, "package test; public class Test { public void test() {int xxxa = 0; int y = xxxa; } }", true);
    }

    public void testSimple3() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0');
        performTest("package test; public class Test { public void test() {int a|bc = 0; int y = abc; } }", 83 - 22 - 1, ke, "package test; public class Test { public void test() {int ac = 0; int y = ac; } }", true);
    }

    public void testSimple4() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0');
        performTest("package test; public class Test { public void test() {int a|bc = 0; int y = abc; } }", 84 - 22 - 1, ke, "package test; public class Test { public void test() {int ab = 0; int y = ab; } }", true);
    }

    public void testSimple5() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DELETE, '\0');
        performTest("package test; public class Test { public void test() {int a|bc = 0; int y = abc; } }", 80 - 22, ke, "package test; public class Test { public void test() {int bc = 0; int y = bc; } }", true);
    }

    public void testSimple6() throws Exception {
        KeyEvent[] kes = new KeyEvent[] {new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0'), new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0')};
        performTest("package test; public class Test { public void test() {int b| = 0; int y = b; } }", 80 - 22, kes, 81 - 22, "package test; public class Test { public void test() {int = 0; int y = ; } }", false);
    }

    public void testCancel1() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 79 - 22, ke, "package test; public class Test { public void test() {inta xxx = 0; int y = xxx; } }", false);
    }

    public void testCancel2() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 85 - 22 - 1, ke, "package test; public class Test { public void test() {int xxx a= 0; int y = xxx; } }", false);
    }

    public void testCancel3() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 79 - 22, ke, "package test; public class Test { public void test() {in xxx = 0; int y = xxx; } }", false);
    }

    public void testCancel4() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 85 - 22 - 1, ke, "package test; public class Test { public void test() {int xxx= 0; int y = xxx; } }", false);
    }

    public void testCancel5() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DELETE, '\0');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 79 - 22, ke, "package test; public class Test { public void test() {intxxx = 0; int y = xxx; } }", false);
    }

    public void testCancel6() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DELETE, '\0');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 84 - 22 - 1, ke, "package test; public class Test { public void test() {int xxx= 0; int y = xxx; } }", false);
    }

    //TODO:
//    public void testUndoAndContinue1() throws Exception {
//        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, '\0');
//        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 80 - 22, ke, "package test; public class Test { public void test() {int xxx = 0; int y = xxx; } }", true);
//    }
//
//    public void testUndoAndContinue2() throws Exception {
//        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DELETE, '\0');
//        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 84 - 22 - 1, ke, "package test; public class Test { public void test() {int xxx = 0; int y = xxx; } }", true);
//    }

    public void testSelection1() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 80 - 22, ke, 84 - 22 - 1, "package test; public class Test { public void test() {int a = 0; int y = a; } }", true);
    }

    public void testSelection2() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 79 - 22, ke, 85 - 22 - 1, "package test; public class Test { public void test() {inta= 0; int y = xxx; } }", false);
    }

    public void testSelection3() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 81 - 22, ke, 85 - 22 - 1, "package test; public class Test { public void test() {int xa= 0; int y = xxx; } }", false);
    }

    public void testSelection4() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x|xx = 0; int y = xxx; } }", 79 - 22, ke, 83 - 22 - 1, "package test; public class Test { public void test() {intax = 0; int y = xxx; } }", false);
    }

    public void testSelection126704a() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x| = 0; int y = x; } }", 81 - 22, ke, 80 - 22, "package test; public class Test { public void test() {int a = 0; int y = a; } }", true);
    }

    public void testSelection126704b() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a');
        performTest("package test; public class Test { public void test() {int x| = 0; int y = x; } }", 80 - 22, ke, 82 - 22 - 1, "package test; public class Test { public void test() {int a = 0; int y = a; } }", true);
    }
    
    public void testTypeParameters153337() throws Exception {
        KeyEvent ke = new KeyEvent(new JFrame(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'R');
        performTest("package test; public class Test { public <T|T> void test(TT t) { } }", 64 - 22, ke, - 1, "package test; public class Test { public <RTT> void test(RTT t) { } }", true);
    }

    private void performTest(String sourceCode, int offset, KeyEvent ke, String golden, boolean stillInRename) throws Exception {
        performTest(sourceCode, offset, ke, -1, golden, stillInRename);
    }

    private void performTest(String sourceCode, int offset, KeyEvent ke, int selectionEnd, String golden, boolean stillInRename) throws Exception {
        KeyEvent[] kes = new KeyEvent[] {ke};
        performTest(sourceCode, offset, kes, selectionEnd, golden, stillInRename);
    }
    
    private void performTest(String sourceCode, int offset, KeyEvent[] kes, int selectionEnd, String golden, boolean stillInRename) throws Exception {
        clearWorkDir();
        
        FileObject root = FileUtil.toFileObject(getWorkDir());
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        FileObject testDir  = sourceDir.createFolder("test");
        
        FileObject source = testDir.createData("Test.java");
        
        TestUtilities.copyStringToFile(source, sourceCode.replaceFirst(Pattern.quote("|"), ""));
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        SourceUtilsTestUtil.compileRecursively(sourceDir);
        
        DataObject od = DataObject.find(source);
        
        assertTrue(od instanceof JavaDataObject);
        
        EditorCookie ec = od.getCookie(EditorCookie.class);
        Document doc = ec.openDocument();
        
        assertTrue(doc instanceof BaseDocument);
        
        UndoManager um = new UndoManager();

        doc.addUndoableEditListener(um);
        doc.putProperty(BaseDocument.UNDO_MANAGER_PROP, um);
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        
        C p = new C();
        
//        p.setEditorKit(new JavaKit());
        
        p.setDocument(doc);
        
        p.setCaretPosition(sourceCode.indexOf('|'));
        
        InstantRenamePerformer.invokeInstantRename(p);
        
        p.setCaretPosition(offset);

        if (selectionEnd != (-1)) {
            p.moveCaretPosition(selectionEnd);
        }
        
        processKeyevents(p, kes);
        
        assertEquals(stillInRename, p.getClientProperty(InstantRenamePerformer.class) != null);
        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

    private void processKeyevents(C p, KeyEvent[] kes) {
        for (int i = 0; i < kes.length; i++) {
            p.processKeyEvent(kes[i]);
        }
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    private static final class DefaultPool extends DataLoaderPool implements LookupListener {
        private final Lookup.Result<DataLoader> result;

        public DefaultPool() {
            result = Lookup.getDefault().lookupResult(DataLoader.class);
            result.addLookupListener(this);
        }

        protected Enumeration<? extends DataLoader> loaders() {
            return Collections.enumeration(result.allInstances());
        }

        public void resultChanged(LookupEvent e) {
            fireChangeEvent(new ChangeEvent(this));
        }
    }
    
    private static final class C extends JEditorPane {

        @Override
        public void processKeyEvent(KeyEvent e) {
            super.processKeyEvent(e);
        }
        
    }
}
