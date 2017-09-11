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

package org.netbeans.jellytools;

import javax.swing.JDialog;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Handle generic NetBeans dialog. The dialog can include Yes, No, OK,
 * Cancel, Close or Help buttons. The dialog is identified by its title.
 */
public class NbDialogOperator extends JDialogOperator {

    private JButtonOperator _btYes;
    private JButtonOperator _btNo;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;
    
    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
    }
    
    /** Waits until dialog with requested title is found. Title is compared
     * on partial match and case non-sensitive. If dialog is not found, runtime
     * exception is thrown.
     * @param title  title of window
     */
    public NbDialogOperator(String title) {
        super(title);
    }
    
    /** Created NbDialogOperator with given dialog
     * @param dialog JDialog instance */
    public NbDialogOperator(JDialog dialog) {
        super(dialog);
    }
    
    /** Returns operator of "Yes" button.
     * @return  JButtonOperator instance of "Yes" button
     */
    public JButtonOperator btYes() {
        if (_btYes == null) {
            String yesCaption = Bundle.getString("org.netbeans.core.windows.services.Bundle", "YES_OPTION_CAPTION");
            _btYes = new JButtonOperator(this, yesCaption);
        }
        return _btYes;
    }

    /** Returns operator of "No" button.
     * @return  JButtonOperator instance of "No" button
     */
    public JButtonOperator btNo() {
        if (_btNo == null) {
            String noCaption = Bundle.getString("org.netbeans.core.windows.services.Bundle", "NO_OPTION_CAPTION");
            _btNo = new JButtonOperator(this, noCaption);
        }
        return _btNo;
    }

    /** Returns operator of "OK" button.
     * @return  JButtonOperator instance of "OK" button
     */
    public JButtonOperator btOK() {
        if (_btOK == null) {
            String oKCaption = Bundle.getString("org.netbeans.core.windows.services.Bundle", "OK_OPTION_CAPTION");
            _btOK = new JButtonOperator(this, oKCaption);
        }
        return _btOK;
    }

    /** Returns operator of "Cancel" button.
     * @return  JButtonOperator instance of "Cancel" button
     */
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            String cancelCaption = Bundle.getString("org.netbeans.core.windows.services.Bundle", "CANCEL_OPTION_CAPTION");
            _btCancel = new JButtonOperator(this, cancelCaption);
        }
        return _btCancel;
    }

    /** Returns operator of "Close" button.
     * @return  JButtonOperator instance of "Close" button
     */
    public JButtonOperator btClose() {
        if (_btClose == null) {
            String closeCaption = Bundle.getString("org.netbeans.core.windows.services.Bundle", "CLOSED_OPTION_CAPTION");
            _btClose = new JButtonOperator(this, closeCaption);
        }
        return _btClose;
    }
    
    /** Returns operator of "Help" button.
     * @return  JButtonOperator instance of "Help" button
     */
    public JButtonOperator btHelp() {
        if (_btHelp == null) {
            String helpCaption = Bundle.getStringTrimmed("org.netbeans.core.windows.services.Bundle", "HELP_OPTION_CAPTION");
            _btHelp = new JButtonOperator(this, helpCaption);
        }
        return _btHelp;
    }
    
    /** Pushes "Yes" button. */
    public void yes() {
        btYes().push();
    }
    
    /** Pushes "No" button. */
    public void no() {
        btNo().push();
    }
    
    /** Pushes "OK" button. */
    public void ok() {
        btOK().push();
    }
    
    /** Pushes "Cancel" button. */
    public void cancel() {
        btCancel().push();
    }

    /** Pushes "Close" button. Using {@link JDialogOperator#close() close()}
     * will close dialog default way like on demand of a window manager.
     */
    public void closeByButton() {
        btClose().push();
    }
    
    /** Pushes "Help" button. */
    public void help() {
        btHelp().push();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void close() {
        requestClose();
    }
}
