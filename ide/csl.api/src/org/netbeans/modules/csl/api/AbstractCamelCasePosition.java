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
package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public abstract class AbstractCamelCasePosition extends BaseAction {

    private Action originalAction;
    
    public AbstractCamelCasePosition(String name, Action originalAction) {
        super(name, MAGIC_POSITION_RESET);
        
        if (originalAction != null) {
            Object nameObj = originalAction.getValue(Action.NAME);
            if (nameObj instanceof String) {
                // We will be wrapping around the original action, use its name
                putValue(NAME, nameObj);
                this.originalAction = originalAction;
            }
        }
        
        String desc = getShortDescription();
        if (desc != null) {
            putValue(SHORT_DESCRIPTION, desc);
        }
    }

    public final void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (originalAction != null && !isUsingCamelCase()) {
                if (originalAction instanceof BaseAction) {
                    ((BaseAction) originalAction).actionPerformed(evt, target);
                } else {
                    originalAction.actionPerformed(evt);
                }
            } else {
                AtomicLockDocument bdoc = LineDocumentUtils.as(target.getDocument(), AtomicLockDocument.class);
                if (bdoc != null) {
                    bdoc.runAtomic(new Runnable() {
                        public void run() {
                            DocumentUtilities.setTypingModification(target.getDocument(), true);
                            try {
                                int offset = newOffset(target);
                                if (offset != -1) {
                                    moveToNewOffset(target, offset);
                                }
                            } catch (BadLocationException ble) {
                                target.getToolkit().beep();
                            } finally {
                                DocumentUtilities.setTypingModification(target.getDocument(), false);
                            }
                        }
                    });
                } else {
                    target.getToolkit().beep();
                }
            }
        }
    }

    protected abstract int newOffset(JTextComponent textComponent) throws BadLocationException;
    protected abstract void moveToNewOffset(JTextComponent textComponent, int offset) throws BadLocationException;

    public String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = NbBundle.getBundle(AbstractCamelCasePosition.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }

    private boolean isUsingCamelCase() {
        Preferences p = NbPreferences.root ();
        if ( p == null ) {
            return false;
        }
        return p.getBoolean("useCamelCaseStyleNavigation", true); // NOI18N
    }
}

