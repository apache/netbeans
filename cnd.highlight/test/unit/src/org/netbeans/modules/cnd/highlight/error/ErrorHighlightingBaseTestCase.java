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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.error;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.highlight.hints.DisableHintFix;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferDoc;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileUtil;

/**
 * 
 */
public class ErrorHighlightingBaseTestCase extends ProjectBasedTestCase {

    protected static final boolean TRACE = true; // Boolean.getBoolean("cnd.error.hl.tests.trace");


    public ErrorHighlightingBaseTestCase(String testName) {
        super(testName, true);
    }

    protected Collection<CsmErrorInfo> getErrors(BaseDocument doc, CsmFile csmFile) {
        final List<CsmErrorInfo> result = new ArrayList<>();
        CsmErrorProvider.Request request = new HighlightProvider.RequestImpl(csmFile, doc, CsmErrorProvider.EditorEvent.FileBased, Interrupter.DUMMY);
        CsmErrorProvider.Response response = new CsmErrorProvider.Response() {
            @Override
            public void addError(CsmErrorInfo info) {
                result.add(info);
            }
            @Override
            public void done() {
            }
        };
        CsmErrorProvider.getAllErrors(request, response);
        Collections.sort(result, new Comparator<CsmErrorInfo>() {
            @Override
            public int compare(CsmErrorInfo o1, CsmErrorInfo o2) {
                if (o1.getStartOffset() == o2.getStartOffset()) {
                    return o1.getMessage().compareTo(o2.getMessage());
                } else {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            }
        });
        return result;
    }

    protected final void performStaticTest(String sourceFileName) throws Exception {
        String datafileName = sourceFileName.replace('/', '_').replace('\\', '_') + ".dat";
        File testSourceFile = getDataFile(sourceFileName);
        File workDir = getWorkDir();
        File output = new File(workDir, datafileName);
        PrintStream out = new PrintStream(output);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        Collection<CsmErrorInfo> errorInfos = getErrors(doc, csmFile);
        for (CsmErrorInfo info : errorInfos) {
            String txt = String.format("%s %s [%d-%d]: %s", info.getSeverity(), sourceFileName, info.getStartOffset(), info.getEndOffset(), info.getMessage());
            out.printf("%s\n", txt);
            if (TRACE) {
                System.err.printf("%s\n", txt);
            }
        }
        compareReferenceFiles(datafileName, datafileName);
    }
    
    protected Collection<Fix> getFixes(BaseDocument doc, CsmFile csmFile) {
        final List<Fix> result = new ArrayList<>();
        Collection<CsmErrorInfo> errors = getErrors(doc, csmFile);        
        for (CsmErrorInfo error : errors) {
            result.addAll(CsmErrorInfoHintProvider.getFixes(error));
        }
        
        return result;
    }
    
    protected final void performFixesTest(String sourceFileName) throws Exception {
        String datafileName = sourceFileName.replace('/', '_').replace('\\', '_') + ".dat";
        File testSourceFile = getDataFile(sourceFileName);
        File workDir = getWorkDir();
        File output = new File(workDir, datafileName);
        PrintStream out = new PrintStream(output);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        Collection<Fix> fixes = getFixes(doc, csmFile);
        for (Fix fix : fixes) {
            if(!(fix instanceof DisableHintFix)) {
                out.printf("%s\n", fix.getText());
                if (TRACE) {
                    System.err.printf("%s\n", fix.getText());
                }
            }
        }
        compareReferenceFiles(datafileName, datafileName);
    }

    protected final void performDynamicTest(String sourceFileName, ErrorMaker errorMaker) throws Exception {
        File testSourceFile = getDataFile(sourceFileName);
        CsmFile csmFile = getCsmFile(testSourceFile);
        performDynamicTest(csmFile, errorMaker);
    }
    
    protected final void performDynamicTest(CsmFile csmFile, ErrorMaker errorMaker) throws Exception {
        File testSourceFile = new File(csmFile.getAbsolutePath().toString());
        BaseDocument doc = getBaseDocument(testSourceFile);

        // TODO: find more elegant solution than setting buffer explicitely
        FileBufferDoc buffer = new FileBufferDoc(FileUtil.toFileObject(testSourceFile), doc);
        ((FileImpl) csmFile).setBuffer(buffer);

        Collection<CsmErrorInfo> errorInfos;
        
        errorInfos = getErrors(doc, csmFile);
        //if (TRACE) trace("INITIAL:", errorInfos, sourceFileName);
        assertTrue("The shouldn't be errors in the initial state", errorInfos.isEmpty()); //NOI18N
        
        Undoer undoer = new Undoer(doc);
        errorMaker.init(doc, csmFile);
        while (errorMaker.change()) {
            if (TRACE) trace("\n\n==========", doc);
            //parseModifiedFile((DataObject) doc.getProperty(BaseDocument.StreamDescriptionProperty));
            errorInfos = getErrors(doc, csmFile);
            if (TRACE) trace("----------", errorInfos, testSourceFile.getName());
            errorMaker.analyze(errorInfos);
            if (undoer.canUndo()) {
                undoer.undo();
                errorMaker.undone();
            } else {
                throw new IllegalStateException("can not undo"); //NOI18N
            }
        }
    }

//    private static void parseModifiedFile(DataObject dob) throws IOException { 
//        CsmFile csmFile = CsmUtilities.getCsmFile(dob, false);
//        assert csmFile != null : "Must be csmFile for data object " + dob;
//        CsmProject prj = csmFile.getProject();
//        assert prj != null : "Must be project for csm file " + csmFile;
//        prj.waitParse();
//        assert csmFile.isParsed() : " file must be parsed: " + csmFile;
//        assert prj.isStable(null) : " full project must be parsed" + prj;
//    }
    

    protected void trace(String title, BaseDocument doc) throws BadLocationException {
        String text = doc.getText(0, doc.getLength());
//        StringTokenizer tokenizer = new StringTokenizer(text, System.getProperty("line.separator"));
//        int lineNo = 1;
//        while (tokenizer.hasMoreTokens()) {
//            String lineText = tokenizer.nextToken();
//            System.err.printf(" %s\n", lineNo, lineText);
//        }
        System.err.printf("%s\n%s\n", title, text);
    }
    
    protected void trace(String title, Collection<CsmErrorInfo> errorInfos, String sourceFileName) {
        System.err.printf("%s\n", title);
        trace(errorInfos, sourceFileName);
    }
    
    protected void trace(Collection<CsmErrorInfo> errorInfos, String sourceFileName) {
        for (CsmErrorInfo info : errorInfos) {
            System.err.printf("%s\n", toString(info, sourceFileName));
        }
    }

    protected String toString(CsmErrorInfo info, String sourceFileName) {
        return String.format("%s %s [%d-%d]: %s", info.getSeverity(), sourceFileName, info.getStartOffset(), info.getEndOffset(), info.getMessage());
    }

    /**
     * Performs undo for changes that are made in a document
     */
    private static class Undoer implements DocumentListener {

        BaseDocument document;
        Stack<DocumentEvent> events = new Stack<>();

        public Undoer(BaseDocument document) {
            this.document = document;
            document.addDocumentListener(this);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            events.add(e);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            events.add(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            events.add(e);
        }

        public boolean canUndo() {
            for (DocumentEvent e : events) {
                if (!canUndo(e)) {
                    return false;
                }
            }
            return true;
        }

        public void undo() {
            document.removeDocumentListener(this);
            while (!events.empty()) {
                DocumentEvent e = events.pop();
                if (e instanceof BaseDocumentEvent) {
                    ((BaseDocumentEvent) e).undo();
                } else {
                    throw new IllegalStateException("Can not undo"); //NOI18N

                }
            }
            events.clear();
            document.addDocumentListener(this);
        }

        private boolean canUndo(DocumentEvent e) {
            if (e instanceof BaseDocumentEvent) {
                return ((BaseDocumentEvent) e).canUndo();
            }
            return false;
        }
    }    
}
