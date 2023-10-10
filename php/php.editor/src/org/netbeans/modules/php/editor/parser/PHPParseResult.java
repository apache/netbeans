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

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Model.Type;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.parser.astnodes.Program;


/**
 *
 * @author Petr Pisl
 */
public class PHPParseResult extends ParserResult {

    private final Program root;
    private List<Error> errors;
    private Model model;

    public PHPParseResult(Snapshot snapshot, Program rootNode) {
        super(snapshot);
        this.root = rootNode;
        this.errors = Collections.<Error>emptyList();
    }

    public Program getProgram() {
        return root;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return new ArrayList<>(errors);
    }

    /**
     * Returns extended model.
     *
     * @return
     */
    public synchronized Model getModel() {
        return getModel(Type.EXTENDED);
    }

    /**
     * @deprecated Use {@link #getModel(org.netbeans.modules.php.editor.model.Model.Type)} instead.
     */
    @Deprecated
    public synchronized Model getModel(boolean extended) {
        return extended ? getModel(Type.EXTENDED) : getModel(Type.COMMON);
    }

    public synchronized Model getModel(Type type) {
        if (model == null) {
            model = ModelFactory.getModel(this);
        }
        type.process(model);
        return model;
    }

    @Override
    protected void invalidate() {
        // comments copied from Groovy:
        // FIXME parsing API
        // remove from parser cache (?)
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public OffsetRange getErrorRange() {
        OffsetRange result = OffsetRange.NONE;
        for (org.netbeans.modules.csl.api.Error error : getDiagnostics()) {
            if (error.getSeverity() == Severity.ERROR) {
                result = new OffsetRange(error.getStartPosition(), error.getEndPosition());
                break;
            }
        }
        return result;
    }

}
