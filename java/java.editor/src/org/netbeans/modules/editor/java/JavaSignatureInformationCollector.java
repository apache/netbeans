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
package org.netbeans.modules.editor.java;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.SignatureInformation;
import org.netbeans.modules.java.completion.JavaTooltipTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.SignatureInformationCollector;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = SignatureInformationCollector.class)
public final class JavaSignatureInformationCollector implements SignatureInformationCollector {

    @Override
    public void collectSignatureInformation(Document doc, int offset, SignatureInformation.Context context, Consumer<SignatureInformation> consumer) {
        if (context == null || context.getTriggerKind() != SignatureInformation.TriggerKind.TriggerCharacter || context.getTriggerCharacter() == '(') {
            try {
                JavaTooltipTask task = JavaTooltipTask.create(offset, () -> false);
                ParserManager.parse(Collections.singletonList(Source.create(doc)), task);
                if (task.getTooltipData() != null && task.getTooltipSignatures() != null) {
                    Iterator<List<String>> it = task.getTooltipData().iterator();
                    for (int i = 0; i < task.getTooltipSignatures().size() && it.hasNext(); i++) {
                        List<String> params = it.next();
                        String signature = task.getTooltipSignatures().get(i);
                        Builder builder = SignatureInformationCollector.newBuilder(signature, i == task.getActiveSignatureIndex());
                        for (int j = 0; j < params.size(); j++) {
                            String param = params.get(j);
                            builder.addParameter(param, j == task.getTooltipIndex(), null);
                        }
                        consumer.accept(builder.build());
                    }
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
