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
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;

import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class DTDFormatter extends ExtFormatter {

    //
    // init
    //

    /** */
    public DTDFormatter (Class kitClass) {
        super (kitClass);
    }


    //
    // ExtFormatter
    //

    /**
     */
    public FormatSupport createFormatSupport (FormatWriter fw) {
        return new XMLFormatSupport (fw);
    }

    /**
     */
    protected boolean acceptSyntax (Syntax syntax) {
        return (syntax instanceof JJEditorSyntax);
    }

    /**
     */
    protected void initFormatLayers() {
        addFormatLayer (new StripEndWhitespaceLayer());
    }    

   /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public int indentNewLine (Document doc, int offset) {
//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\n+ XMLFormatter::indentNewLine: doc = " + doc); // NOI18N
//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+             ::indentNewLine: offset = " + offset); // NOI18N

        if (doc instanceof BaseDocument) {
            BaseDocument bdoc = (BaseDocument)doc;

            bdoc.atomicLock();
            try {
                bdoc.insertString (offset, "\n", null); // NOI18N
                offset++;

                int fullLine = Utilities.getFirstNonWhiteBwd (bdoc, offset - 1);
                int indent = Utilities.getRowIndent (bdoc, fullLine);
                
//                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+             ::indentNewLine: fullLine = " + fullLine); // NOI18N
//                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+             ::indentNewLine: indent   = " + indent); // NOI18N
//
//                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+             ::indentNewLine: offset   = " + offset); // NOI18N
//                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+             ::indentNewLine: sb       = '" + sb.toString() + "'"); // NOI18N

                String indentation = getIndentString(bdoc, indent);
                bdoc.insertString (offset, indentation, null);
                offset += indentation.length();

//                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+             ::indentNewLine: offset = " + offset); // NOI18N
            } catch (BadLocationException e) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            } finally {
                bdoc.atomicUnlock();
            }

            return offset;
        }

        return super.indentNewLine (doc, offset);
    }

    
    //
    // class StripEndWhitespaceLayer
    //

    /**
     *
     */
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {
       
        //
        // init
        //

        /** */
        public StripEndWhitespaceLayer() {
            super("xml-strip-whitespace-at-line-end-layer"); // NOI18N
        }
        
        //
        // AbstractFormatLayer
        //

        /**
         */
        protected FormatSupport createFormatSupport (FormatWriter fw) {
            return new XMLFormatSupport (fw);
        }
        
        /**
         */
        public void format (FormatWriter fw) {
            XMLFormatSupport xfs = (XMLFormatSupport)createFormatSupport (fw);
            
            FormatTokenPosition pos = xfs.getFormatStartPosition();
            
            if ( (xfs.isLineStart (pos) == false) ||
                 xfs.isIndentOnly() ) { // don't do anything
                
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLFormatSupport::StripEndWhitespaceLayer::format: position = " + pos); // NOI18N

                    pos = xfs.removeLineEndWhitespace (pos);
                    if (pos.getToken() != null) {
                        pos = xfs.getNextPosition (pos);
                    }
                }
            }
        }

    } // end: class StripEndWhitespaceLayer

}
