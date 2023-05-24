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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.languages.hcl.HCLParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLAttribute;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLContainer;
import org.netbeans.modules.languages.hcl.ast.HCLDocument;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.ast.SourceRef;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
public class TerraformParserResult extends HCLParserResult {


    enum BlockType {

        DATA("data", 3),
        LOCALS("locals", 1),
        MODULE("module", 2),
        MOVED("moved", 1),
        OUTPUT("output", 2),
        PROVIDER("provider", 2),
        RESOURCE("resource", 3),
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
        Set<String> defined = new HashSet<>();
        for (HCLBlock block : doc.getBlocks()) {
            List<HCLIdentifier> decl = block.getDeclaration();
            HCLIdentifier type = decl.get(0);

            BlockType bt = BlockType.get(type.id());
            if (bt != null) {
                if (decl.size() != bt.definitionLength) {
                    references.getOffsetRange(type).ifPresent((range) -> addError(Bundle.INVALID_BLOCK_DECLARATION(bt.type, bt.definitionLength - 1), range));
                } else {
                    if (!defined.add(block.id())) {
                        switch (bt) {
                            case DATA:
                            case MODULE:
                            case OUTPUT:
                            case RESOURCE:
                            case VARIABLE:
                                references.getOffsetRange(type).ifPresent((range) -> addError(Bundle.DUPLICATE_BLOCK(block.id()), range));
                        }
                    }
                }
            } else {
                references.getOffsetRange(type).ifPresent((range) -> addError(Bundle.UNKNOWN_BLOCK(type.id()), range));
            }
            checkDuplicateAttribute(doc, references);
        }
        for (HCLAttribute attribute : doc.getAttributes()) {
            references.getOffsetRange(attribute.getName()).ifPresent((range) -> addError(Bundle.UNEXPECTED_DOCUMENT_ATTRIBUTE(attribute.id()), range));
        }
    }

    private void checkDuplicateAttribute(HCLContainer c, SourceRef references) {
        for (HCLBlock block : c.getBlocks()) {
            checkDuplicateAttribute(block, references);
        }
        if (c.hasAttributes()) {
            Set<String> defined = new HashSet<>();
            for (HCLAttribute attr : c.getAttributes()) {
                if (!defined.add(attr.id())) {
                    references.getOffsetRange(attr.getName()).ifPresent((range) -> addError(Bundle.DUPLICATE_ATTRIBUTE(attr.id()), range));
                }
            }
        }
    }

    private void addError(String message, OffsetRange range) {
        DefaultError error = new DefaultError(null, message, null, getFileObject(), range.getStart() , range.getEnd(), Severity.ERROR);
        errors.add(error);
    }

}
