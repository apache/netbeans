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

package org.apache.tools.ant.module.wizards.shortcut;

import java.util.Arrays;
import javax.swing.ListModel;
import org.openide.loaders.DataFolder;

/**
 * Test functionality of SelectFolderPanel.
 * @author Jesse Glick
 */
public final class SelectFolderPanelTest extends ShortcutWizardTestBase {

    public SelectFolderPanelTest(String name) {
        super(name);
    }

    private SelectFolderPanel.SelectFolderWizardPanel menuPanel;
    private SelectFolderPanel.SelectFolderWizardPanel toolbarsPanel;
    private ListModel menuListModel;
    private ListModel toolbarsListModel;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        iter.nextPanel();
        wiz.putProperty(ShortcutWizard.PROP_SHOW_MENU, true);
        wiz.putProperty(ShortcutWizard.PROP_SHOW_TOOL, true);
        menuPanel = (SelectFolderPanel.SelectFolderWizardPanel)iter.current();
        menuListModel = menuPanel.getPanel().getModel();
        iter.nextPanel();
        toolbarsPanel = (SelectFolderPanel.SelectFolderWizardPanel)iter.current();
        toolbarsListModel = toolbarsPanel.getPanel().getModel();
    }
    
    public void testFolderListDisplay() throws Exception {
        String[] names = new String[menuListModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = menuPanel.getPanel().getNestedDisplayName((DataFolder)menuListModel.getElementAt(i));
        }
        String[] expected = {
            "File",
            "Edit",
            "Build",
            "Build \u2192 Other",
            "Help",
        };
        assertEquals("right names in list", Arrays.asList(expected), Arrays.asList(names));
        names = new String[toolbarsListModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = toolbarsPanel.getPanel().getNestedDisplayName((DataFolder)toolbarsListModel.getElementAt(i));
        }
        expected = new String[] {
            "Build",
            "Help",
        };
        assertEquals("right names in list", Arrays.asList(expected), Arrays.asList(names));
    }
    
    // XXX test setting correct folder & display name in wizard data
    
}
