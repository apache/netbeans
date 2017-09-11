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
package org.netbeans.core.output2;

import java.awt.event.MouseEvent;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Caret;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.util.NbPreferences;


class OutputPane extends AbstractOutputPane {

    OutputTab parent;
    PropertyChangeListener editorKitListener = new OutputEditorKitListener();

    public OutputPane(OutputTab parent) {
        this.parent = parent;
    }

    @Override
    protected void documentChanged() {
        super.documentChanged();
        findOutputTab().documentChanged(this);
    }

    protected void caretPosChanged(int pos) {
        findOutputTab().caretPosChanged(pos);
    }

    protected void lineClicked(int line, int pos) {
        if ((getDocument() instanceof OutputDocument)) {
            findOutputTab().lineClicked(line, pos);
        }
    }

    @Override
    protected void enterPressed() {
        Caret caret = textView.getCaret();
        findOutputTab().enterPressed(caret.getMark(), caret.getDot());
    }

    protected void postPopupMenu(Point p, Component src) {
        if (src.isShowing()) {
            findOutputTab().postPopupMenu(p, src);
        }
    }
    
    /**
     * Only calls super if there are hyperlinks in the document to avoid huge
     * numbers of calls to viewToModel if the cursor is never going to be 
     * changed anyway.
     */
    @Override
    public void mouseMoved (MouseEvent evt) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            if (((OutputDocument) doc).getLines().hasListeners()) {
                super.mouseMoved(evt);
            }
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        //Refine possibly to focus just what is important..
        if (!e.isPopupTrigger()) {
            findOutputTab().setToFocus((Component) e.getSource());
            findOutputTab().requestActive();
        }
    }

    private OutputTab findOutputTab() {
        return parent;
    }

    @Override
    protected void setDocument (Document doc) {
        if (doc == null) {
            Document d = getDocument();
            if (d != null) {
                d.removeDocumentListener(this);
            }
            textView.setDocument (new PlainDocument());
            return;
        }
        textView.setEditorKit(new OutputEditorKit(isWrapped(), textView,
                editorKitListener));
        super.setDocument(doc);
        updateKeyBindings();
    }
    
    
    public void setWrapped (boolean val) {
        if (val != isWrapped() || !(getEditorKit() instanceof OutputEditorKit)) {
            NbPreferences.forModule(OutputPane.class).putBoolean("wrap", val); //NOI18N
            textView.setFont(OutputOptions.getDefault().getFont(val));
            final int pos = textView.getCaret().getDot();
            Cursor cursor = textView.getCursor();
            try {
                textView.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                setEditorKit(new OutputEditorKit(val, textView, editorKitListener));
            } finally {
                textView.setCursor (cursor);
            }
            /*if (val) { #78191
                getViewport().addChangeListener(this);
            } else {
                getViewport().removeChangeListener(this);
            }*/
            
            //Don't try to set the caret position until the view has
            //been fully readjusted to its new dimensions, scroll bounds, etc.
            SwingUtilities.invokeLater (new Runnable() {
                private boolean first = true;
                public void run() {
                    if (first) {
                        first = false;
                        SwingUtilities.invokeLater(this);
                        return;
                    }
                    textView.getCaret().setDot(pos);
                }
            });
            if (getDocument() instanceof OutputDocument && ((OutputDocument) getDocument()).getLines().isGrowing()) {
                lockScroll();
            }
            if (!val) {
                //If there are long lines, it will suddenly get scrolled to the right
                //with the non-wrapping editor kit, so fix that
                getHorizontalScrollBar().setValue(getHorizontalScrollBar().getModel().getMinimum());
            }
            validate();
            getFoldingSideBar().setWrapped(val);
        }
    }
    
    public boolean isWrapped() {
        if (getEditorKit() instanceof OutputEditorKit) {
            return getEditorKit() instanceof OutputEditorKit 
              && ((OutputEditorKit) getEditorKit()).isWrapped();
        } else {
            return NbPreferences.forModule(OutputPane.class).getBoolean("wrap", false); //NOI18N
        }
    }

    protected JEditorPane createTextView() {
        JEditorPane result = new JEditorPane();
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
            result.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        } else if ("GTK".equals(UIManager.getLookAndFeel().getID())) {
            result.setBackground(UIManager.getColor("text")); //NOI18N
        }

        // we don't want the background to be gray even though the text there is not editable
        result.setDisabledTextColor(result.getBackground());
        
        //#83118 - remove the "control shift 0" from editor pane to lt the Open Project action through
        InputMap map = result.getInputMap();
        MyInputMap myMap = new MyInputMap();
        myMap.setParent(map);
        result.setInputMap(JEditorPane.WHEN_FOCUSED, myMap);
        
        Action act = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                OutputDocument od =(OutputDocument)((JEditorPane)arg0.getSource()).getDocument();
                findOutputTab().inputSent(od.sendLine());
            }
        };
        result.getActionMap().put("SENDLINE", act);
        
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                OutputDocument od =(OutputDocument)((JEditorPane)arg0.getSource()).getDocument();
                findOutputTab().inputSent(od.sendLine());
                findOutputTab().inputEof();
            }
        };
        result.getActionMap().put("EOF", act);
        result.setDragEnabled(false); // #225994
        
        
        return result;
    }

    @Override
    protected void changeFontSizeBy(int change) {
        Controller.getDefault().changeFontSizeBy(change, isWrapped());
    }

    //#83118 - remove the "control shift 0" from editor pane to lt the Open Project action through
    protected class MyInputMap extends  InputMap {
        
        public MyInputMap() {
            super();
        }
        
        @Override
        public Object get(KeyStroke keyStroke) {
            KeyStroke stroke = KeyStroke.getKeyStroke("control shift O");
            if (keyStroke.equals(stroke)) {
                return null;
            }
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.CTRL_MASK);
            if (keyStroke.equals(stroke)) {
                return null;
            }
            
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            if (keyStroke.equals(stroke)) {
                if (findOutputTab().isInputVisible()) {/* #105954 */
                    return "SENDLINE";
                }
            }
            
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK);
            if (keyStroke.equals(stroke)) {
                return "EOF";
            }
            Object retValue;
            retValue = super.get(keyStroke);
            return retValue;
        }
        
    }

    private class OutputEditorKitListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("charsPerLine".equals(evt.getPropertyName())) { //NOI18N
                getFoldingSideBar().setCharsPerLine(
                        (Integer) evt.getNewValue());
            }
        }
    }
}
