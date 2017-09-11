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
