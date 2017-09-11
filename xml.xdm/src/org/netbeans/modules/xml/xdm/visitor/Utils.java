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

package org.netbeans.modules.xml.xdm.visitor;

import javax.swing.text.AbstractDocument;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.XMLSyntaxParser;
import org.w3c.dom.NodeList;

/**
 * Utils.java
 *
 * Created on November 16, 2004, 3:21 PM
 * @author mkuchtiak
 */
public class Utils {
    
    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     * 
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
  public static void replaceDocument(final javax.swing.text.Document doc, final String newDoc, String prefixMark)
  throws javax.swing.text.BadLocationException {
	  if (doc == null) {
		  return;
	  }
	  final String origDocument = doc.getText(0, doc.getLength());
	  final String newDocument = newDoc;
	  
	  if (origDocument.equals(newDocument)) {
		  // no change in document
		  return;
	  }
	  
	  final char[] origChars = origDocument.toCharArray();
	  final char[] newcChars = newDocument.toCharArray();
	  int tailIndex = origChars.length;
	  final int delta = newcChars.length - tailIndex;
	  int n = delta < 0 ? tailIndex + delta : tailIndex;
	  int offset;
	  for (offset = 0; offset < n; offset++) {
		  if (origChars[offset] != newcChars[offset]) {
			  break;
		  }
	  }
	  n = delta < 0 ? offset - delta : offset;
	  for (int i = tailIndex - 1; i >= n; i--) {
		  if (origChars[i] == newcChars[i + delta]) {
			  tailIndex = i;
		  } else {
			  break;
		  }
	  }
	  
	  final String s = newDocument.substring(offset, tailIndex + delta);
	  final int length = tailIndex - offset;
	  if (doc instanceof AbstractDocument) {
		  ((AbstractDocument) doc).replace(offset, length, s, null);
	  } else {
		  if (length > 0) {
			  doc.remove(offset, length);
		  }
		  if (s.length() > 0) {
			  doc.insertString(offset, s, null);
		  }
	  }
  }

    public static void replaceDocument(javax.swing.text.Document doc, String newDoc) throws javax.swing.text.BadLocationException {
        replaceDocument(doc,newDoc,null);
    }
    
    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    public static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        if (text.length==0) return "";
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i)
                    text[pos] = c;
                pos++;
            }
        }
        return new String(text, 0, pos);
    }

    public static BaseDocument loadDocument(String text) throws IOException {
        BaseDocument sd = new BaseDocument(true, "text/xml"); //NOI18N
        sd.putProperty(Language.class, XMLTokenId.language());
        try {
            sd.insertString(0, text, null);
            return sd;
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
    
    public static NodeList parseFragment(String text) throws IOException {
        StringBuilder sb = new StringBuilder(text.length()+20);
//        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<r>");
        sb.append(text);
        sb.append("</r>");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document dom = parser.parse(loadDocument(sb.toString()));
            return dom.getDocumentElement().getChildNodes();
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
    
}
