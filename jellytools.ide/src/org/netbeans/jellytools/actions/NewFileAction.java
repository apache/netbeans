/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;

/**
 * Used to call "File|New File..." main menu item, "New|Other..." popup menu
 * item, "org.netbeans.modules.project.ui.actions.NewFile" action or Ctrl+N
 * shortcut.<br> Usage:
 * <pre>
 *    new NewFileAction().performMenu();
 *    new NewFileAction().performPopup();
 *    new NewFileAction().performShortcut();
 * </pre>
 *
 * @see Action
 * @see ActionNoBlock
 * @author tb115823
 */
public class NewFileAction extends ActionNoBlock {

    /**
     * "New" popup menu item.
     */
    private static final String popupPathNew = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_PopupName");
    /**
     * "Other..." popup menu sub item.
     */
    private static final String popupSubPath = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_File_PopupName");
    /**
     * File|New File..." main menu path.
     */
    private static final String menuPathNewFile = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File")
            + "|"
            + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_Name");
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK);

    /**
     * Creates new NewFileAction instance.
     */
    public NewFileAction() {
        super(menuPathNewFile, popupPathNew + "|" + popupSubPath, "org.netbeans.modules.project.ui.actions.NewFile", keystroke);
    }

    /**
     * Create new NewFileAction instance with name of template for popup
     * operation (only popup mode allowed).
     *
     * @param templateName name of template shown in sub menu (e.g. "Java Main
     * Class")
     */
    public NewFileAction(String templateName) {
        super(null, popupPathNew + "|" + templateName);
    }
}
