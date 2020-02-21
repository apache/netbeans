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

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.fortran.FKit;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranReformatter;
import org.netbeans.modules.cnd.editor.indent.CppIndentFactory;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.netbeans.modules.cnd.test.base.BaseDocumentUnitTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.indent.api.Reformat;

/**
 * base class to run tests around editor. Like typing, formatting, indenting
 */
public class EditorBase extends BaseDocumentUnitTestCase {
    private boolean isCPP = true;
    
    public EditorBase(String testMethodName) {
        super(testMethodName);
    }
    
    @Override
    protected final EditorKit createEditorKit() {
        if (isCPP) {
            return new CCKit();
        } else {
            return new CKit();
        }
    }
    private MimePath mimePathCpp;
    private final MimePath[] embeddingsPathCpp = new MimePath[4];
    private MimePath mimePathHeader;
    private final MimePath[] embeddingsPathHeader = new MimePath[4];
    private MimePath mimePathC;
    private final MimePath[] embeddingsPathC = new MimePath[4];
    private MimePath mimePathFortran;
    private MimePath mimePathAsm;

    @Override
    protected void setUpMime() {
        mimePathCpp = MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE);
        setUpWithEmbeddings(mimePathCpp, embeddingsPathCpp, new CCKit());
        mimePathHeader = MimePath.parse(MIMENames.HEADER_MIME_TYPE);
        setUpWithEmbeddings(mimePathHeader, embeddingsPathHeader, new HKit());
        mimePathC = MimePath.parse(MIMENames.C_MIME_TYPE);
        setUpWithEmbeddings(mimePathC, embeddingsPathC, new CKit());
        mimePathFortran = MimePath.parse(MIMENames.FORTRAN_MIME_TYPE);
        MockMimeLookup.setInstances(mimePathFortran, new FKit(), new FortranReformatter.Factory());
        mimePathAsm = MimePath.parse(MIMENames.ASM_MIME_TYPE);
        // TODO: add needed dependency in all dependant test cases to use real asm editor kit
        //MockMimeLookup.setInstances(mimePath5, new AsmEditorKit());
        MockMimeLookup.setInstances(mimePathAsm, new AsmStub());
    }

    private void setUpWithEmbeddings(MimePath mimePath, MimePath[] embeddings, NbEditorKit kit) {
        MockMimeLookup.setInstances(mimePath, kit, new Reformatter.Factory(), new CppIndentFactory(),
                new CppDTIFactory(), new CppTBIFactory(), new CppTTIFactory());
        embeddings[0] = MimePath.get(mimePath, MIMENames.DOXYGEN_MIME_TYPE);
        embeddings[1] = MimePath.get(mimePath, MIMENames.STRING_DOUBLE_MIME_TYPE);
        embeddings[2] = MimePath.get(mimePath, MIMENames.STRING_SINGLE_MIME_TYPE);
        embeddings[3] = MimePath.get(mimePath, MIMENames.PREPROC_MIME_TYPE);
        for (MimePath embMP : embeddings) {
            MockMimeLookup.setInstances(embMP, kit, new Reformatter.Factory(), new CppIndentFactory(),
                    new CppDTIFactory(), new CppTBIFactory(), new CppTTIFactory());
        }
    }

    private static final class AsmStub extends NbEditorKit {
        private AsmStub(){
        }
    }

    protected final void setCppEditorKit(boolean isCPP){
        this.isCPP = isCPP;
    }

    protected final void setDefaultsOptions(){
        // Note due to IZ#130533 the default style is changed. Hence we reset some properties.
        if (isCPP) {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument()));
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceNamespace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        } else {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C, getDocument()));
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBraceNamespace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        }
    }
    protected final void setDefaultsOptions(String style){
        if (isCPP) {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument()));
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument()), style);
        } else {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C, getDocument()));
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C, getDocument()), style);
        }
    }

    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        final Reformat f = Reformat.get(getDocument());
        final Runnable runnable = new Runnable() {

              @Override
              public void run() {
                  f.lock();
                  try {
                      f.reformat(0, getDocument().getLength());
                  } catch (BadLocationException e) {
                      e.printStackTrace(getLog());
                      fail(e.getMessage());
                  } finally {
                      f.unlock();
                  }
              }
          };
        if (getDocument() instanceof BaseDocument) {
            getDocument().runAtomic(runnable);
        } else {
            runnable.run();
        }
    }

    // ------- help methods -------------
    /**
    *
    * @param origTextWithPipe original text where "|" means caret position
    * @param typedText any text to type in editor character by character. There are special characters supported:
    * '\n' - insert new line
    * '\f' - delete
    * '\b' - backspace
    * @param resultTextWithPipe expected text where "|" means caret position
    */
    protected void typeCharactersInText(String origTextWithPipe, String typedText, String resultTextWithPipe) {
        Context context = new Context(getEditorKit(), origTextWithPipe);
        context.typeText(typedText);
        context.assertDocumentTextEquals(resultTextWithPipe);
    }
}
