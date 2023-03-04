/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;


public class ExpectedDocument extends PlainDocument {
    
    private final PositionSyncList positionSyncList;
    
    public ExpectedDocument() {
        super(new ExpectedDocumentContent());
        this.positionSyncList = new PositionSyncList(this);
    }
    
    void setTestDocument(TestEditorDocument testDoc) {
        positionSyncList.setTestDocument(testDoc);
    }
    
    public Position createSyncedTestPosition(int offset, boolean backwardBias) throws BadLocationException {
        return positionSyncList.createSyncedTestPosition(offset, backwardBias);
    }
    
    public void releaseSyncedTestPosition(Position testPos) {
        positionSyncList.releaseTestPos(testPos);
    }
    
    public void releaseSyncedPosition(int index) {
        positionSyncList.removePair(index);
    }
    
    public int syncPositionCount() {
        return positionSyncList.size();
    }

    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        super.insertUpdate(chng, attr);
        positionSyncList.insertUpdate(chng);
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        positionSyncList.removeUpdate(chng);
    }
    
    ExpectedDocumentContent getDocumentContent() {
        return (ExpectedDocumentContent) getContent();
    }

    public Position createBackwardBiasPosition(int offs) throws BadLocationException {
        return getDocumentContent().createBackwardBiasPosition(offs);
    }

    public void checkConsistency() {
        positionSyncList.checkConsistency();
    }

}
