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
