/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.terminal.nb.actions;

import javax.swing.Action;
import org.openide.awt.Actions;

/**
 *
 * @author igromov
 */
public class ActionFactory {

    public static final String CATEGORY = "Terminal"; //NOI18N
    public static final String ACTIONS_PATH = "Actions/Terminal"; //NOI18N
    public static final String DUMP_SEQUENCE_ACTION_ID = "org.netbeans.modules.terminal.actions.DumpSequenceAction"; //NOI18N
    public static final String COPY_ACTION_ID = "org.netbeans.modules.terminal.actions.CopyAction"; //NOI18N
    public static final String CLOSE_ACTION_ID = "org.netbeans.modules.terminal.actions.CloseAction"; //NOI18N
    public static final String PASTE_ACTION_ID = "org.netbeans.modules.terminal.actions.PasteAction"; //NOI18N
    public static final String FIND_ACTION_ID = "org.netbeans.modules.terminal.actions.FindAction"; //NOI18N
    public static final String CLEAR_ACTION_ID = "org.netbeans.modules.terminal.actions.ClearAction"; //NOI18N
    public static final String LARGER_FONT_ACTION_ID = "org.netbeans.modules.terminal.actions.LargerFontAction"; //NOI18N
    public static final String SMALLER_FONT_ACTION_ID = "org.netbeans.modules.terminal.actions.SmallerFontAction"; //NOI18N
    public static final String WRAP_ACTION_ID = "org.netbeans.modules.terminal.actions.WrapAction"; //NOI18N
    public static final String SET_TITLE_ACTION_ID = "org.netbeans.modules.terminal.actions.SetTitleAction"; //NOI18N
    public static final String PIN_TAB_ACTION_ID = "org.netbeans.modules.terminal.actions.PinTabAction"; //NOI18N
    public static final String SWITCH_TAB_ACTION_ID = "org.netbeans.modules.terminal.actions.SwitchTabAction"; //NOI18N
    public static final String NEW_TAB_ACTION_ID = "org.netbeans.modules.terminal.actions.NewTabAction"; //NOI18N

    public static Action forID(String id) {
	return Actions.forID(CATEGORY, id);
    }
}
