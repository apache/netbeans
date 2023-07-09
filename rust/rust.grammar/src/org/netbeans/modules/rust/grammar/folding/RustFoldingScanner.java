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
package org.netbeans.modules.rust.grammar.folding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.rust.grammar.RustLanguageParserResult;
import org.netbeans.modules.rust.grammar.ast.RustAST;
import org.netbeans.modules.rust.grammar.ast.RustASTNode;
import org.openide.util.NbBundle;

/**
 * Defines types of Rust codeblockFolds and scans for codeblockFolds.
 */
public final class RustFoldingScanner {

    private static String getBundleString(String key) {
        return NbBundle.getMessage(RustFoldingScanner.class, key);
    }

    /**
     * Factory method
     *
     * @return A brand new RustFoldingScanner
     */
    public static RustFoldingScanner create() {
        return new RustFoldingScanner();
    }

    /**
     * FoldType for code blocks.
     */
    public static final FoldType TYPE_CODE_BLOCKS = FoldType.CODE_BLOCK;
    /**
     * FoldType for Rust structs
     */
    @NbBundle.Messages("FT_Structs=Structs")
    public static final FoldType TYPE_STRUCTS = FoldType.NESTED.derive(
            "class", // NOI18N
            getBundleString("FT_Structs"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);
    /**
     * FoldType for Rust functions
     */
    @NbBundle.Messages("FT_Functions=Functions")
    public static final FoldType TYPE_FUNCTION = FoldType.MEMBER.derive("function", // NOI18N
            getBundleString("FT_Functions"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);

    /**
     * FoldType for Rust impls
     */
    @NbBundle.Messages("FT_Impls=Impls")
    public static final FoldType TYPE_IMPL = FoldType.MEMBER.derive("impl", // NOI18N
            getBundleString("FT_Impls"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);

    /**
     * FoldType for Rust traits
     */
    @NbBundle.Messages("FT_Traits=Traits")
    public static final FoldType TYPE_TRAIT = FoldType.MEMBER.derive("trait", // NOI18N
            getBundleString("FT_Traits"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);

    /**
     * FoldType for Rust enums
     */
    @NbBundle.Messages("FT_Enums=Enums")
    public static final FoldType TYPE_ENUM = FoldType.MEMBER.derive("enum", // NOI18N
            getBundleString("FT_Enums"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);

    /**
     * FoldType for Rust macros
     */
    @NbBundle.Messages("FT_Macros=Macros")
    public static final FoldType TYPE_MACRO = FoldType.MEMBER.derive("macro", // NOI18N
            getBundleString("FT_Macros"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);

    /**
     * FoldType for Rust modules
     */
    @NbBundle.Messages("FT_Modules=Modules")
    public static final FoldType TYPE_MODULE = FoldType.MEMBER.derive("module", // NOI18N
            getBundleString("FT_Modules"), // NOI18N
            FoldTemplate.DEFAULT_BLOCK);

    public Map<String, List<OffsetRange>> folds(RustLanguageParserResult rustLanguageParserResult) {
        RustAST ast = rustLanguageParserResult.getAST();
        if (ast == null) {
            return Collections.emptyMap();
        }
        RustASTNode crate = ast.getCrate();
        if (crate == null) {
            return Collections.emptyMap();
        }

        HashMap<String, List<OffsetRange>> foldMap = new HashMap<>();
        recursivelyAddFolds(crate, foldMap);
        return foldMap;

    }

    private Map<String, List<OffsetRange>> recursivelyAddFolds(
            RustASTNode node,
            final Map<String, List<OffsetRange>> foldMap) {

        if (node.getFold() != null) {
            String foldType = null;
            switch (node.getKind()) {
                case ENUM:
                    foldType = TYPE_ENUM.code();
                    break;
                case FUNCTION:
                    foldType = TYPE_FUNCTION.code();
                    break;
                case IMPL:
                    foldType = TYPE_IMPL.code();
                    break;
                case MACRO:
                    foldType = TYPE_MACRO.code();
                    break;
                case MODULE:
                    foldType = TYPE_MODULE.code();
                    break;
                case STRUCT:
                    foldType = TYPE_STRUCTS.code();
                    break;
                case TRAIT:
                    foldType = TYPE_TRAIT.code();
                    break;
            }
            if (foldType != null) {
                List<OffsetRange> folds = foldMap.get(foldType);
                if (folds == null) {
                    folds = new ArrayList<>();
                    foldMap.put(foldType,folds);
                }
                if (! folds.contains(node.getFold())) {
                    folds.add(node.getFold());
                }
            }
        }

        node.visit( (child) -> { recursivelyAddFolds(child, foldMap); });

        if (node.codeblockFolds() != null) {
            List<OffsetRange> codeBlockFolds = foldMap.get(TYPE_CODE_BLOCKS.code());
            if (codeBlockFolds == null) {
                codeBlockFolds = new ArrayList<>();
                foldMap.put(TYPE_CODE_BLOCKS.code(), codeBlockFolds);
            }
            final List<OffsetRange> finalCodeBlocks = codeBlockFolds;
            node.codeblockFolds().forEach( (range) -> {
                if (! finalCodeBlocks.contains(range)) {
                    finalCodeBlocks.add(range);
                }
            });
        }

        return foldMap;
    }

    /**
     * Returns the codeblockFolds from the giveh RustLanguageParserResult
     *
     * @param rustLanguageParserResult The result of parsing, i.e.: a RustAST
     * @return A list of codeblockFolds of appropriate types.
     */
    public Map<String, List<OffsetRange>> foldsOLD(RustLanguageParserResult rustLanguageParserResult) {
        RustAST ast = rustLanguageParserResult.getAST();
        if (ast == null) {
            return Collections.emptyMap();
        }
        RustASTNode crate = ast.getCrate();
        if (crate == null) {
            return Collections.emptyMap();
        }
        HashMap<String, List<OffsetRange>> folds = new HashMap<>();
        List<OffsetRange> allFunctionFolds = new ArrayList<>();
        List<OffsetRange> allCodeblockFolds = new ArrayList<>();

        Consumer<? super RustASTNode> addFunction = (function) -> {
            if (function.getFold() != null) {
                allFunctionFolds.add(function.getFold());
            }
            allCodeblockFolds.addAll(function.codeblockFolds());
        };

        Consumer<? super RustASTNode> addFunctionsInNode = (nodeWithFunctions) -> {
            nodeWithFunctions.functions().forEach(addFunction);
        };

        // Structs
        {
            List<OffsetRange> structFolds = crate.structs().stream().map(RustASTNode::getFold).filter(Objects::nonNull).collect(Collectors.toList());
            folds.put(TYPE_STRUCTS.code(), structFolds);
            crate.structs().forEach(addFunctionsInNode);
        }

        // Impls
        {
            List<OffsetRange> implFolds = crate.impls().stream().map(RustASTNode::getFold).filter(Objects::nonNull).collect(Collectors.toList());
            folds.put(TYPE_IMPL.code(), implFolds);
            crate.impls().forEach(addFunctionsInNode);
        }

        // Traits
        {
            List<OffsetRange> traitFolds = crate.traits().stream().map(RustASTNode::getFold).filter(Objects::nonNull).collect(Collectors.toList());
            folds.put(TYPE_TRAIT.code(), traitFolds);
            crate.traits().forEach(addFunctionsInNode);
        }

        // Enums
        {
            List<OffsetRange> enumFolds = crate.enums().stream().map(RustASTNode::getFold).filter(Objects::nonNull).collect(Collectors.toList());
            folds.put(TYPE_ENUM.code(), enumFolds);
            crate.enums().forEach(addFunctionsInNode);
        }

        // Macros
        {
            List<OffsetRange> macroFolds = crate.macros().stream().map(RustASTNode::getFold).filter(Objects::nonNull).collect(Collectors.toList());
            folds.put(TYPE_MACRO.code(), macroFolds);
            crate.macros().forEach(addFunctionsInNode);
        }

        // Modules
        {
            List<OffsetRange> moduleFolds = crate.modules().stream().map(RustASTNode::getFold).filter(Objects::nonNull).collect(Collectors.toList());
            folds.put(TYPE_MODULE.code(), moduleFolds);
            crate.modules().forEach(addFunctionsInNode);
        }

        // Function folds 
        {
            crate.functions().forEach(addFunction);
        }

        folds.put(TYPE_FUNCTION.code(), allFunctionFolds);
        folds.put(TYPE_CODE_BLOCKS.code(), allCodeblockFolds);

        return folds;

    }

}
