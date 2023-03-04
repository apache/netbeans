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

package org.netbeans.modules.editor.lib2.testactionsregistration;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.modules.editor.impl.ToolbarActionsProvider;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.netbeans.modules.editor.lib2.actions.SearchableEditorKit;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.AnnotationProcessorTestUtils;

import org.openide.loaders.DataShadow;

/**
 * Test registration of editor actions through an annotation.
 *
 * @author Miloslav Metelka
 */
public class EditorActionRegistrationTest extends NbTestCase {
    
    private static final String NAME1 = "editor-test-action1";
    private static final String NAME2 = "editor-test-action2";
    private static final String NAME3 = "editor-test-action3";
    private static final String NAME4 = "editor-test-action4";
    private static final String NAME5 = "editor-test-action5";
    private static final String NAME_NO_ICON_AND_KEY_BINDING = "editor-test-action-no-icon-and-kb";

    private static final String bundleHash = "org.netbeans.modules.editor.lib2.testactionsregistration.Bundle#";

    public EditorActionRegistrationTest(String name) {
        super(name);
    }

    public void testRegistration() throws Exception {
        FileObject fo = FileUtil.getConfigFile("/Editors/Actions/" + NAME1 + ".instance");
        assertNotNull(fo);
        assertEquals(fo.getAttribute(Action.SHORT_DESCRIPTION), fo.getAttribute("displayName"));
        assertEquals("Short Desc1", fo.getAttribute(Action.SHORT_DESCRIPTION));
        assertEquals("Short Desc1", fo.getAttribute("menuText"));
        assertEquals("Short Desc1", fo.getAttribute("popupText"));

        fo = FileUtil.getConfigFile("/Editors/Actions/" + NAME2 + ".instance");
        assertNotNull(fo);
        assertEquals(fo.getAttribute(Action.SHORT_DESCRIPTION), fo.getAttribute("displayName"));
        assertEquals("Short Desc2", fo.getAttribute(Action.SHORT_DESCRIPTION));
        assertEquals("Menu Text2", fo.getAttribute("menuText"));
        assertEquals("Menu Text2", fo.getAttribute("popupText"));

        fo = FileUtil.getConfigFile("/Editors/Actions/" + NAME3 + ".instance");
        assertNotNull(fo);
        assertEquals(fo.getAttribute(Action.SHORT_DESCRIPTION), fo.getAttribute("displayName"));
        assertEquals("Short Desc3", fo.getAttribute(Action.SHORT_DESCRIPTION));
        assertEquals("Menu Text3", fo.getAttribute("menuText"));
        assertEquals("Popup Text3", fo.getAttribute("popupText"));

        fo = FileUtil.getConfigFile("/Editors/Actions/" + NAME5 + ".instance");
        assertNotNull(fo);
        assertEquals(fo.getAttribute(Action.SHORT_DESCRIPTION), fo.getAttribute("displayName"));
        assertEquals("Short Desc5", fo.getAttribute(Action.SHORT_DESCRIPTION));
        fo = FileUtil.getConfigFile("/OptionsDialog/Actions/NAME5#CATEGORY/" + NAME5);
        assertNotNull(fo);
        assertFalse(fo.getAttributes().hasMoreElements());
    }

    public void testRegistrationNoIconAndKeyBinding() throws Exception {
        FileObject fo = FileUtil.getConfigFile("/Editors/Actions/" + NAME_NO_ICON_AND_KEY_BINDING + ".instance");
        assertNotNull(fo);
        assertTrue((Boolean) fo.getAttribute(AbstractEditorAction.NO_ICON_IN_MENU));
        assertTrue((Boolean) fo.getAttribute(AbstractEditorAction.NO_KEY_BINDING));
        SearchableEditorKit globalActionsKit = EditorActionUtilities.getGlobalActionsKit();
        AbstractEditorAction a = (AbstractEditorAction) globalActionsKit.getAction(NAME_NO_ICON_AND_KEY_BINDING);
        assertTrue((Boolean) a.getValue(AbstractEditorAction.NO_ICON_IN_MENU));
        assertTrue((Boolean) a.getValue(AbstractEditorAction.NO_KEY_BINDING));
    }
    
    public void testToolbarRegistration() throws Exception {
        FileObject toolbarDir = FileUtil.getConfigFile("/Editors/text/foo/Toolbars/Default");
        assertNotNull("text/base toolbar directory is null", toolbarDir);
        FileObject toolbarLink = toolbarDir.getFileObject(EditorTestActionToolbar.id + ".shadow"); // NOI18N
        assertNotNull("Action link in toolbar must exist", toolbarLink);
        FileObject orig = DataShadow.findOriginal(toolbarLink);
        assertNotNull("Action registration must exist", orig);
        
        List items = ToolbarActionsProvider.getToolbarItems("text/foo");
        assertFalse("At least one registration must be present", items.isEmpty());
        for (Object o : items) {
            if (!(o instanceof Action)) {
                continue;
            }
            Action a = (Action)o;
            String name = (String)a.getValue(Action.NAME);
            if (EditorTestActionToolbar.id.equals(name)) {
                return; // OK
            }
        }
        fail("Registered action not found");
    }

    public void testNonPublicClass() throws Exception {
        String nonPublicClassSource =
                "import org.netbeans.api.editor.EditorActionRegistration;\n" +
                "import javax.swing.AbstractAction;\n" +
                "import java.awt.event.ActionEvent;\n" +
                "@EditorActionRegistration(name = \"NonPublicClass\", shortDescription = \"\")\n" +
                "final class EditorTestActionNonPublic extends AbstractAction {\n" +
                "        public EditorTestActionNonPublic() {}\n" +
                "        @Override\n" +
                "        public void actionPerformed(ActionEvent evt) {}\n"+
                "}\n";
        checkCompilationFails(nonPublicClassSource);
    }

    public void testNonPublicConstructor() throws Exception {
        String nonPublicConstructorSource =
                "import org.netbeans.api.editor.EditorActionRegistration;\n" +
                "import javax.swing.AbstractAction;\n" +
                "import java.awt.event.ActionEvent;\n" +
                "@EditorActionRegistration(name = \"NonPublicClass\", shortDescription = \"\")\n" +
                "public final class EditorTestActionNonPublic extends AbstractAction {\n" +
                "        EditorTestActionNonPublic() {}\n" +
                "        @Override\n" +
                "        public void actionPerformed(ActionEvent evt) {}\n"+
                "}\n";
        checkCompilationFails(nonPublicConstructorSource);

    }

    public void checkCompilationFails(String classSource) throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.EditorTestActionNonPublic", classSource);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, out);
        assertFalse("Compilation failed", res);
        if (!out.toString().contains("not public")) {
            fail(out.toString());
        }
    }

    @EditorActionRegistration(name = NAME1)
    public static final class EditorTestAction extends AbstractAction {

        public EditorTestAction() {
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
        }

    }

    @EditorActionRegistration(
            name = NAME2,
            shortDescription="#editor-test-action-explicit",
            menuText = bundleHash + NAME2 + "_menu_text"
            // popupText same like menuText
    )
    public static EditorTestAction createAction2() {
        return new EditorTestAction();
    }

    @EditorActionRegistration(
            name = NAME3,
            menuText = "#" + NAME3 + "_menu_text",
            popupText = "#" + NAME3 + "_popup_text"
    )
    public static EditorTestAction createAction3() {
        return new EditorTestAction();
    }

    @EditorActionRegistration(
            name = NAME4,
            shortDescription = ""
    )
    public static EditorTestAction createAction4() {
        return new EditorTestAction();
    }

    @EditorActionRegistration(
            name = NAME5,
            category = "NAME5#CATEGORY"
    )
    public static EditorTestAction createAction5() {
        return new EditorTestAction();
    }

    @EditorActionRegistration(
            name = NAME_NO_ICON_AND_KEY_BINDING,
            shortDescription = "",
            noIconInMenu = true,
            noKeyBinding = true
    )
    public static EditorTestAction createActionNoIconAndKeyBinding() {
        return new EditorTestAction();
    }

    @EditorActionRegistration(name = "NonPublicClass", shortDescription = "")
    public static final class EditorTestActionNonPublic extends AbstractAction {

        public EditorTestActionNonPublic() {
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
        }

    }
    
    @EditorActionRegistration(name = "ToolbarIcon", shortDescription = "", 
            mimeType="text/foo", toolBarPosition = 1000)
    public static final class EditorTestActionToolbar extends AbstractAction {
        static final String id = "ToolbarIcon";
        public EditorTestActionToolbar() {
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
        }

    }
    
}
