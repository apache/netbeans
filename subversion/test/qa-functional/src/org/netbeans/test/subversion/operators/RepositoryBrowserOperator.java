/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "RepositoryBrowserOperator" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class RepositoryBrowserOperator extends NbDialogOperator {

    /** Creates new RepositoryBrowserOperator that can handle it.
     */
    public RepositoryBrowserOperator() {
        super("Browse Repository");
    }

    private JTableOperator _table;
    private JLabelOperator _lblSpecifyFolderToCheckout;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTableOperator table() {
        if (_table == null) {
            _table = new JTableOperator(this);
        }
        return _table;
    }

    /** Tries to find "Specify Folder to checkout:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyFolderToCheckout() {
        if (_lblSpecifyFolderToCheckout==null) {
            _lblSpecifyFolderToCheckout = new JLabelOperator(this);
        }
        return _lblSpecifyFolderToCheckout;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** Performs popup menu on specified row.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopup(int row, String popupPath) {
        table().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(table().callPopupOnCell(row, 0));
        popup.pushMenu(popupPath);
    }

    /** Performs popup menu on specified file.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopup(String filename, String popupPath) {
        performPopup(table().findCellRow(filename), popupPath);
    }

    /** Performs popup menu on specified row and no block further execution.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(int row, String popupPath) {
        table().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(table().callPopupOnCell(row, 0));
        popup.pushMenuNoBlock(popupPath);
    }

    /** Performs popup menu on specified file and no block further execution.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(String filename, String popupPath) {
        performPopupNoBlock(table().findCellRow(filename), popupPath);
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "OK" JButton
     */
    @Override
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    @Override
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    @Override
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of RepositoryBrowserOperator by accessing all its components.
     */
    public void verify() {
        table();
        lblSpecifyFolderToCheckout();
        btOK();
        btCancel();
        btHelp();
    }
}

