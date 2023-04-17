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

import java.util.List;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.languages.hcl.HCLParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLDocument;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.ast.SourceRef;
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
    })
    protected void processDocument(HCLDocument doc) {
        for (HCLBlock block : doc.getBlocks()) {
            List<HCLIdentifier> decl = block.getDeclaration();
            HCLIdentifier type = decl.get(0);
            SourceRef src = type.getSourceRef().get();
            DefaultError error = new DefaultError(null, Bundle.INVALID_BLOCK(), null, getFileObject(), src.startOffset , src.endOffset, Severity.ERROR);
            errors.add(error);
        }
    }

}
