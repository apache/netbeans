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
package org.netbeans.modules.languages.hcl.tfvars;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.languages.hcl.HCLParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLAttribute;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLDocument;
import org.netbeans.modules.languages.hcl.SourceRef;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
public class TFVarsParserResult extends HCLParserResult {

    public TFVarsParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    @Override
    @NbBundle.Messages({
        "INVALID_BLOCK=Blocks are not supported in TFVars files.",
        "# {0} - The name of duplicated variable",
        "DUPLICATE_VARIABLE=Variable {0} is already defined."
    })
    protected void processDocument(HCLDocument doc, SourceRef references) {
        for (HCLBlock block : doc.blocks()) {
            addError(block, Bundle.INVALID_BLOCK());
        }
        Set<String> usedAttributes = new HashSet<>();
        for (HCLAttribute attr : doc.attributes()) {
            if (!usedAttributes.add(attr.id())) {
                addError(attr.name(), Bundle.DUPLICATE_VARIABLE(attr.id()));
            }
        }
    }

}
