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
package org.netbeans.modules.micronaut.symbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.spi.lsp.StructureProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = StructureProvider.class, position = 1000)
public class MicronautStructureProvider implements StructureProvider {

    @Override
    public List<StructureElement> getStructure(Document doc) {
        JavaSource js = JavaSource.forDocument(doc);
        if (js != null) {
            ClassPath cp = js.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
            if (cp.findResource("io/micronaut/http/annotation/HttpMethodMapping.class") != null) {
                try {
                    List<StructureElement> elements = new ArrayList<>();
                    js.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        for (MicronautSymbolFinder.SymbolLocation symbolLocation : MicronautSymbolFinder.scan(cc, false)) {
                            elements.add(StructureProvider.newBuilder(symbolLocation.getName(), StructureElement.Kind.Interface)
                                    .file(cc.getFileObject())
                                    .expandedStartOffset(symbolLocation.getStart())
                                    .expandedEndOffset(symbolLocation.getEnd())
                                    .selectionStartOffset(symbolLocation.getSelectionStart())
                                    .selectionEndOffset(symbolLocation.getSelectionEnd())
                                    .build());
                        }
                    }, true);
                    return elements;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return Collections.emptyList();
    }
}
