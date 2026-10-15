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
package org.netbeans.modules.rust.grammar.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.rust.grammar.RustLanguageParserResult;
import org.netbeans.modules.rust.grammar.ast.RustAST;
import org.netbeans.modules.rust.grammar.ast.RustASTNode;
import org.netbeans.modules.rust.grammar.folding.RustFoldingScanner;

/**
 *
 */
public final class RustStructureScanner implements StructureScanner {

    private static final Logger LOG = Logger.getLogger(RustStructureScanner.class.getName());
    private static final Level LOGLEVEL = Level.FINE;

    /**
     * Factory method
     *
     * @return an instance of this class.
     */
    public static RustStructureScanner create() {
        return new RustStructureScanner();
    }

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        if (info instanceof RustLanguageParserResult) {
            RustAST ast = ((RustLanguageParserResult) info).getAST();
            if (ast != null) {
                return createStructureFromAST(ast);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        if (info instanceof RustLanguageParserResult) {
            return RustFoldingScanner.create().folds((RustLanguageParserResult) info);
        }
        return Collections.emptyMap();
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }

    private List<? extends StructureItem> createStructureFromAST(final RustAST ast) {
        RustASTNode crate = ast.getCrate();
        if (crate == null) {
            return Collections.emptyList();
        }
        return recursivelyCreateStructureFromNode(ast, crate);
    }

    private static List<RustStructureItem> recursivelyCreateStructureFromNode(final RustAST ast, final RustASTNode root) {
        if (root == null) {
            return Collections.emptyList();
        }
        ArrayList<RustStructureItem> items = new ArrayList<>();

        Consumer<RustASTNode> adder = (node) -> {
            RustStructureItem item = new RustStructureItem(ast, node);
            items.add(item);
            item.getChildren().addAll(recursivelyCreateStructureFromNode(ast, node));
        };

        LOG.log(LOGLEVEL, String.format("Visiting child %s", root.toString()));

        root.enums().forEach(adder);
        root.functions().forEach(adder);
        root.macros().forEach(adder);
        root.structs().forEach(adder);
        root.traits().forEach(adder);
        root.impls().forEach(adder);
        root.modules().forEach(adder);

        return items;
    }

}
