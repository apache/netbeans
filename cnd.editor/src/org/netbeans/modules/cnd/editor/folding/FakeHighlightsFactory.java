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
package org.netbeans.modules.cnd.editor.folding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;

/**
 * This fake factory is needed to add property change listener to {@link EditorRegistry}.
 * There is no better way to do it so far. See bug 173002.
 *
 * With the listener registered, every C/C++/H editor has code folding
 * controls right after it's opened. Without the listener, code folding
 * controls used to appear only after editing a file.
 *
 */
public final class FakeHighlightsFactory implements HighlightsLayerFactory, PropertyChangeListener {

    private static final HighlightsLayer[] EMPTY = {};

    public FakeHighlightsFactory() {
        EditorRegistry.addPropertyChangeListener(this);
    }

    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return EMPTY;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Document docToReparse = null;
        if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
            docToReparse = ((JTextComponent) evt.getNewValue()).getDocument();
        } else if (EditorRegistry.FOCUSED_DOCUMENT_PROPERTY.equals(evt.getPropertyName())) {
            docToReparse = (Document) evt.getNewValue();
        }
        if (docToReparse != null && MIMENames.isHeaderOrCppOrC(DocumentUtilities.getMimeType(docToReparse))) {
            CppMetaModel.getDefault().scheduleParsing(docToReparse);
        }
    }
}
