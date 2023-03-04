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
package org.netbeans.modules.html.editor;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author marekfukala
 */
public class Utils {

    /**
     * Saves the given document to its underlying {@link FileObject} if the document
     * is not opened in the nb editor, more formally if EditorCookie.getOpenedPanes() == null.
     * 
     * @param document
     * @throws IOException 
     */
    public static void saveDocumentIfNotOpened(Document document) throws IOException {

        Object o = document.getProperty(Document.StreamDescriptionProperty);
        if (!(o instanceof DataObject)) {
            return;
        }
        DataObject dobj = (DataObject) o;
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if (ec != null && ec.getOpenedPanes() == null) {
            //file not open in any editor
            SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
            if (save != null) {
                save.save();
            }
        }
    }
    
    /**
     * Gets a {@link Document} instance for the given {@link FileObject}.
     * 
     * Formally it does EditorCookie.openDocument().
     * 
     * @param file
     * @return 
     */
    public static Document getDocument(FileObject file) {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getLookup().lookup(EditorCookie.class);

            if (ec == null) {
                return null;
            }
            return ec.openDocument();
        } catch (IOException e) {
            return null;
        }
    }
    
        /**
     * Creates a new {@link Model} instance for the given {@link Source}.
     * 
     * @since 1.3
     * @param source
     * @return
     * @throws ParseException 
     */
    public static Model createCssSourceModel(Source source) throws ParseException {
        final AtomicReference<Model> model_ref = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css");
                if(ri != null) {
                    CssParserResult result = (CssParserResult)ri.getParserResult();
                    model_ref.set(Model.getModel(result));
                }
                
            }
        });
        return model_ref.get();
    }
    
    public static boolean isAttributeValueQuoted(CharSequence value) {
        if (value == null) {
            return false;
        }
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }
}
