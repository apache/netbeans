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
