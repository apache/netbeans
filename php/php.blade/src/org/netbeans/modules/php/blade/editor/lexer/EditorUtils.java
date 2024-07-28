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
package org.netbeans.modules.php.blade.editor.lexer;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author bogdan
 */
public class EditorUtils {

    public static TokenSequence<PHPTokenId> getTokenSequence(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument) doc;
        TokenSequence<PHPTokenId> tokenSequence = null;
        baseDoc.readLock();
        try {
            TokenHierarchy<Document> hierarchy = TokenHierarchy.get(baseDoc);
            tokenSequence = hierarchy.tokenSequence(PHPTokenId.language());
        } finally {
            baseDoc.readUnlock();
        }
        if (tokenSequence != null) {
            tokenSequence.move(offset);
            tokenSequence.moveNext();
        }
        return tokenSequence;

    }
    
    public static FileObject getFileObjectFromDoc(Document doc) {
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (dObject != null) {
            return dObject.getPrimaryFile().getParent();
        }
        return null;
    }
    
    public static Project getProjectOwner(Document doc) {
        FileObject file = getFileObjectFromDoc(doc);
        if (file == null){
            return null;
        }
        return ProjectUtils.getMainOwner(file);
    }
}
