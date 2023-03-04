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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
/** A custom editor for Strings.
*
* @author  Ian Formanek
* @version 1.00, Sep 21, 1998
*/
public class StringCustomEditor extends javax.swing.JPanel implements PropertyChangeListener {
    
    static final long serialVersionUID =7348579663907322425L;
    
    private static final int TOO_LARGE = 100*1024;
    
    boolean oneline=false;
    String instructions = null;

    private PropertyEnv env;

    private PropertyEditor editor;


    //enh 29294, provide one line editor on request
    /** Create a StringCustomEditor.
     * @param value the initial value for the string
     * @param editable whether to show the editor in read only or read-write mode
     * @param oneline whether the text component should be a single-line or multi-line component
     * @param instructions any instructions that should be displayed
     */
    StringCustomEditor (String value, boolean editable, boolean oneline, String instructions, PropertyEditor editor, PropertyEnv env) {
        this.oneline = oneline;
        this.instructions = instructions;
        this.env = env;
        this.editor = editor;

        this.env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        this.env.addPropertyChangeListener(this);

        init (value, editable);
   }
    
    /** Initializes the Form 
     * @deprecated Nothing should be using this constructor */
    @Deprecated
    public StringCustomEditor(String s, boolean editable) {
        init (s, editable);
    }
    
    private void init (String s, boolean editable) {
        setLayout (new java.awt.BorderLayout ());
        if (oneline) {
            textArea = new javax.swing.JTextField();
            add (textArea, BorderLayout.CENTER);
        } else {
            textAreaScroll = new javax.swing.JScrollPane ();
            textArea = new javax.swing.JTextArea ();
            textAreaScroll.setViewportView (textArea);
            add (textAreaScroll, BorderLayout.CENTER);
        }
        //original constructor code
        textArea.setEditable(editable);
        
        int from = 0;
        int to = 0;
        int ctn = 1;
        while ((to = s.indexOf ("\n", from)) > 0) {
            ctn ++;
            from = to + 1;
        }

        textArea.setText (s);
        if (textArea instanceof JTextArea && s.length () < TOO_LARGE) {
            ((JTextArea) textArea).setWrapStyleWord( true );
            ((JTextArea)textArea).setLineWrap( true );
            setPreferredSize (new java.awt.Dimension(500, 300));
        } else {
            textArea.setMinimumSize (new java.awt.Dimension (100, 20));
            if (textArea instanceof JTextArea) {
                //Some gargantuan string value - do something that will
                //show it.  Line wrap is off, otherwise it will spend 
                //minutes trying to calculate preferred size, etc.
                setPreferredSize (new java.awt.Dimension(500, 300));
            }
        }
        
        if ( !editable ) {
            // hack to fix #9219
            //TODO Fix this to use UIManager values, this is silly
            JTextField hack = new JTextField();
            hack.setEditable( false );
            textArea.setBackground( hack.getBackground() );
            textArea.setForeground( hack.getForeground() );
        }
        
        setBorder (BorderFactory.createEmptyBorder(12,12,0,11));
        
        textArea.getAccessibleContext().setAccessibleName(NbBundle.getBundle(StringCustomEditor.class).getString("ACS_TextArea")); //NOI18N
        if (instructions == null) {
            textArea.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(StringCustomEditor.class).getString("ACSD_TextArea")); //NOI18N
        } else {
            textArea.getAccessibleContext().setAccessibleDescription(instructions);
        }
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(StringCustomEditor.class).getString("ACSD_CustomStringEditor")); //NOI18N
        //Layout is not quite smart enough about text field along with variable
        //size text area
        int prefHeight;
        
        //IZ 44152, Debugger can produce 512K+ length strings, avoid excessive
        //iterations (which textArea.getPreferredSize() will definitely do)
        if (s.length () < TOO_LARGE) {
            prefHeight = textArea.getPreferredSize().height + 8;
        } else {
            prefHeight = ctn * 8;
        }
        
        if (instructions != null) {
            final JTextArea jta = new JTextArea(instructions);
            jta.setEditable (false);
            java.awt.Color c = UIManager.getColor("control");  //NOI18N
            if (c != null) {
                jta.setBackground (c);
            } else {
                jta.setBackground (getBackground());
            }
            jta.setLineWrap(true);
            jta.setWrapStyleWord(true);
            jta.setFont (getFont());
            add (jta, BorderLayout.NORTH, 0);
            jta.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(StringCustomEditor.class, 
                "ACS_Instructions")); //NOI18N
            jta.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(StringCustomEditor.class, 
                "ACSD_Instructions")); //NOI18N
            prefHeight += jta.getPreferredSize().height;
            //jlf guidelines - auto select text when clicked
            jta.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    jta.setSelectionStart(0);
                    jta.setSelectionEnd(jta.getText().length());
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    jta.setSelectionStart(0);
                    jta.setSelectionEnd(0);
                }
            });          
        }
        if (textArea instanceof JTextField) {
            setPreferredSize (new java.awt.Dimension (300, 
                prefHeight));
        }
    }

    @Override
    public void addNotify () {
        super.addNotify();
        //force focus to the editable area
        if (isEnabled() && isFocusable()) {
            textArea.requestFocus();
        }
    }

    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    private Object getPropertyValue () throws IllegalStateException {
        return textArea.getText ();
    }



    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    private javax.swing.JScrollPane textAreaScroll;
    private JTextComponent textArea;
}
