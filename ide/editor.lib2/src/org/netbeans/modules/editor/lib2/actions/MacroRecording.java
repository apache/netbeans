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

package org.netbeans.modules.editor.lib2.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.JTextComponent;

/**
 * Class handling macro recording of editor actions.
 *
 * @author Miloslav Metelka
 * @since 1.13
 */
public final class MacroRecording {

    /** Action's property for disabling recording of the action as part of a macro. */
    public static final String NO_MACRO_RECORDING_PROPERTY = "NoMacroRecording";
    
    private static final MacroRecording INSTANCE = new MacroRecording();

    public static MacroRecording get() {
        return INSTANCE;
    }

    private StringBuilder macroBuffer;

    private StringBuilder textBuffer;

    private MacroRecording() {
    }

    /**
     * Start recording a macro.
     *
     * @return true if macro recording started successfully or false otherwise.
     */
    public synchronized boolean startRecording() {
        if (isRecording()) {
            return false;
        }
        macroBuffer = new StringBuilder(100);
        textBuffer = new StringBuilder(20);
        return true;
    }

    /**
     * Stop macro recording.
     *
     * @return string describing the macro or null if no recording takes place currently.
     */
    public synchronized String stopRecording() {
        if (!isRecording()) {
            return null;
        }
        if (textBuffer.length() > 0) {
            if (macroBuffer.length() > 0) {
                macroBuffer.append( ' ' );
            }
            appendEncodedText(macroBuffer, textBuffer);
        }
        String completeMacroText = macroBuffer.toString();
        textBuffer = null;
        macroBuffer = null;
        return completeMacroText;
    }

    /**
     * Record given action into a macro buffer.
     *
     * @param action non-null action to record
     * @param evt non-null evt used when recording typed text of default key-typed action.
     */
    public synchronized void recordAction(Action action, ActionEvent evt, JTextComponent target) {
        if (!(isRecording() && !Boolean.TRUE.equals(action.getValue(NO_MACRO_RECORDING_PROPERTY)))) {
            return;
        }
        String actionName = actionName(action);
        if(action == target.getKeymap().getDefaultAction() ) { // defaultKeyTyped
            // see #218258; must filter key-typed events after key-pressed. Not ideal,
            // but shares logic with the actual action that inserts content into the editor.
            if (isValidDefaultTypedAction(evt) &&
                isValidDefaultTypedCommand(evt)) {
                textBuffer.append( evt.getActionCommand() );
            }
        } else {
            if (textBuffer.length() > 0) {
                if (macroBuffer.length() > 0) {
                    macroBuffer.append( ' ' );
                }
                appendEncodedText(macroBuffer, textBuffer);
                textBuffer.setLength(0);
            }
            if (macroBuffer.length() > 0) {
                macroBuffer.append(' ');
            }
            // Append encoded action name
            for (int i = 0; i < actionName.length(); i++) {
                char c = actionName.charAt(i);
                if (Character.isWhitespace(c) || c == '\\') {
                    macroBuffer.append('\\');
                }
                macroBuffer.append(c);
            }
        }
    }

    /**
     * Copied from BaseKit
     */
    static boolean isValidDefaultTypedAction(ActionEvent evt) {
        // Check whether the modifiers are OK
        int mod = evt.getModifiers();
        boolean ctrl = ((mod & ActionEvent.CTRL_MASK) != 0);
        boolean alt = org.openide.util.Utilities.isMac() ? ((mod & ActionEvent.META_MASK) != 0) :
            ((mod & ActionEvent.ALT_MASK) != 0);
        return !(alt || ctrl);
    }
    
    /**
     * Copied from BaseKit
     */
    static boolean isValidDefaultTypedCommand(ActionEvent evt) {
        final String cmd = evt.getActionCommand();
        return (cmd != null && cmd.length() == 1 && cmd.charAt(0) >= 0x20 && cmd.charAt(0) != 0x7F);
    }

    private boolean isRecording() {
        return (macroBuffer != null);
    }

    private static String actionName(Action action) {
        return (String) action.getValue(Action.NAME);
    }

    private static String getFilteredActionCommand(String cmd) {
        if (cmd == null || cmd.length() == 0) {
            return "";
        }
        char ch = cmd.charAt(0);
        if ((ch >= 0x20) && (ch != 0x7F)) {
            return cmd;
        } else {
            return "";
        }
    }

    private static void appendEncodedText(StringBuilder sb, StringBuilder text) {
        sb.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append('"');
    }

}
