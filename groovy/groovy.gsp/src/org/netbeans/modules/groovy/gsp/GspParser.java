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
package org.netbeans.modules.groovy.gsp;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;


/**
 * just fake class, we need the parser and the StructureScanner to enable 
 * navigator of embedded languages
 *
 * @author marek
 */
public class GspParser extends Parser {

    private Result fakeResult;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        fakeResult = new FakeParserResult(snapshot);
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return fakeResult;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        //do nothing
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        //do nothing
    }

    private static class FakeParserResult extends ParserResult {

        public FakeParserResult(Snapshot s) {
            super(s);
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            return Collections.emptyList();
        }

        @Override
        protected void invalidate() {
            //do nothing
        }

    }

}
