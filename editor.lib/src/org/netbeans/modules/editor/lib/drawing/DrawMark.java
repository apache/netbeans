/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
