/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
