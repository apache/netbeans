/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    @Override
    protected void caretPosChanged(int pos) {
        findOutputTab().caretPosChanged(pos);
    }

    @Override
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

    @Override
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
    
    
    @Override
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
                @Override
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
    
    @Override
    public boolean isWrapped() {
        if (getEditorKit() instanceof OutputEditorKit) {
            return getEditorKit() instanceof OutputEditorKit 
              && ((OutputEditorKit) getEditorKit()).isWrapped();
        } else {
            return NbPreferences.forModule(OutputPane.class).getBoolean("wrap", false); //NOI18N
        }
    }

    @Override
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
            @Override
            public void actionPerformed(ActionEvent arg0) {
                OutputDocument od =(OutputDocument)((JEditorPane)arg0.getSource()).getDocument();
                findOutputTab().inputSent(od.sendLine());
            }
        };
        result.getActionMap().put("SENDLINE", act);
        
        act = new AbstractAction() {
            @Override
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
