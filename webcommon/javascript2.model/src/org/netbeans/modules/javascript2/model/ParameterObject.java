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
package org.netbeans.modules.javascript2.model;

import java.util.Collections;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationPrinter;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class ParameterObject extends JsObjectImpl {

    public ParameterObject(JsObject parent, Identifier name, String mimeType, String sourceLabel) {
        super(parent, name, name.getOffsetRange(), mimeType, sourceLabel);
        if (hasExactName()) {
            addOccurrence(name.getOffsetRange());
        }
    }

    @Override
    public boolean isDeclared() {
        return true;
    }

    @Override
    public Kind getJSKind() {
        return JsElement.Kind.PARAMETER;
    }

    @Override
    public Documentation getDocumentation() {
        final String[] result = new String[1];
        try {
            ParserManager.parse(Collections.singleton(Source.create(getParent().getFileObject())), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result parserResult = resultIterator.getParserResult(getParent().getOffset());
                    if (parserResult instanceof ParserResult) {
                        JsDocumentationHolder holder = JsDocumentationSupport.getDocumentationHolder((ParserResult) parserResult);
                        JsComment comment = holder.getCommentForOffset(getParent().getOffset(), holder.getCommentBlocks());
                        if (comment != null) {
                            for (DocParameter docParameter : comment.getParameters()) {
                                if (docParameter.getParamName().getName().equals(getDeclarationName().getName())) {
                                    result[0] = JsDocumentationPrinter.printParameterDocumentation(docParameter);
                                    return;
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result[0] != null && !result[0].isEmpty()) {
            return Documentation.create(result[0]);
        }
        return null;
    }
}
