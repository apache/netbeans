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

import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author lahvac
 */
public class ParserImpl extends Parser {

    private Snapshot snapshot;
    private DeclarativeHintsParser.Result result;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        this.snapshot = snapshot;
        this.result   = null;
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        if (this.result == null) {
            this.result = new DeclarativeHintsParser().parse(snapshot.getSource().getFileObject(),
                                                             snapshot.getText(),
                                                             snapshot.getTokenHierarchy().tokenSequence(DeclarativeHintTokenId.language()));
        }

        return new ResultImpl(snapshot, result);
    }

    @Override
    public void cancel() {}

    @Override
    public void addChangeListener(ChangeListener changeListener) {}

    @Override
    public void removeChangeListener(ChangeListener changeListener) {}

    private static final class ResultImpl extends Result {

        private final DeclarativeHintsParser.Result result;

        public ResultImpl(Snapshot _snapshot, DeclarativeHintsParser.Result result) {
            super(_snapshot);
            this.result = result;
        }

        @Override
        protected void invalidate() {
        }

    }

    @MimeRegistration(mimeType=DeclarativeHintTokenId.MIME_TYPE, service=ParserFactory.class)
    public static final class FactoryImpl extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new ParserImpl();
        }
    }

    public static DeclarativeHintsParser.Result getResult(Result r) {
        if (r instanceof ResultImpl) {
            return ((ResultImpl) r).result;
        }

        return null;
    }
}
