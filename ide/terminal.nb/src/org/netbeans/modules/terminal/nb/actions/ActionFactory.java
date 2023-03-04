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
