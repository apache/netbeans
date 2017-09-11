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

package org.netbeans.modules.editor.macros.storage.ui;

import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.macros.MacroDialogSupport;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class MacrosModelTest extends NbTestCase {
    
    public MacrosModelTest (String testName) {
        super (testName);
    }

    protected @Override void setUp() throws Exception {
//        super.setUp();
//        
//        EditorTestLookup.setLookup(
//            new URL[] {
//                getClass().getClassLoader().getResource("org/netbeans/modules/options/editor/mf-layer.xml"),
//                getClass().getClassLoader().getResource("org/netbeans/modules/java/editor/resources/layer.xml"),
//                getClass().getClassLoader().getResource("org/netbeans/modules/defaults/mf-layer.xml"),
//                getClass().getClassLoader().getResource("org/netbeans/modules/editor/settings/storage/layer.xml"), // mime types are detected by fontcolor settings
//                getClass().getClassLoader().getResource("org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
//                getClass().getClassLoader().getResource("org/netbeans/core/ui/resources/layer.xml")
//            },
//            getWorkDir(),
//            new Object[] {},
//            getClass().getClassLoader()
//        );
//                
//        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
//        // which is needed by Nb EntityCatalog (org.netbeans.core).
//        // Also see the test dependencies in project.xml
//        Main.initializeURLFactory();
        
        // The above doesn't work, because of problems with core/settings. So kick up
        // the whole module system.
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testAddMacro () {

        // 1) init model
        MacrosModel model = MacrosModel.get();
        model.load();
        int originalMacrosCount = model.getAllMacros().size();
        
        // 2) do some changes
        {
            MacrosModel.Macro macro1 = model.createMacro(MimePath.EMPTY, "testName");
            macro1.setCode("testValue");
            macro1.setShortcut("Alt+Shift+H");
        }
        {
            MacrosModel.Macro macro2 = model.createMacro(MimePath.EMPTY, "testName2");
            macro2.setCode("testValue2");
            macro2.setShortcut("Alt+Shift+R");
        }
        
        // 3) test changes
        assertEquals ("Wrong number of macros", originalMacrosCount + 2, model.getAllMacros().size());
        {
            MacrosModel.Macro macro = macroByName(model, "testName");
            assertNotNull("Cant find macro 'testName'", macro);
            assertEquals("Wrong macro.code value", "testValue", macro.getCode());
            assertEquals("Wrong number of shortcuts", 1, macro.getShortcuts().size());
            assertEquals("Wrong macro action name", MacroDialogSupport.RunMacroAction.runMacroAction, macro.getShortcuts().get(0).getActionName());
            assertEquals("Wrong number of keystrokes", 1, macro.getShortcuts().get(0).getKeyStrokeCount());
            assertEquals("Wrong shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), macro.getShortcuts().get(0).getKeyStrokeList().get(0));
        }
        {
            MacrosModel.Macro macro = macroByName(model, "testName2");
            assertNotNull("Cant find macro 'testName2'", macro);
            assertEquals("Wrong macro.code value", "testValue2", macro.getCode());
            assertEquals("Wrong number of shortcuts", 1, macro.getShortcuts().size());
            assertEquals("Wrong macro action name", MacroDialogSupport.RunMacroAction.runMacroAction, macro.getShortcuts().get(0).getActionName());
            assertEquals("Wrong number of keystrokes", 1, macro.getShortcuts().get(0).getKeyStrokeCount());
            assertEquals("Wrong shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), macro.getShortcuts().get(0).getKeyStrokeList().get(0));
        }
        
        model.save();
        
        // Discard the model
        WeakReference<MacrosModel> ref = new WeakReference<MacrosModel>(model);
        model = null;
        assertGC("The shared instance of MacrosModel was not GCed", ref);
        
        model = MacrosModel.get();
        model.load();
        
        // 4) test the changes again
        assertEquals ("Wrong number of macros", originalMacrosCount + 2, model.getAllMacros().size());
        {
            MacrosModel.Macro macro = macroByName(model, "testName");
            assertNotNull("Cant find macro 'testName'", macro);
            assertEquals("Wrong macro.code value", "testValue", macro.getCode());
            assertEquals("Wrong number of shortcuts", 1, macro.getShortcuts().size());
            assertEquals("Wrong macro action name", MacroDialogSupport.RunMacroAction.runMacroAction, macro.getShortcuts().get(0).getActionName());
            assertEquals("Wrong number of keystrokes", 1, macro.getShortcuts().get(0).getKeyStrokeCount());
            assertEquals("Wrong shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), macro.getShortcuts().get(0).getKeyStrokeList().get(0));
        }
        {
            MacrosModel.Macro macro = macroByName(model, "testName2");
            assertNotNull("Cant find macro 'testName2'", macro);
            assertEquals("Wrong macro.code value", "testValue2", macro.getCode());
            assertEquals("Wrong number of shortcuts", 1, macro.getShortcuts().size());
            assertEquals("Wrong macro action name", MacroDialogSupport.RunMacroAction.runMacroAction, macro.getShortcuts().get(0).getActionName());
            assertEquals("Wrong number of keystrokes", 1, macro.getShortcuts().get(0).getKeyStrokeCount());
            assertEquals("Wrong shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), macro.getShortcuts().get(0).getKeyStrokeList().get(0));
        }
    }

    private static MacrosModel.Macro macroByName(MacrosModel model, String macroName) {
        for(MacrosModel.Macro macro : model.getAllMacros()) {
            if (macro.getName().equals(macroName)) {
                return macro;
            }
        }
        return null;
    }
}
