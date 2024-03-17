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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.languages.hcl.HCLParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLAttribute;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLContainer;
import org.netbeans.modules.languages.hcl.ast.HCLDocument;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.SourceRef;
import org.netbeans.modules.languages.hcl.ast.HCLTreeWalker;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
public class TerraformParserResult extends HCLParserResult {

    private final Map<String, HCLBlock> definedBlocks = new HashMap<>();
    
    public enum BlockType {

        CHECK("check", 2),
        DATA("data", 3),
        IMPORT("import", 1),
        LOCALS("locals", 1),
        MODULE("module", 2),
        MOVED("moved", 1),
        OUTPUT("output", 2),
        PROVIDER("provider", 2),
        REMOVED("removed", 1),
        RESOURCE("resource", 3),
        RUN("run", 2),
        TERRAFORM("terraform", 1),
        VARIABLE("variable", 2);

        final String type;
        final int definitionLength;

        private static final Map<String, BlockType> TYPES = new HashMap<>();
        static {
            for (BlockType bt : values()) {
                TYPES.put(bt.type, bt);
            }
        }

        private BlockType(String type, int definitionLenght) {
            this.type = type;
            this.definitionLength = definitionLenght;
        }

        public static BlockType get(String name) {
            return TYPES.get(name);
        }
    }

    public TerraformParserResult(Snapshot snapshot) {
        super(snapshot);
    }


    @Override
    @Messages({
        "# {0} - Block type name",
        "# {1} - supported declaration",
        "INVALID_BLOCK_DECLARATION={0} block needs {1,choice,0#no identifiers|1#one identifier|1<{1,number,integer} identifiers}.",
        "# {0} - Block type name",
        "UNKNOWN_BLOCK=Unknown block: {0}",
        "# {0} - Block ID",
        "DUPLICATE_BLOCK=Duplicate Block Definition: {0}",
        "# {0} - Attribute name",
        "DUPLICATE_ATTRIBUTE=Attribute {0} has already defined",
        "# {0} - Attribute name",
        "UNEXPECTED_DOCUMENT_ATTRIBUTE=No attributes expected at Terraform document level.",

    })
    protected void processDocument(HCLDocument doc, SourceRef references) {
        HCLTreeWalker.breadthFirst(doc, this::duplicateAttributeVisitor);
        HCLTreeWalker.breadthFirst(doc, this::checkBlockDeclarationVisitor);
    }

    private boolean checkBlockDeclarationVisitor(HCLTreeWalker.Step step) {
        if (step.node() instanceof HCLBlock block) {
            List<HCLIdentifier> decl = block.declaration();
            HCLIdentifier type = decl.get(0);

            BlockType bt = BlockType.get(type.id());
            if (bt != null) {
                if (decl.size() != bt.definitionLength) {
                    addError(type, Bundle.INVALID_BLOCK_DECLARATION(bt.type, bt.definitionLength - 1));
                } else {
                    if (definedBlocks.put(block.id(), block) != null) {
                        switch (bt) {
                            case CHECK:
                            case DATA:
                            case MODULE:
                            case OUTPUT:
                            case RESOURCE:
                            case RUN:
                            case VARIABLE:
                                addError(decl.get(bt.definitionLength - 1), Bundle.DUPLICATE_BLOCK(block.id()));
                        }
                    }
                }
            } else {
                addError(type, Bundle.UNKNOWN_BLOCK(type.id()));
            }
        }
        // Here we just check the first level of blocks, won't go deeper int the tree
        return false;
    }
    
    private boolean duplicateAttributeVisitor(HCLTreeWalker.Step step) {
        if (step.node() instanceof HCLDocument doc) {
            for (HCLAttribute attr : doc.attributes()) {
                addError(attr, Bundle.UNEXPECTED_DOCUMENT_ATTRIBUTE(attr.id()));
            }
            return true;
        }
        if (step.node() instanceof HCLContainer c) {
            if (c.hasAttribute()) {
                Set<String> defined = new HashSet<>();
                for (HCLAttribute attr : c.attributes()) {
                    if (!defined.add(attr.id())) {
                        addError(attr.name(), Bundle.DUPLICATE_ATTRIBUTE(attr.id()));
                    }                        
                }
            }
            return false;
        }
        return true;            
    }
    

}
