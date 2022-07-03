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
package org.netbeans.modules.javascript2.source.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Entlicher
 */
@ServiceProvider(service = SourceElementsQuery.class)
public class SourceElementsQueryImpl implements SourceElementsQuery {

    @Override
    public Collection<Var> getVarsAt(Source source, int offset) {
        final Collection<Var>[] vars = new Collection[] { Collections.emptyList()};
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                public @Override
                void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result r = resultIterator.getParserResult();
                    ParserResult pr = (ParserResult) r;
                    Model model = Model.getModel(pr, false);
                    if (model == null) {    // no model, no translation
                        return ;
                    }
                    Collection<? extends JsObject> variables = model.getVariables(offset);
                    Collection<Var> varpos = new ArrayList<>(variables.size());
                    for (JsObject var : variables) {
                        varpos.add(new Var(var.getName(), var.getOffset()));
                    }
                    vars[0] = varpos;
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return vars[0];
    }

    @Override
    public int getObjectOffsetAt(Source source, int offset) {
        final int[] objOffsetPtr = new int[] { -1 };
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                public @Override
                void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result r = resultIterator.getParserResult();
                    ParserResult pr = (ParserResult) r;
                    Model model = Model.getModel(pr, false);
                    if (model == null) {    // no model, no offset
                        return ;
                    }
                    JsObject declarationObject = model.getDeclarationObject(offset);
                    Identifier declarationName = declarationObject.getDeclarationName();
                    int doffset = declarationName.getOffsetRange().getStart();
                    objOffsetPtr[0] = doffset;
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return objOffsetPtr[0];
    }
    
}
