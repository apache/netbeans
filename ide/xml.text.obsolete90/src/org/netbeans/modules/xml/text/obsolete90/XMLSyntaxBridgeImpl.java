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
package org.netbeans.modules.xml.text.obsolete90;

import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.xml.text.syntax.DTDKit;
import org.netbeans.modules.xml.text.syntax.DTDSyntaxTokenMapper;
import org.netbeans.modules.xml.text.syntax.DTDTokenContext;
import org.netbeans.modules.xml.text.syntax.ENTKit;
import org.netbeans.modules.xml.text.syntax.XMLDefaultSyntax;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.bridge.LegacySyntaxBridge;
import org.netbeans.modules.xml.text.syntax.javacc.DTDSyntaxTokenManager;
import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;

/**
 *
 * @author sdedic
 */
@MimeRegistrations({
    @MimeRegistration(service = LegacySyntaxBridge.class, mimeType = "text/xml"),
    @MimeRegistration(service = LegacySyntaxBridge.class, mimeType = "application/xml-dtd"),
    @MimeRegistration(service = LegacySyntaxBridge.class, mimeType = "text/xml-external-parsed-entity")
})
public class XMLSyntaxBridgeImpl implements LegacySyntaxBridge {

    @Override
    public org.netbeans.editor.Syntax createSyntax(EditorKit host, Document doc, String mimeType) {
        if (DTDKit.MIME_TYPE.equals(mimeType)) {
            return new JJEditorSyntax( 
                new DTDSyntaxTokenManager(null).new Bridge(),
                new DTDSyntaxTokenMapper(),
                DTDTokenContext.contextPath
            );

        } else if (XMLKit.MIME_TYPE.equals(mimeType)) {
            return new XMLDefaultSyntax();
        } else if (ENTKit.MIME_TYPE.equals(mimeType)) {
            return new XMLDefaultSyntax();
        } else {
            return null;
        }
    }

    @Override
    public SyntaxSupport createSyntaxSupport(EditorKit host, Document doc, String mimeType) {
        if (XMLKit.MIME_TYPE.equals(mimeType) || ENTKit.MIME_TYPE.equals(mimeType)) {
            return new XMLSyntaxSupport((BaseDocument)doc);
        } else {
            return null;
        }
    }
}
