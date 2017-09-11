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

package org.netbeans.modules.editor.lib2.testactionsregistration;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.netbeans.modules.editor.lib2.actions.SearchableEditorKit;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.AnnotationProcessorTestUtils;

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

}
