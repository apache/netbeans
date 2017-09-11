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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/** Class implementing all necessary methods for handling "Help" Frame.
 * Normally the Help window is a JFrame.
 * But the Help window can be transformed to a JDialog 
 * when another modal dialog is shown. This operator can handle both states.
 *
 * @see HelpAction
 * @author Adam.Sotona@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class HelpOperator extends WindowOperator {

    /** Creates new HelpOperator that can handle it. It tries to find a window
     * which contains some javax.help.JHelp* sub component.
     * It throws TimeoutExpiredException when window is not found
     */
    public HelpOperator() {
        super(WindowOperator.waitWindow(new HelpWindowChooser()));
    }

    /** Creates new HelpOperator that can handle it. It tries to find a window
     * which contains some javax.help.JHelp* sub component and the window
     * has given title.
     * It throws TimeoutExpiredException when JFrame not found
     * @param title String help frame title 
     */
    public HelpOperator(String title) {
        super(WindowOperator.waitWindow(new HelpWindowChooser(title)));
    }

    private static final HelpAction helpAction = new HelpAction();
    
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btPrint;
    private JButtonOperator _btPageSetup;
    private JSplitPaneOperator _splpHelpSplitPane;
    private JTabbedPaneOperator _tbpHelpTabPane;
    private JTreeOperator _treeContents;
    private JTreeOperator _treeSearch;
    private JTextFieldOperator _txtSearchFind;
    private JEditorPaneOperator _txtContentViewer;

    /** Returns title of help window. The help window can be either JFrame
     * or JDialog.
     * @return title of help window
     */
    public String getTitle() {
        if(getSource() instanceof Frame) {
            return ((Frame)getSource()).getTitle();
        } else {
            return ((Dialog)getSource()).getTitle();
        }
    }
    
    /** invokes default help
     * @return HelpOperator for invoked help */    
    public static HelpOperator invoke() {
        helpAction.perform();
        return new HelpOperator();
    }

    /** invokes help with defined help set
     * @param helpSet String help set name
     * @return HelpOperator for invoked help */
    /* Help set submenu not used for NetBeans 3.6.
    public static HelpOperator invoke(String helpSet) {
        new HelpAction(helpSet).perform();
        return new HelpOperator(helpSet);
    }
     */

    /** Tries to find "" JButton in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, helpButtonChooser, 0);
        }
        return _btBack;
    }

    /** Tries to find "" JButton in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, helpButtonChooser, 1);
        }
        return _btNext;
    }

    /** Tries to find "" JButton in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btPrint() {
        if (_btPrint==null) {
            _btPrint = new JButtonOperator(this, helpButtonChooser, 2);
        }
        return _btPrint;
    }

    /** Tries to find "" JSplitPaneOperator in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JSplitPaneOperator splpHelpSplitPane() {
        if (_splpHelpSplitPane==null) {
            _splpHelpSplitPane = new JSplitPaneOperator( this );
        }
        return _splpHelpSplitPane;
    }

    /** Tries to find "" JTabbedPane in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JTabbedPaneOperator tbpHelpTabPane() {
        if (_tbpHelpTabPane==null) {
            _tbpHelpTabPane = new JTabbedPaneOperator( splpHelpSplitPane() );
        }
        return _tbpHelpTabPane;
    }

    /** Tries to find "" JButton in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btPageSetup() {
        if (_btPageSetup==null) {
            _btPageSetup = new JButtonOperator(this, helpButtonChooser, 3);
        }
        return _btPageSetup;
    }

    /** Tries to find JTree in Contents tab of this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JTreeOperator
     */
    public JTreeOperator treeContents() {
        selectPageContents();
        if (_treeContents==null) {
            _treeContents = new JTreeOperator( tbpHelpTabPane(), 0 );
        }
        return _treeContents;
    }

    /** Tries to find JTree in Search tab of this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JTreeOperator
     */
    public JTreeOperator treeSearch() {
        selectPageSearch();
        if (_treeSearch==null) {
            _treeSearch = new JTreeOperator( tbpHelpTabPane(), 0 );
        }
        return _treeSearch;
    }

    /** Tries to find JTextField Find in Search tab of this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtSearchFind() {
        selectPageSearch();
        if (_txtSearchFind==null) {
            _txtSearchFind = new JTextFieldOperator( tbpHelpTabPane(), 0 );
        }
        return _txtSearchFind;
    }

    /** Tries to find null BasicContentViewerUI$JHEditorPane in this dialog.
     * It throws TimeoutExpiredException when component not found
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtContentViewer() {
        if (_txtContentViewer==null) {
            _txtContentViewer = new JEditorPaneOperator( splpHelpSplitPane(), 0 );
        }
        return _txtContentViewer;
    }

    /** clicks on "Back" JButton
     * It throws TimeoutExpiredException when MetalSplitPaneDivider$1 not found
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next" JButton
     * It throws TimeoutExpiredException when JButton not found
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Print" JButton
     * It throws TimeoutExpiredException when JButton not found
     */
    public void print() {
        btPrint().push();
    }

    /** clicks on "Page Setup" JButton
     * It throws TimeoutExpiredException when JButton not found
     */
    public void pageSetup() {
        btPageSetup().pushNoBlock();
    }

    /** selects page Contents */    
    public void selectPageContents() {
        tbpHelpTabPane().selectPage(0);
    }
    
    /** selects page Search */    
    public void selectPageSearch() {
        tbpHelpTabPane().selectPage(1);
    }

    /** tries to find and set text of txtSearchFind
     * @param text String text
     */
    public void searchFind( String text ) {
        txtSearchFind().enterText(text);
    }

    /** returns help content in plain text form
     * @return String text of help
     */
    public String getContentText() {
        return txtContentViewer().getText();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void close() {
        requestClose();
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btBack();
        btNext();
        btPageSetup();
        btPrint();
        treeContents();
        txtContentViewer();
        treeSearch();
        txtSearchFind();
    }
    
    /** Implementation of ComponentChooser to choose component which 
     * is instance of javax.help.JHelp*. */
    private static final ComponentChooser jHelpChooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().startsWith("javax.help.JHelp");
        }
        public String getDescription() {
            return("any javax.help");
        }
    };

    /** Compare title of window with given pattern. */
    private static boolean compareTitle(WindowOperator oper, String expectedTitle) {
        String title;
        if(oper.getSource() instanceof Frame) {
            title = ((Frame)oper.getSource()).getTitle();
        } else {
            title = ((Dialog)oper.getSource()).getTitle();
        }
        return oper.getComparator().equals(title, expectedTitle);
    }
    
    /** SubChooser to determine Window which contains some 
     *  javax.help.JHelp* sub component.
     */
    private static final class HelpWindowChooser implements ComponentChooser {
        
        private String title;
        
        public HelpWindowChooser() {
        }
        
        public HelpWindowChooser(String title) {
            this.title = title;
        }
        
        public boolean checkComponent(Component comp) {
            WindowOperator winOper = new WindowOperator((Window)comp);
            winOper.setOutput(TestOut.getNullOutput());
            if(winOper.findSubComponent(jHelpChooser) != null) {
                if(title != null) {
                    return compareTitle(winOper, title);
                } else {
                    return true;
                }
            } 
            return false;
        }
        
        public String getDescription() {
            return "containing any javax.help.JHelp component"+
                    (title == null ? "" : " and with title '"+title+"'");
        }
    }
    
    /** SubChooser to find HelpButton in help window. */
    private static final ComponentChooser helpButtonChooser = new ComponentChooser() {
        
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("HelpButton");
        }
        
        public String getDescription() {
            return "HelpButton";
        }
    };
}

