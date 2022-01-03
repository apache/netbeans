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
package org.netbeans.modules.cnd.refactoring.hints;

import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 */
public class SurroundWithIfndef implements Fix {
    private final BaseDocument doc;
    private final CsmFile file;
    private final String macroIdentifier;
    private final CsmReference ref;
    
    public SurroundWithIfndef(Document doc, CsmFile file, CsmReference ref, String macroIdentifier) {
        this.doc = (BaseDocument) doc;
        this.file = file;
        this.ref = ref;
        this.macroIdentifier = macroIdentifier;
    }
    
    @Override
    public String getText() {
        return NbBundle.getMessage(SurroundWithIfndef.class, "HINT_Ifndef"); //NOI18N
    }
    
    @Override
    public ChangeInfo implement() throws Exception {
        CsmFileInfoQuery query = CsmFileInfoQuery.getDefault();
        int line = query.getLineColumnByOffset(file, ref.getStartOffset())[0];
        int startOffset = (int) query.getOffset(file, line, 1);
        int endOffset = (int) query.getOffset(file, line+1, 1) - 1;
        Position startPosition = NbDocument.createPosition(doc, startOffset, Position.Bias.Forward);
        Position endPosition = NbDocument.createPosition(doc, endOffset, Position.Bias.Forward);
        
        String ifndefText = "#ifndef " + macroIdentifier + "\n"; // NOI18N
        doc.insertString(startPosition.getOffset(), ifndefText, null);
        doc.insertString(endPosition.getOffset(), "\n#endif", null); // NOI18N
        
        return null;
    }
    
}
