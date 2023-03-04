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
package org.netbeans.modules.java.hints.declarative.idebinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.Result;
import org.netbeans.modules.java.hints.infrastructure.JavaErrorProvider;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType=DeclarativeHintTokenId.MIME_TYPE, service=ErrorProvider.class)
public class HintsErrorProvider implements ErrorProvider {

    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        if (context.errorKind() == Kind.HINTS) {
            return Collections.emptyList();
        }

        List<Diagnostic> result = new ArrayList<>();

        try {
            ParserManager.parse(Collections.singletonList(Source.create(context.file())), new UserTask() {
                @Override
                public void run(ResultIterator it) throws Exception {
                    Result parseResult = ParserImpl.getResult(it.getParserResult());
                    if (parseResult != null) {
                        result.addAll(JavaErrorProvider.convert2Diagnostic(context.errorKind(), HintsTask.computeErrors(parseResult, it.getSnapshot().getText(), it.getSnapshot().getSource().getFileObject()), err -> true));
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

}
