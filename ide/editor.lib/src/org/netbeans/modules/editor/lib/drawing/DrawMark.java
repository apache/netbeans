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

package org.netbeans.modules.editor.lib.drawing;

import java.lang.ref.WeakReference;
import javax.swing.text.Position;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.InvalidMarkException;
import org.netbeans.editor.MarkFactory.ContextMark;

/** Activation mark for particular layer. When layer is not active
* its updateContext() method is not called.
*/
/* package */ class DrawMark extends ContextMark {

    /** Activation flag means either activate layer or deactivate it */
    protected boolean activateLayer;

    /** Reference to draw layer this mark belogns to */
    String layerName;

    /** Reference to extended UI if this draw mark is info-specific or
    * null if it's document-wide.
    */
    WeakReference editorUIRef;

    public DrawMark(String layerName, EditorUI editorUI) {
        this(layerName, editorUI, Position.Bias.Forward);
    }

    public DrawMark(String layerName, EditorUI editorUI, Position.Bias bias) {
        super(bias, false);
        this.layerName = layerName;
        setEditorUI(editorUI);
    }

    public boolean isDocumentMark() {
        return (editorUIRef == null);
    }

    public EditorUI getEditorUI() {
        if (editorUIRef != null) {
            return (EditorUI)editorUIRef.get();
        }
        return null;
    }

    public void setEditorUI(EditorUI editorUI) {
        this.editorUIRef = (editorUI != null) ? new WeakReference(editorUI) : null;
    }

    public boolean isValidUI() {
        return !(editorUIRef != null && editorUIRef.get() == null);
    }

    public void setActivateLayer(boolean activateLayer) {
        this.activateLayer = activateLayer;
    }

    public boolean getActivateLayer() {
        return activateLayer;
    }

    public boolean removeInvalid() {
        if (!isValidUI() && isValid()) {
            try {
                this.remove();
            } catch (InvalidMarkException e) {
                throw new IllegalStateException(e.toString());
            }
            return true; // invalid and removed
        }
        return false; // valid
    }

    public @Override String toString() {
        try {
            return "pos=" + getOffset() + ", line=" + getLine(); // NOI18N
        } catch (InvalidMarkException e) {
            return "mark not valid"; // NOI18N
        }
    }

}
