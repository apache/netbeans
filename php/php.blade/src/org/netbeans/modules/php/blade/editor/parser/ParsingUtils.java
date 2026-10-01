/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.editor.parser;

/**
 *
 * @author bogdan
 */
import java.io.IOException;
import java.util.Collections;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

/**
 *
 * @author bogdan
 */
public final class ParsingUtils {

    public ParsingUtils() {

    }

    private PHPParseResult phpParserResult;
    
    public BaseDocument createPhpBaseDocument(String content) {
        String mimeType = "text/x-php5"; //NOI18N
        try {
            BaseDocument doc = new BaseDocument(true, mimeType) {
                @Override
                public boolean isIdentifierPart(char ch) {
                    return super.isIdentifierPart(ch);
                }
            };

            doc.putProperty("mimeType", mimeType); //NOI18N
            doc.insertString(0,content, null);

            return doc;
        } catch (BadLocationException ex) {
            return null;
        }
    }

    public void parseFileObject(FileObject file) {
        Document doc = openDocument(file);

        try {
            Source source = Source.create(doc);

            if (source == null) {
                return;
            }

            Document sourceDoc = source.getDocument(false);

            if (sourceDoc == null) {
                return;
            }

            source.createSnapshot();
            ParserManager.parseWhenScanFinished(Collections.singletonList(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result parserResult = resultIterator.getParserResult();
                    if (parserResult instanceof PHPParseResult) {
                        phpParserResult = (PHPParseResult) parserResult;
                    }
                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void laterParseFileObject(FileObject file) {
        Document doc = openDocument(file);

        try {
            Source source = Source.create(doc);

            if (source == null) {
                return;
            }

            Document sourceDoc = source.getDocument(false);

            if (sourceDoc == null) {
                return;
            }

            source.createSnapshot();
            ParserManager.parseWhenScanFinished(Collections.singletonList(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result parserResult = resultIterator.getParserResult();
                    if (parserResult instanceof PHPParseResult) {
                        phpParserResult = (PHPParseResult) parserResult;
                    }
                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public PHPParseResult getParserResult() {
        return phpParserResult;
    }

    private Document openDocument(FileObject f) {
        try {
            DataObject dataObject = DataObject.find(f);
            EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
            return ec.openDocument();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }
}