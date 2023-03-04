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
package org.netbeans.modules.xml.xdm.xam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Nam Nguyen
 */
@org.openide.util.lookup.ServiceProviders({@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider.class), @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.ModelAccessProvider.class)})
public class XDMAccessProvider implements DocumentModelAccessProvider {
    
    /** Creates a new instance of XDMAccessProvider */
    public XDMAccessProvider() {
    }

    public DocumentModelAccess createModelAccess(AbstractDocumentModel model) {
        return new XDMAccess(model);
    }
    
    public Document loadSwingDocument(InputStream in) throws IOException, BadLocationException {
        BaseDocument sd = new BaseDocument(true, "text/xml"); //NOI18N
        String encoding = in.markSupported() ? EncodingUtil.detectEncoding(in) : null;
        Reader r = encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding);
        sd.read(r, 0);
        return sd;
    }

    public Object getModelSourceKey(ModelSource source) {
        Object key = source.getLookup().lookup(DataObject.class);
        //Fix for IZ 112329: For referenced schemas in runtime catalog, there will be no DO,
        //hence we must return the Document as the key as an alternative.
        if(key != null)
            return key;
        return source.getLookup().lookup(Document.class);
    }
}
