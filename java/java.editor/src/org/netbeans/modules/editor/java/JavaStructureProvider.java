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
package org.netbeans.modules.editor.java;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.spi.lsp.StructureProvider;

/**
 * Implementation of StructureProvider from LSP API. It's used for displaying
 * outline view and GoTo File Symbols in VSCode.
 *
 * @author Petr Pisl
 */
@MimeRegistration(mimeType = "text/x-java", service = StructureProvider.class, position = 100)
public class JavaStructureProvider implements StructureProvider {

    private static final Logger LOGGER = Logger.getLogger(JavaStructureProvider.class.getName());

    @Override
    public List<StructureElement> getStructure(Document doc) {
        JavaSource js = JavaSource.forDocument(doc);

        if (js != null) {
            List<StructureElement> result = new ArrayList<>();
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Trees trees = cc.getTrees();
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    if (cu.getPackage() != null) {
                        TreePath tp = trees.getPath(cu, cu.getPackage());
                        Element el = trees.getElement(tp);
                        if (el != null && el.getKind() == ElementKind.PACKAGE) {
                            Builder builder = StructureProvider.newBuilder(el.getSimpleName().toString(), ElementHeaders.javaKind2Structure(el));
                            int start = (int) cc.getTrees().getSourcePositions().getStartPosition(cu, cu.getPackage());
                            int end = (int) cc.getTrees().getSourcePositions().getEndPosition(cu, cu.getPackage());

                            builder.expandedStartOffset(start).selectionStartOffset(start);
                            builder.expandedEndOffset(end).selectionEndOffset(end);
                            result.add(builder.build());
                        }
                    }
                    for (Element tel : cc.getTopLevelElements()) {
                        StructureElement jse = element2StructureElement(cc, tel);
                        if (jse != null) {
                            result.add(jse);
                        }
                    }
                }, true);
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    private static final ElementUtilities.ElementAcceptor ALL_ACCEPTOR = new ElementUtilities.ElementAcceptor() {
        @Override
        public boolean accept(Element e, TypeMirror type) {
            return true;
        }
    };
    
    private static StructureElement element2StructureElement(CompilationInfo info, Element el) {
        return ElementHeaders.toStructureElement(info, el, ALL_ACCEPTOR);
    }
}
