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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


