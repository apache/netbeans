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
package org.netbeans.modules.cnd.asm.core.editor;

import java.io.StringReader;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.util.Lookup;

import org.netbeans.modules.cnd.asm.model.AsmSyntaxProvider;
import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.AsmModelProvider;
import org.netbeans.modules.cnd.asm.model.AsmSyntax;
import org.netbeans.modules.cnd.asm.model.AsmTypesProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorUtilities;

public class AsmEditorKit extends NbEditorKit {

    /** Initialize document by adding the draw-layers for example. */
    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        initLanguage(doc);
    }

    @Override
    public String getContentType() {
        return MIMENames.ASM_MIME_TYPE;
    }

    private static final class LangInitializer implements DocumentListener {

        private final Document doc;
        LangInitializer(Document doc) {
            this.doc = doc;
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            initLanguage();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            initLanguage();
        }

        private void initLanguage() {
            if (AsmEditorKit.initLanguage(doc)) {
                doc.removeDocumentListener(this);
                doc.putProperty(LangInitializer.class, null);
            }
        }
    }

    private static boolean initLanguage(Document doc) {
        AsmModelAccessor acc = (AsmModelAccessor) doc.getProperty(AsmModelAccessor.class);
        if (acc == null) {
            if (doc.getProperty(Language.class) != null) {
                return true;
            }
            String text = AsmObjectUtilities.getText(doc);
            if (text.length() == 0) {
                text = AsmObjectUtilities.getText(NbEditorUtilities.getFileObject(doc));
            }
            if (text.length() == 0) {
                if (doc.getProperty(LangInitializer.class) == null) {
                    LangInitializer langInitializer = new LangInitializer(doc);
                    doc.putProperty(LangInitializer.class, langInitializer);
                    doc.addDocumentListener(langInitializer);
                }
                return false;
            }
            AsmModelProvider modelProv = null;
            AsmSyntaxProvider syntProv = null;
            Collection<? extends AsmTypesProvider> idents = Lookup.getDefault().lookup(new Lookup.Template<AsmTypesProvider>(AsmTypesProvider.class)).allInstances();
            AsmTypesProvider.ResolverResult res = null;
            for (AsmTypesProvider ident : idents) {
                res = ident.resolve(new StringReader(text));
                if (res != null) {
                    modelProv = res.getModelProvider();
                    syntProv = res.getSyntaxProvider();
                    Logger.getLogger(AsmEditorKit.class.getName()).log(Level.FINE, "Asm Regognized " + modelProv + " " + syntProv); // NOI18N
                }
            }
            if (res == null || modelProv == null || syntProv == null) {
                return false;
            }
            AsmModel model = modelProv.getModel();
            AsmSyntax synt = syntProv.getSyntax(model);
            acc = new AsmModelAccessorImpl(model, synt, doc);
            doc.putProperty(AsmModelAccessor.class, acc);
            doc.putProperty(AsmModel.class, model);
            doc.putProperty(Language.class, new AsmLanguageHierarchy(synt).language());
            return true;
        }
        return false;
    }
}
