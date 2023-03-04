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
/*
 * AutoCompleteDocument.java
 *
 * Created on September 5, 2006, 4:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;

/**
 *
 * @author John Baker
 */
public class QueryBuilderSqlCompletion extends DefaultStyledDocument {

    private List dictionary = new ArrayList();
    private JTextComponent comp;
    private int charCount = -1;
    private int lastOffset = 0;

    public QueryBuilderSqlCompletion( JTextComponent field, String[] aDictionary ) {
        comp = field;
        dictionary.addAll( Arrays.asList( aDictionary ) );
    }

    public void addDictionaryEntry( String item ) {
        dictionary.add( item );
    }

    /**
     * Insert text that is matched by the sequence of keys typed in the QueryBuilderSqlTextArea
     **/
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        super.insertString( offs, str, a );

        if ((offs + charCount) <=  lastOffset) // caret currently precedes or equals position of the last offset
            charCount = 0; // if cursor moved reset character count (may not be needed)
        else
            charCount ++; // contiguous chars

        String charTyped = getText(offs - charCount , this.comp.getCaretPosition() - (offs - charCount)   );
        String word = completeText( charTyped );

        // do the completion if the keys typed form the sequence of a matching word
        if( word != null ) {
            super.insertString( offs + str.length(), word, a );
            comp.setCaretPosition( offs + str.length() );
            comp.moveCaretPosition( offs + word.length()+1 );
        } else {
            comp.setCaretPosition( offs + str.length() );

            if (charCount >= 0)
                charCount--; // if no matching character, reset
        }

        // save the starting position in case the caret location is moved behind the previous starting cursor position
        lastOffset = offs;
    }

    // Compare prefix of chars typed with  sqlReservedWords from QueryBuilderSqlTextArea
    public String completeText( String text ) {
        for( Iterator i = dictionary.iterator(); i.hasNext(); ) {
            String word = (String) i.next();
            if( word.startsWith( text ) ) {
                return word.substring( text.length() );
            } else if (word.startsWith(text.toUpperCase()))
                return word.substring( text.length() ).toLowerCase();
        }
        return null;
    }

}


