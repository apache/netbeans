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

package org.netbeans.modules.xml.xdm;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.modules.xml.xdm.nodes.Document;


/**
 *
 * @author Chris Webster
 */
class XDMModelUndoableEdit extends AbstractUndoableEdit {
	// Even though AbstractUndoableEdit is serializable this class is not. The
	// UndoableEdit interface is not Serializable, so this is not required but
	// just an implementation detail.

	private static final long serialVersionUID = -4513245871320808368L;

	public XDMModelUndoableEdit(Document oldDoc, Document newDoc, XDMModel model) {
		oldDocument = oldDoc;
		newDocument = newDoc;
		this.model = model;
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
        try {
            model.resetDocument(newDocument);
        } catch (RuntimeException ex) {
            if (newDocument != model.getCurrentDocument()) {
                CannotRedoException e = new CannotRedoException();
                e.initCause(ex);
                throw e;
            } else {
                throw ex;
            }
        }
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
        try {
            model.resetDocument(oldDocument);
        } catch (RuntimeException ex) {
            if (oldDocument != model.getCurrentDocument()) {
                CannotUndoException e = new CannotUndoException();
                e.initCause(ex);
                throw e;
            } else {
                throw ex;
            }
        }
	}
	
        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof XDMModelUndoableEdit) {
                XDMModelUndoableEdit theEdit = (XDMModelUndoableEdit) anEdit;
                if (newDocument == theEdit.oldDocument) {
                    newDocument = theEdit.newDocument;
                    return true;
                }
            }
            return false;
        }
        
	private Document oldDocument;
	private Document newDocument;
	private XDMModel model;
	
}
