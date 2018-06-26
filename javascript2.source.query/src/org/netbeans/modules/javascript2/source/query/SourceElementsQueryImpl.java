/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
        final Collection[] vars = new Collection[] { Collections.EMPTY_LIST };
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
