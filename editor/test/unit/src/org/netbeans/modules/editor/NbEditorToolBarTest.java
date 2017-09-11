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

package org.netbeans.modules.editor;

import java.net.URL;
import java.util.Collection;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 */
public class NbEditorToolBarTest extends NbTestCase {

    public NbEditorToolBarTest(String testName) {
        super(testName);
    }

    public @Override boolean runInEQ() {
        return true;
    }

    protected @Override void setUp() throws Exception {
        super.setUp();

        clearWorkDir();

        EditorTestLookup.setLookup(
            new URL[] {
                EditorTestConstants.EDITOR_LAYER_URL,
            },
            new Object[] {},
            getClass().getClassLoader()
        );
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * is the first Lookup.Provider ancestor.
     */
    public void testActionContextAncestorsLookupProviderIsPreferred() throws Exception {
        JPanel parent1 = new LookupPanel(Lookups.singleton(new Foo() { }));
        JPanel parent2 = new LookupPanel(Lookups.singleton(new Bar() { }));
        parent1.add(parent2);
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent2.add(editor);
        DataObject docDataObject = createDataObject();
        assertNotNull(docDataObject);
        editor.getDocument().putProperty(Document.StreamDescriptionProperty, docDataObject);

        Lookup actionContext = NbEditorToolBar.createActionContext(editor);
        assertNotNull(actionContext.lookup(Bar.class));
        assertNotNull(actionContext.lookup(Node.class));
        assertNull(actionContext.lookup(Foo.class));
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * is the DataObject corresponding to the current document if there is no
     * Lookup.Provider ancestor.
     */
    public void testActionContextFallbackToDataObject() throws Exception {
        JPanel parent = new JPanel();
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent.add(editor);
        DataObject docDataObject = createDataObject();
        assertNotNull(docDataObject);
        editor.getDocument().putProperty(Document.StreamDescriptionProperty, docDataObject);

        Lookup actionContext = NbEditorToolBar.createActionContext(editor);
        assertNotNull(actionContext.lookup(Node.class));
        assertNull(actionContext.lookup(Foo.class));
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * contains the editor pane if there is no Lookup.Provider ancestor and no DataObject
     * corresponding to the current document.
     */
    public void testActionContextNullWhenNoDataObject() {
        JPanel parent = new JPanel();
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent.add(editor);

        Lookup actionContext = NbEditorToolBar.createActionContext(editor);
        // changed when fixing #127757
        //assertNull(actionContext);
        assertNotNull(actionContext);
        Collection<?> all = actionContext.lookupAll(Object.class);
        assertEquals("Expecting singleton Lookup", 1, all.size());
        assertSame("Expecting the editor pane", editor, all.iterator().next());
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * contains the node corresponding to the current document only once, even
     * though the node is both contained in an ancestor Lookup.Provider and
     * obtained as the node delegate of the DataObject of the current document.
     */
    public void testActionContextLookupContainsNodeOnlyOnce() throws Exception {
        DataObject docDataObject = createDataObject();
        assertNotNull(docDataObject);
        JPanel parent = new LookupPanel(Lookups.fixed(new Object[] { new Bar() { }, docDataObject.getNodeDelegate().getLookup() }));
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent.add(editor);
        editor.getDocument().putProperty(Document.StreamDescriptionProperty, docDataObject);

        Lookup actionContext = NbEditorToolBar.createActionContext(editor);
        assertNotNull(actionContext.lookup(Bar.class));
        assertNotNull(actionContext.lookup(Node.class));
        assertEquals(1, actionContext.lookup(new Lookup.Template(Node.class)).allInstances().size());
    }

    private DataObject createDataObject() throws Exception {
        getWorkDir().mkdirs();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileObject fo = lfs.getRoot().createData("file", "txt");
        return DataObject.find(fo);
    }

    private static final class LookupPanel extends JPanel implements Lookup.Provider {

        private final Lookup lookup;

        public LookupPanel(Lookup lookup) {
            this.lookup = lookup;
        }

        public Lookup getLookup() {
            return lookup;
        }
    }

    private static interface Foo {
    }

    private static interface Bar {
    }
}
