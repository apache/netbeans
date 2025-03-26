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
package org.netbeans.modules.languages.hcl.terraform;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.languages.hcl.HCLSemanticAnalyzer;
import org.netbeans.modules.languages.hcl.HCLParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.ast.HCLResolveOperation;
import org.netbeans.modules.languages.hcl.ast.HCLVariable;
import org.netbeans.modules.languages.hcl.SourceRef;
import org.netbeans.modules.languages.hcl.ast.HCLTreeWalker;
import org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.CHECK;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.DATA;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.LOCALS;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.MODULE;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.OUTPUT;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.PROVIDER;
import static org.netbeans.modules.languages.hcl.terraform.TerraformParserResult.BlockType.RESOURCE;

/**
 *
 * @author lkishalmi
 */
public final class TerraformSemanticAnalyzer extends HCLSemanticAnalyzer {

    private static final Set<String> RESOLVE_BASES = Set.of(
            "data",
            "local",
            "module",
            "path",
            "provider",
            "var"
    );

    private static final Set<String> LITERAL_TYPES = Set.of(
            "bool",
            "number",
            "null",
            "string"
    );

    @Override
    protected Highlighter createHighlighter(HCLParserResult result) {
        return new TerraformHighlighter(result.getReferences());
    }
    
    private class TerraformHighlighter extends DefaultHighlighter {
        private BlockType rootBlockType;

        protected TerraformHighlighter(SourceRef refs) {
            super(refs);
        }

        @Override
        protected void highlight(HCLTreeWalker.Step step) {
            super.highlight(step);

            // TODO: Can use record patterns from Java 21
            if (step.depth() == 1 && step.node() instanceof HCLBlock block) {
                List<HCLIdentifier> dcl = block.declaration();
                if (!dcl.isEmpty()) {
                    rootBlockType = TerraformParserResult.BlockType.get(dcl.get(0).id());
                }
            } else if (step.node() instanceof HCLResolveOperation.Attribute attr) {
                if ((rootBlockType != null) && (attr.base() instanceof HCLVariable var)) {
                    String name = var.name().id();
                    switch (rootBlockType) {
                        case CHECK:
                        case DATA:
                        case LOCALS:
                        case MODULE:
                        case OUTPUT:
                        case PROVIDER:
                        case RESOURCE:
                            if (RESOLVE_BASES.contains(name)) {
                                mark(attr.base(), ColoringAttributes.FIELD_SET);
                            }
                            break;
                    }
                }
            } else if (rootBlockType == BlockType.VARIABLE && (step.node() instanceof HCLVariable var)) {
                String name = var.name().id();
                if (LITERAL_TYPES.contains(name)) {
                    mark(var, ColoringAttributes.FIELD_SET);
                }
            }
        }

    }
}
