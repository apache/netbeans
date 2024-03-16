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
/*
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A class that handles the "File Name:" text field auto-completion drop-down selection list.
 *
 * @author Soot Phengsy
 */
public class FileCompletionPopup extends JPopupMenu implements KeyListener {
    
    private final JList<File> list;
    private final JTextField textField;
    private final JFileChooser chooser;
    
    public FileCompletionPopup(JFileChooser chooser, JTextField textField, Vector<File> files) {
        this.list = new JList<>(files);
        this.textField = textField;
        this.chooser = chooser;
        list.setVisibleRowCount(4);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane jsp = new JScrollPane(list);
        add(jsp);
        
        list.setFocusable(false);
        jsp.setFocusable(false);
        setFocusable(false);
        
        list.addFocusListener(new FocusHandler());
        list.addMouseListener(new MouseHandler());
        list.addMouseMotionListener(new MouseHandler());
        
        textField.addKeyListener(this);
    }
     
    public void setDataList(Vector<File> files) {
        list.setListData(files);
        ensureSelection();
    }

    void detach () {
        textField.removeKeyListener(this);
    }
    
    private void setSelectNext() {
        if (list.getModel().getSize() > 0) {
            int cur = (list.getSelectedIndex() + 1) % list.getModel().getSize();
            list.setSelectedIndex(cur);
            list.ensureIndexIsVisible(cur);
        }
    }
    
    private void setSelectPrevious() {
        if (list.getModel().getSize() > 0) {
            int cur = (list.getSelectedIndex() == -1) ? 0
                    : list.getSelectedIndex();
            cur = (cur == 0) ? list.getModel().getSize() - 1 : cur - 1;
            list.setSelectedIndex(cur);
            list.ensureIndexIsVisible(cur);
        }
    }
    
    public void showPopup(JTextComponent source, int x, int y) {
        if(list.getModel().getSize() == 0) {
            return;
        }
        setPreferredSize(new Dimension(source.getWidth(), source.getHeight() * 6));
        show(source, x, y);
        ensureSelection();
    }
    
    // #106268: always have some item selected for better usability
    private void ensureSelection () {
        if (list.getSelectedIndex() == -1 && (list.getModel().getSize() > 0)) {
            list.setSelectedIndex(0);
        }
    }
    
    private class FocusHandler extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
                setVisible(false);
                textField.requestFocus();
            }
        }
    }
    
    private class MouseHandler extends MouseAdapter implements MouseMotionListener{
        @Override
        public void mouseMoved(MouseEvent e) {
            if (e.getSource() == list) {
                Point location = e.getPoint();
                int index = list.locationToIndex(location);
                Rectangle r = new Rectangle();
                list.computeVisibleRect( r );
                if ( r.contains( location ) ) {
                    list.setSelectedIndex(index);
                }
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getSource() == list) {
                return;
            }
            if ( isVisible() ) {
                MouseEvent newEvent = convertMouseEvent( e );
                Rectangle r = new Rectangle();
                list.computeVisibleRect( r );
                Point location =  newEvent.getPoint();
                int index = list.locationToIndex(location);
                if ( r.contains( location ) ) {
                    list.setSelectedIndex(index);
                }
            }
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();
            int index = list.locationToIndex(p);
            list.setSelectedIndex(index);
            setVisible(false);
            File file = list.getSelectedValue();
            if (file == null) {
                return;
            }
            if(file.equals(chooser.getCurrentDirectory())) {
                chooser.firePropertyChange(JFileChooser.DIRECTORY_CHANGED_PROPERTY, false, true);
            } else {
                chooser.setCurrentDirectory(file);
            }
            textField.requestFocus();
        }
        
        private MouseEvent convertMouseEvent( MouseEvent e ) {
            Point convertedPoint = SwingUtilities.convertPoint( (Component)e.getSource(),
                    e.getPoint(), list );
            MouseEvent newEvent = new MouseEvent( (Component)e.getSource(),
                    e.getID(),
                    e.getWhen(),
                    e.getModifiersEx(),
                    convertedPoint.x,
                    convertedPoint.y,
                    e.getClickCount(),
                    e.isPopupTrigger() );
            return newEvent;
        }
    }

    /****** implementation of KeyListener of fileNameTextField ******/
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (!isVisible()) {
            return;
        }
        
        int code = e.getKeyCode();
        switch (code) {
        case KeyEvent.VK_DOWN:
            setSelectNext();
            e.consume();
            break;
        case KeyEvent.VK_UP:
            setSelectPrevious();
            e.consume();
            break;
        case KeyEvent.VK_ESCAPE:
            setVisible(false);
            textField.requestFocus();
            e.consume();
            break;
        }
        
        if (isCompletionKey(code, textField) && !e.isConsumed()) {
            File file = list.getSelectedValue();
            if(file != null) { 
                if(file.equals(chooser.getCurrentDirectory())) {
                    chooser.firePropertyChange(JFileChooser.DIRECTORY_CHANGED_PROPERTY, false, true);
                } else {
                    chooser.setSelectedFiles(new File[] {file});
                    chooser.setCurrentDirectory(file);
                }
                if (file.isDirectory()) {
                    try {
                        Document doc = textField.getDocument();
                        doc.insertString(doc.getLength(), File.separator, null);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(getClass().getName()).log(
                                Level.FINE, "Cannot append directory separator.", ex);
                    }
                }
            }
            setVisible(false);
            textField.requestFocus();
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // no operation
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // no operation
    }
    
    private boolean isCompletionKey (int keyCode, JTextField textField) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_TAB) {
            return true;
        }
        if (keyCode == KeyEvent.VK_RIGHT && 
                (textField.getCaretPosition() >= (textField.getDocument().getLength() - 1))) {
            return true;
        }
        
        return false;
    }
    
}
