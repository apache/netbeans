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
package org.netbeans.modules.jshell.parsing;

import org.netbeans.modules.jshell.model.ConsoleModel;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
@EmbeddingProvider.Registration(
        mimeType = "text/x-repl", targetMimeType = "text/x-java")
public class ConsoleEmbeddingProvider extends EmbeddingProvider {
    
    @Override
    public void cancel() {
        
    }
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        Document d = snapshot.getSource().getDocument(false);
        if (d == null) {
            return Collections.emptyList();
        }
        ShellSession session = ShellSession.get(d);
        if (session == null) {
            return Collections.emptyList();
        }
        List[] res = new List[1];
        try {
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ConsoleContents cts = ConsoleContents.get(resultIterator);
                    if (cts == null) {
                        res[0] = Collections.emptyList();
                        return;
                    }
                    ConsoleSection inputSection = cts.getInputSection();
                    EmbeddingProcessor p = new EmbeddingProcessor(session, cts, snapshot, inputSection);
                    res[0] = p.process();
                }
                
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return (List<Embedding>)res[0];
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
}
