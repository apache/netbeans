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

package org.netbeans.modules.cnd.completion.impl.xref;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;

/**
 *
 *
 */
public class DocOffsetableImpl implements CsmOffsetable {
    private final CsmUID<CsmFile> uidFile;
    private final DocOffsPositionImpl pos;
    
    public DocOffsetableImpl(BaseDocument doc, CsmFile file, int offset) {
        this.pos = new DocOffsPositionImpl(doc, offset);
        assert file != null : "null file for document " + doc + " on offset " + offset;
        this.uidFile = UIDs.get(file);
    }

    protected BaseDocument getDocument() {
        return pos.getDocument();
    }
    
    @Override
    public CsmFile getContainingFile() {
        return this.uidFile.getObject();
    }

    @Override
    public int getStartOffset() {
        return pos.getOffset();
    }

    @Override
    public int getEndOffset() {
        return this.getStartOffset() + getText().length();
    }

    @Override
    public CsmOffsetable.Position getStartPosition() {
        return pos;
    }

    @Override
    public CsmOffsetable.Position getEndPosition() {
        return new DocOffsPositionImpl(getDocument(),  getEndOffset());
    }

    @Override
    public CharSequence getText() {
        return "";
    }
    
    protected final boolean isValid() {
        CsmFile file = getContainingFile();
        return file != null && file.isValid();
    }
}
