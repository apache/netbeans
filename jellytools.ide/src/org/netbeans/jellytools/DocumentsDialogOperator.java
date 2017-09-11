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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import org.netbeans.jellytools.actions.DocumentsAction;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;

/**
 * Handle the dialog which is open from main menu "Window|Documents...".
 * This dialog shows list of all opened documents and it enables to close
 * or save selected documents. You can also activate one of documents.
 * <p>
 * Usage:<br>
 * <pre>
        DocumentsDialogOperator ddo = DocumentsDialogOperator.invoke();
        ddo.selectDocument("MyDocument");
        ddo.activate();
        ddo = DocumentsDialogOperator.invoke();
        ddo.selectDocuments(new int[] {0, 3, 4});
        ddo.closeDocuments();
        ddo.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see org.netbeans.jellytools.actions.DocumentsAction
 */
public class DocumentsDialogOperator extends NbDialogOperator {
    
    private JButtonOperator _btClose;
    private JButtonOperator _btActivate;
    private JButtonOperator _btCloseDocuments;
    private JButtonOperator _btSaveDocuments;
    private JTextAreaOperator _txtDescription;
    private JListOperator _lstDocuments;
    
    private static final String title = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle",
                                                         "CTL_DocumentsTitle");
    
    /** Waits until dialog with title Documents is found. */
    public DocumentsDialogOperator() {
        super(title);
    }
    
    /** Invokes dialog from main menu "Windows|Documents...".
     * @return instance of DocumentsDialogOperator
     */
    public static DocumentsDialogOperator invoke() {
        new DocumentsAction().perform();
        return new DocumentsDialogOperator();
    }
    
    /** Returns operator of "Switch to Document" button.
     * @return  JButtonOperator instance of "Switch to Document" button
     */
    public JButtonOperator btSwitchToDocument() {
        if (_btActivate == null) {
            String switchCaption = Bundle.getStringTrimmed("org.netbeans.core.windows.view.ui.Bundle", 
                                                           "LBL_Activate");
            _btActivate = new JButtonOperator(this, switchCaption);
        }
        return _btActivate;
    }

    /** Returns operator of "Close Document(s)" button.
     * @return  JButtonOperator instance of "Close Document(s)" button
     */
    public JButtonOperator btCloseDocuments() {
        if (_btCloseDocuments == null) {
            String closeDocumentsCaption = Bundle.getStringTrimmed("org.netbeans.core.windows.view.ui.Bundle", 
                                                                   "LBL_CloseDocuments");
            _btCloseDocuments = new JButtonOperator(this, closeDocumentsCaption);
        }
        return _btCloseDocuments;
    }

    /** Returns operator of "Save Document(s)" button.
     * @return  JButtonOperator instance of "Save Document(s)" button
     */
    public JButtonOperator btSaveDocuments() {
        if (_btSaveDocuments == null) {
            String saveDocumentsCaption = Bundle.getStringTrimmed("org.netbeans.core.windows.view.ui.Bundle", 
                                                                  "LBL_SaveDocuments");
            _btSaveDocuments = new JButtonOperator(this, saveDocumentsCaption);
        }
        return _btSaveDocuments;
    }
    
    /** Returns operator of "Close" button.
     * @return  JButtonOperator instance of "Close" button
     */
    @Override
    public JButtonOperator btClose() {
        if (_btClose == null) {
            String closeCaption = Bundle.getStringTrimmed("org.netbeans.core.windows.services.Bundle", 
                                                          "CLOSED_OPTION_CAPTION");
            // need to set exact matching comparator becouse Close button can 
            // be mistaken for Close Document(s) button
            StringComparator oldComparator = this.getComparator();
            this.setComparator(new DefaultStringComparator(true, true));
            _btClose = new JButtonOperator(this, closeCaption);
            this.setComparator(oldComparator);
        }
        return _btClose;
    }

    /** Returns operator of "Description" text area.
     * @return  JTextAreaOperator instance of "Description" text area
     */
    public JTextAreaOperator txtDescription() {
        if (_txtDescription == null) {
            _txtDescription = new JTextAreaOperator(this);
        }
        return _txtDescription;
    }

    /** Returns operator of list of documents.
     * @return  JListOperator instance of list of documents
     */
    public JListOperator lstDocuments() {
        if (_lstDocuments == null) {
            _lstDocuments = new JListOperator(this);
        }
        return _lstDocuments;
    }

    /** Pushes "Switch to Document" button. It closes the dialog and focuses selected
     * document in the editor area.
     */
    public void switchToDocument() {
        btSwitchToDocument().push();
    }
    
    /** Pushes "Close Document(s)" button. */
    public void closeDocuments() {
        btCloseDocuments().push();
    }
    
    /** Pushes "Save Document(s)" button. */
    public void saveDocuments() {
        btSaveDocuments().push();
    }
    
    /** Returns description text.
     * @return description text.
     */
    public String getDescription() {
        return txtDescription().getText();
    }
    
    /** Selects document with given name in the list.
     * @param name name of document to be selected
     */    
    public void selectDocument(String name) {
        lstDocuments().selectItem(name);
    }
    
    /** Selects index-th document in the list.
     * @param index index of document to be selected
     */    
    public void selectDocument(int index) {
        lstDocuments().selectItem(index);
    }

    /** Selects documents with given names in the list.
     * @param names names of document to be selected
     */    
    public void selectDocuments(String[] names) {
        lstDocuments().selectItem(names);
    }

    /** Selects documents with given indexes in the list.
     * @param indexes indexes of documents to be selected
     */    
    public void selectDocuments(int[] indexes) {
        lstDocuments().selectItems(indexes);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btSwitchToDocument();
        btCloseDocuments();
        btSaveDocuments();
        btClose();
        txtDescription();
        lstDocuments();
    }
}
