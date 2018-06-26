/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.actions;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.web.jsf.editor.PositionRange;
import org.netbeans.modules.web.jsf.editor.actions.ImportData.VariantItem;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.util.Exceptions;

/**
 * Processes the Facelet and updates namespace imports according to preprocessed data.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
class FixNamespacesPerformer {

    private final HtmlParserResult parserResult;
    private final ImportData importData;
    private final List<VariantItem> selections;
    private final boolean removeUnused;
    private BaseDocument baseDocument;

    public FixNamespacesPerformer(HtmlParserResult parserResult, ImportData importData, List<VariantItem> selections, boolean removeUnused) {
        this.parserResult = parserResult;
        this.importData = importData;
        this.selections = selections;
        this.removeUnused = removeUnused;
    }

    void perform() {
        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document instanceof BaseDocument) {
            baseDocument = (BaseDocument) document;

            baseDocument.runAtomic(new Runnable() {
                @Override
                public void run() {
                    // import missing namespaces
                    includeMissingNamespaces();

                    // remove unused namespaces
                    if (removeUnused) {
                        removeUnusedNamespaces();
                    }
                }
            });
        }
    }

    private void removeUnusedNamespaces() {
        try {
            List<PositionRange> ranges = new ArrayList<>(importData.getItemsToRemove().size());
            for (Attribute attribute : importData.getItemsToRemove()) {
                int from = attribute.from();
                int to = attribute.to();
                //check if the line before the area is white
                int lineBeginning = Utilities.getRowStart(baseDocument, attribute.from());
                int firstNonWhite = Utilities.getFirstNonWhiteBwd(baseDocument, attribute.from());
                if (lineBeginning > firstNonWhite) {
                    //delete the white content before the area inclusing the newline
                    from = lineBeginning - 1; // (-1 => includes the line end)
                }
                ranges.add(new PositionRange(baseDocument, from, to));
            }
            for (PositionRange positionRange : ranges) {
                baseDocument.remove(positionRange.getFrom(), positionRange.getTo() - positionRange.getFrom());
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void includeMissingNamespaces() {
        for (VariantItem variant : selections) {
            LibraryUtils.importLibrary(baseDocument, variant.getLibrary(), variant.getPrefix(), importData.isJsf22);
        }
    }
}
