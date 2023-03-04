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
package org.netbeans.modules.css.editor.csl;

import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.openide.util.Pair;

/**
 * Hyperlinking for @import declaration only.
 * 
 * TODO: Add support for URLs in properties, namespace declarations
 *
 * @author mfukala@netbeans.org
 */
public class CssDeclarationFinder implements DeclarationFinder {

    private AtomicReference<FutureParamTask<DeclarationLocation, EditorFeatureContext>> taskRef 
            = new AtomicReference<>();

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        FutureParamTask<DeclarationLocation, EditorFeatureContext> task = taskRef.getAndSet(null);
        if(task != null) {
            CssParserResult wrapper = (CssParserResult)info;
            return task.run(new EditorFeatureContext(wrapper, caretOffset));
        }
        
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> declarationLocation = CssModuleSupport.getDeclarationLocation(doc, caretOffset, new FeatureCancel());
        if(declarationLocation != null) {
            taskRef.set(declarationLocation.second());
            return declarationLocation.first();
        }
        return OffsetRange.NONE;
    }
}
