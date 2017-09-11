/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.editor.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.editor.EditorActionNames;

/**
 * Toggle toolbar/lines visibility.
 *
 * @author Miloslav Metelka
 */
@EditorActionRegistrations({
    @EditorActionRegistration(
        name = EditorActionNames.toggleToolbar,
        menuPath = "View",
        menuPosition = 800,
        menuText = "#" + EditorActionNames.toggleToolbar + "_menu_text",
        preferencesKey = SimpleValueNames.TOOLBAR_VISIBLE_PROP
    ),
    @EditorActionRegistration(
        name = EditorActionNames.toggleLineNumbers,
        menuPath = "View",
        menuPosition = 850,
        menuText = "#" + EditorActionNames.toggleLineNumbers + "_menu_text",
        preferencesKey = SimpleValueNames.LINE_NUMBER_VISIBLE
    ),
    @EditorActionRegistration(
        name = EditorActionNames.toggleNonPrintableCharacters,
        menuPath = "View",
        menuPosition = 870,
        menuText = "#" + EditorActionNames.toggleNonPrintableCharacters + "_menu_text",
        preferencesKey = SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE
    )
})
public final class ToggleAction extends AbstractEditorAction {

    private static final Logger LOG = Logger.getLogger(ToggleAction.class.getName());

    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent component) {
        // Leave empty - AlwaysEnabledAction toggles state in preferences by default
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("actionPerformed: actionName=" + actionName());
        }
    }

}
