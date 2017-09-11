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
package org.netbeans.modules.xml.util;

import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;

import javax.swing.text.Document;

import org.xml.sax.InputSource;

import org.netbeans.modules.xml.text.TextEditorSupport;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import java.net.URL;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Set of static methods converting misc data representations.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public final class Convertors {


    /**
     * @return current state of Document as string
     */
    public static String documentToString(final Document doc) {

        if (doc == null) throw new NullPointerException();
        
        final String[] str = new String[1];

        // safely take the text from the document
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    e.printStackTrace();
                }
            }
        };

        doc.render(run);
        return str[0];
        
    }
    
    /**
     * @return InputSource, a callie SHOULD set systemId if available
     */
    public static InputSource documentToInputSource(Document doc) {
        
        if (doc == null) throw new NullPointerException();
        
        String text = documentToString(doc); 
        Reader reader = new StringReader(text);
        
        // our specifics property
        String system = (String) doc.getProperty(TextEditorSupport.PROP_DOCUMENT_URL);
        
        // try Swing general property
        if (system == null) {
            Object obj = doc.getProperty(Document.StreamDescriptionProperty);        
            if (obj instanceof DataObject) {
                try { 
                        DataObject dobj = (DataObject) obj;
                        FileObject fo = dobj.getPrimaryFile();
                        URL url = fo.getURL();
                        system = url.toExternalForm();
                } catch (IOException io) {
                    ErrorManager emgr = Lookup.getDefault().lookup(ErrorManager.class);
                    emgr.notify(io);
                }
            } else {
                ErrorManager emgr = Lookup.getDefault().lookup(ErrorManager.class);
                emgr.log("XML:Convertors:Unknown stream description:" + obj);
            }
        }

        // set something, some parsers are nervous if no system id
        if (system == null) {
            system = "XML/Convertors/documentToInputSource()";  //NOI18N
        }            
        
        InputSource in = new InputSource(system); // NOI18N
        in.setCharacterStream(reader);
        return in;
    }


    /**
     * Wrap reader into buffered one and start reading returning
     * String as a EOF is reached.
     */
    public static String readerToString(Reader reader) throws IOException {
        
        BufferedReader fastReader = new BufferedReader(reader);
        StringBuffer buf = new StringBuffer(1024);
        try {
            for (int i = fastReader.read(); i >= 0; i = fastReader.read()) {
                buf.append((char)i);                            
            }
        } catch (EOFException eof) {
            //expected
        }

        return buf.toString();        
    }

}
