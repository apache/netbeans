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
package org.netbeans.modules.xml.text.syntax;

import java.io.*;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;

import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.text.syntax.bridge.LegacySyntaxBridge;

/**
 * Editor kit implementation for xml content type.
 * It translates encoding used by document to Unicode encoding.
 * It makes sence for org.epenide.loaders.XMLDataObject that does
 * use default EditorSupport.
 *
 * @author Petr Kuzel
 */
public class UniKit extends NbEditorKit {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -940485353900594155L;
    
    /**
     * Read document.
     */
    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {

        // predetect it to get optimalized XmlReader if utf-8
        String enc = EncodingUtil.detectEncoding(in);
        if ( enc == null ) {
            enc = "UTF8"; //!!! // NOI18N
        }
        //    System.err.println("UniKit.reading as " + enc);
        Reader r = new InputStreamReader(in, enc);
        super.read(r, doc, pos);

    }

    /**
     * Hope that it is called by knowing (encoding).
     */  
    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        super.read(in, doc, pos);
    }

    /**
     * Write document.
     */
    @Override
    public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
        String enc = EncodingUtil.detectEncoding(doc);
        if ( enc == null ) {
            enc = "UTF8"; //!!! // NOI18N
        }
        //    System.err.println("UniKit.writing as " + enc);
        super.write( new OutputStreamWriter(out, enc), doc, pos, len);
    }

    /**
     * Hope that it is called by knowing (encoding).
     */  
    @Override
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        super.write(out, doc, pos, len);
    }

    @Override
    public Syntax createSyntax(Document doc) {
        Syntax syn = null;
        LegacySyntaxBridge bridge = MimeLookup.getLookup(getContentType()).lookup(LegacySyntaxBridge.class);
        if (bridge != null) {
            syn = bridge.createSyntax(this, doc, getContentType());
        }
        return syn != null ? syn : super.createSyntax(doc);
    }

    /** Create syntax support */
    @Override
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        SyntaxSupport syn = null;
        LegacySyntaxBridge bridge = MimeLookup.getLookup(getContentType()).lookup(LegacySyntaxBridge.class);
        if (bridge != null) {
            syn = bridge.createSyntaxSupport(this, doc, getContentType());
        }
        return syn != null ? syn : super.createSyntaxSupport(doc);
    }
    
}
