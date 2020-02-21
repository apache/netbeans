/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
