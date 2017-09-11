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
package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
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
                final BaseDocument bdoc = org.netbeans.editor.Utilities.getDocument(target);
                if (bdoc != null) {
                    bdoc.runAtomic(new Runnable() {
                        public void run() {
                            DocumentUtilities.setTypingModification(bdoc, true);
                            try {
                                int offset = newOffset(target);
                                if (offset != -1) {
                                    moveToNewOffset(target, offset);
                                }
                            } catch (BadLocationException ble) {
                                target.getToolkit().beep();
                            } finally {
                                DocumentUtilities.setTypingModification(bdoc, false);
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

