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
package org.openide.explorer.propertysheet;

import java.awt.Rectangle;
import javax.accessibility.Accessible;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 *
 * @author stan
 */
final class ComboBoxAutoCompleteSupport {

    public static boolean install( JComboBox combo ) {
        boolean res = false;
        ComboBoxEditor comboEditor = combo.getEditor();
        if( comboEditor.getEditorComponent() instanceof JTextComponent ) {
            JTextComponent textEditor = ( JTextComponent ) comboEditor.getEditorComponent();
            Document doc = textEditor.getDocument();
            doc.addDocumentListener( new AutoCompleteListener( combo ) );
            setIgnoreSelectionEvents( combo, false );
            combo.setEditable( true );
            res = true;
        }
        combo.putClientProperty( "nb.combo.autocomplete", res ); //NOI18N
        return res;
    }

    static boolean isIgnoreSelectionEvents( JComboBox combo ) {
        Object res = combo.getClientProperty( "nb.combo.autocomplete.ignoreselection" ); //NOI18N
        
        return res instanceof Boolean && ((Boolean)res).booleanValue();
    }

    static void setIgnoreSelectionEvents( JComboBox combo, boolean ignore ) {
        combo.putClientProperty( "nb.combo.autocomplete.ignoreselection", ignore ); //NOI18N
    }

    static boolean isAutoCompleteInstalled( JComboBox combo ) {
        Object res = combo.getClientProperty( "nb.combo.autocomplete" ); //NOI18N

        return res instanceof Boolean && ((Boolean)res).booleanValue();
    }

    private static class AutoCompleteListener implements DocumentListener {

        private final JComboBox combo;

        public AutoCompleteListener( JComboBox combo ) {
            this.combo = combo;
        }
        
        @Override
        public void insertUpdate( DocumentEvent e ) {
            matchSelection( e );
        }

        @Override
        public void removeUpdate( DocumentEvent e ) {
            matchSelection( e );
        }

        @Override
        public void changedUpdate( DocumentEvent e ) {
            matchSelection( e );
        }
        private void matchSelection( DocumentEvent e ) {
            if( isIgnoreSelectionEvents( combo ) )
                return;
            try {
                setIgnoreSelectionEvents( combo, true );
                if( !combo.isDisplayable() )
                    return;
                String editorText;
                try {
                    editorText = e.getDocument().getText( 0, e.getDocument().getLength() );
                } catch( BadLocationException ex ) {
                    //ignore
                    return;
                }

                if( null != combo.getSelectedItem() && combo.getSelectedItem().toString().equals(editorText) )
                    return;

                if( !combo.isPopupVisible() ) {
                    combo.showPopup();
                }

                JList list = getPopupList( combo );
                if( null == list )
                    return;

                int matchIndex = findMatch( combo, editorText );

                if( matchIndex >= 0 ) {
                    list.setSelectedIndex( matchIndex );
                    Rectangle rect = list.getCellBounds(matchIndex, matchIndex);
                    if( null != rect )
                        list.scrollRectToVisible( rect );
                } else {
                    list.clearSelection();
                    list.scrollRectToVisible( new Rectangle( 1, 1 ) );
                }
            } finally {
                setIgnoreSelectionEvents( combo, false );
            }
        }
    }

    static int findMatch( JComboBox combo, String editorText ) {
        if( null == editorText || editorText.isEmpty() )
            return -1;

        for( int i=0; i<combo.getItemCount(); i++ ) {
            String item = combo.getItemAt( i ).toString();
            if( item.toLowerCase().compareTo( editorText ) == 0 ) {
                return i;
            }
        }

        for( int i=0; i<combo.getItemCount(); i++ ) {
            String item = combo.getItemAt( i ).toString();
            if( item.toLowerCase().startsWith( editorText ) ) {
                return i;
            }
        }
        return -1;
    }

    private static JList getPopupList( JComboBox combo ) {
        Accessible a = combo.getUI().getAccessibleChild(combo, 0);

        if( a instanceof ComboPopup ) {
            return ((ComboPopup) a).getList();
        }
        return null;
    }
}
