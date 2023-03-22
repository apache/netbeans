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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.languages.hcl.HCLParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
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
        "INVALID_BLOCK_DECLARATION=<html><b>{0}</b> block needs {1,choice,0#no identifiers|1#one identifier|1<{1,number,integer} identifiers}.",
        "# {0} - Block type name",
        "UNKNOWN_BLOCK=Unknown block: {0}"

    })
    protected void processDocument(HCLDocument doc) {
        for (HCLBlock block : doc.getBlocks()) {
            List<HCLIdentifier> decl = block.getDeclaration();
            HCLIdentifier type = decl.get(0);

            BlockType bt = BlockType.get(type.id());
            SourceRef src = type.getSourceRef().get();
            if (bt != null) {
                if (decl.size() != bt.definitionLength) {
                    DefaultError error = new DefaultError(null, Bundle.INVALID_BLOCK_DECLARATION(bt.type, bt.definitionLength - 1), null, getFileObject(), src.startOffset , src.endOffset, Severity.ERROR);
                    errors.add(error);
                }
            } else {
                DefaultError error = new DefaultError(null, Bundle.UNKNOWN_BLOCK(type.id()), null, getFileObject(), src.startOffset , src.endOffset, Severity.ERROR);
                errors.add(error);
            }
        }
    }

}
