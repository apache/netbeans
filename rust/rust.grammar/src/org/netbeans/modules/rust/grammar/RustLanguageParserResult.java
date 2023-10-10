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
package org.netbeans.modules.rust.grammar;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.rust.grammar.ast.RustAST;
import org.openide.filesystems.FileObject;

/**
 *
 * @author antonio
 */
public class RustLanguageParserResult extends ParserResult {

    private volatile RustAST ast;
    private volatile boolean finished = false;

    public RustLanguageParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    public RustLanguageParserResult parse() {
        if (!finished) {
            FileObject fileObject = getSnapshot().getSource().getFileObject();
            CharSequence text = getSnapshot().getText();
            this.ast = RustAST.parse(fileObject, text);
            finished = true;
        }
        return this;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return ast == null ? Collections.emptyList() : ast.getErrors();
    }

    void cancel() {
        if (this.ast != null && ! finished) {
            this.ast.cancel();
        }
    }

    @Override
    protected void invalidate() {
    }

//    List<? extends StructureItem> getStructureItems() {
//        if (this.ast == null || ! finished) {
//            return Collections.emptyList();
//        }
//        ArrayList<StructureItem> structureItems = new ArrayList<>();
//        RustASTNode node = ast.getCrate();
//
//
//        return structureItems;
//    }

    public RustAST getAST() {
        return this.ast;
    }

}
