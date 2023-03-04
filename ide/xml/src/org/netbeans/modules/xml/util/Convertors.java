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
