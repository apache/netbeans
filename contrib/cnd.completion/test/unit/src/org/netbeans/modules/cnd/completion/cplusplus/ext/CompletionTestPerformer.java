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
package org.netbeans.modules.cnd.completion.cplusplus.ext;
import java.beans.PropertyVetoException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.LockForFile;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;

/**
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * Completion module API test: completion/CompletionTestPerformer
 * </B>
 * </FONT>
 *
 * <P>
 * <B>What it tests:</B><BR>
 * The purpose of this test is to test C/C++ code completion. This test
 * is done on some layer between user and API. It uses file and completion
 * is called on the top of the file, but it is never shown.
 * </P>
 *
 * <P>
 * <B>How it works:</B><BR>
 * TestFile is opened, given text is written to it, and code completion is
 * asked to return response for (row, col) position of document.
 * The type of completion is defined by the type of the file.
 * Unfortunately, it is not possible to ask completion for response
 * without opening the file.
 * </P>
 *
 * <P>
 * <B>Settings:</B><BR>
 * This test is not complete test, it's only stub, so for concrete test instance
 * it's necessary to provide text to add and whether the response should be
 * sorted. No more settings needed, when runned on clean build.
 * </P>
 *
 * <P>
 * <B>Output:</B><BR>
 * The output should be completion reponse in human readable form.
 * </P>
 *
 * <P>
 * <B>Possible reasons of failure:</B><BR>
 * <UL>
 * <LI>An exception when obtaining indent engine (for example if it doesn't exist).</LI>
 * <LI>An exception when writting to indent engine.</LI>
 * <LI>Possibly unrecognized MIME type.</LI>
 * <LI>Indent engine error.</LI>
 * <LI>The file can not be opened. This test must be able to open the file.
 * The test will fail if it is not able to open the file. In case it starts
 * opening sequence, but the editor is not opened, it may lock.</LI>
 * </UL>
 * </P>
 *
 *
 * (copy of Jan Lahoda CompletionTest.java)
 *
 *
 * @version 1.0
 */
public class CompletionTestPerformer {

    private static final long OPENING_TIMEOUT = 60 * 1000;
    private static final long SLEEP_TIME = 1000;
    private static final RequestProcessor RP = new RequestProcessor("CompletionTestPerformer", 1);

    private final CompletionResolver.QueryScope queryScope;
    /**
     * Creates new CompletionTestPerformer
     */
    public CompletionTestPerformer() {
        this(CompletionResolver.QueryScope.GLOBAL_QUERY);
    }

    public CompletionTestPerformer(CompletionResolver.QueryScope queryScope) {
        this.queryScope = queryScope;
    }

    protected CompletionItem[] completionQuery(
            PrintWriter  log,
            final JEditorPane  editor,
            final BaseDocument aDoc,
            final int caretOffset,
            final boolean unsorted,
            final boolean tooltip) throws InterruptedException {
        final BaseDocument doc = aDoc == null ? Utilities.getDocument(editor) : aDoc;
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
        assert csmFile != null : "Must be csmFile for document " + doc;
        final CsmCompletionQuery query = CsmCompletionProvider.getTestCompletionQuery(csmFile, this.queryScope);
        final AtomicReference<CsmCompletionQuery.CsmCompletionResult> res = new AtomicReference<CsmCompletionQuery.CsmCompletionResult>();
        RP.post(new Runnable() {
            @Override
            public void run() {
                res.set(query.query(editor, doc, caretOffset, tooltip, !unsorted, true, tooltip));
            }
        }).waitFinished(OPENING_TIMEOUT);

        CompletionItem[] array =  res.get() == null ? new CompletionItem[0] : res.get().getItems().toArray(new CompletionItem[res.get().getItems().size()]);
        assert array != null;
        return array;
    }

    /**Currently, this method is supposed to be runned inside the AWT thread.
     * If this condition is not fullfilled, an IllegalStateException is
     * thrown. Do NOT modify this behaviour, or deadlock (or even Swing
     * or NetBeans winsys data corruption) may occur.
     *
     * Currently threading model of this method is compatible with
     * editor code completion threading model. Revise if this changes
     * in future.
     */
    private CompletionTestResultItem[] testPerform(PrintWriter log,
            JEditorPane editor,
            BaseDocument doc,
            boolean unsorted,
            final String textToInsert, int offsetAfterInsertion,
            int lineIndex,
            int colIndex,
            boolean tooltip) throws BadLocationException, IOException, InterruptedException {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("The testPerform method may be called only inside AWT event dispatch thread.");
        }
        Logger.getLogger(FileLock.class.getName()).setLevel(Level.SEVERE);
        doc = doc == null ? Utilities.getDocument(editor) : doc;
        assert doc != null;
        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);

        if (textToInsert.length() > 0) {
            final int insOffset = offset;
            final BaseDocument insDoc = doc;
            final BadLocationException ex[] = new BadLocationException[] { null };
            insDoc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        insDoc.insertString(insOffset, textToInsert, null);
                    } catch (BadLocationException e) {
                        ex[0] = e;
                    }
                }
            });
            if (ex[0] != null) {
                throw ex[0];
            }
            String text = doc.getText(0, doc.getLength());
            parseModifiedFile((DataObject) doc.getProperty(BaseDocument.StreamDescriptionProperty),text);
            offset += textToInsert.length() + offsetAfterInsertion;
        }
        if (editor != null) {
            editor.grabFocus();
            editor.getCaret().setDot(offset);
        }

        CompletionItem items[] = completionQuery(log, editor, doc, offset, unsorted, tooltip);

        CompletionTestResultItem results[] = new CompletionTestResultItem[items.length];

        int lineBeginningOffset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, 1);
        JEditorPane textComponent = new JEditorPane();
        textComponent.setDocument(new BaseDocument(false, "text/plain"));

        for (int i = 0; i < items.length; i++) {
            textComponent.setText(doc.getText(0, doc.getLength()));

            items[i].defaultAction(textComponent);

            int lineEndingOffset = LineDocumentUtils.getLineEnd((BaseDocument) textComponent.getDocument(), offset);

            results[i] = new CompletionTestResultItem(items[i], textComponent.getText(lineBeginningOffset, lineEndingOffset - lineBeginningOffset));
        }

        return results;
    }

    public CompletionTestResultItem[] test(final PrintWriter log,
            final String textToInsert, final int offsetAfterInsertion, final boolean unsorted,
            final File testSourceFile, final int line, final int col, final boolean tooltip) throws Exception {
        try {
            final CompletionTestResultItem[][] array = new CompletionTestResultItem[][] {null};
            log.println("Completion test start.");
            log.flush();

            final FileObject testFileObject = getTestFile(testSourceFile, log);
            final DataObject testFile = DataObject.find(testFileObject);
            if (testFile == null) {
                throw new DataObjectNotFoundException(testFileObject);
            }
            try {
                final Throwable[] asserts = new Throwable[] { null };
                final BaseDocument doc = CndCoreTestUtils.getBaseDocument(testFile);
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            array[0] = testPerform(log, null, doc, unsorted, textToInsert, offsetAfterInsertion, line, col, tooltip);
                        } catch (IOException ex) {
                            ex.printStackTrace(log);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace(log);
                        } catch (Throwable as) {
                            asserts[0] = as;
                        }
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    run.run();
                } else {
                    try {
                        SwingUtilities.invokeAndWait(run);
                    } catch (InvocationTargetException invocationTargetException) {
                        if (invocationTargetException.getCause() != null) {
                            invocationTargetException.getCause().printStackTrace(System.err);
                        }
                    }
                }
                if (asserts[0] != null) {
                    new Exception("\nhappens in ", asserts[0]).printStackTrace(System.err);
                }
            } finally {
                testFile.setModified(false);
                FileLock lock = LockForFile.findValid(testSourceFile);
                if (lock != null) {
                    lock.releaseLock();
                }
                log.flush();
            }
            //((CloseCookie) testFile.getCookie(CloseCookie.class)).close();
            return array[0] == null ? new CompletionTestResultItem[0] : array[0];
        } catch (Exception e) {
            e.printStackTrace(log);
            throw e;
        }
    }

    private FileObject getTestFile(File testFile, PrintWriter log) throws IOException, InterruptedException, PropertyVetoException {
        FileObject test = CndFileUtils.toFileObject(testFile);
        CsmFile csmFile = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(test), true, false);
        if (test == null || !test.isValid() || csmFile == null) {
            throw new IllegalStateException("Given test file does not exist, file:" + testFile + ", FO: " + test + ", CsmFile: " + csmFile);
        }
        log.println("File found: " + csmFile);
        return test;
    }

    private static void parseModifiedFile(DataObject dob,String docText) throws IOException { //!!!WARNING: if this exception is thrown, the test may be locked (the file in editor may be modified, but not saved. problems with IDE finishing are supposed in this case).
//        SaveCookie sc = dob.getCookie(SaveCookie.class);
//        assert sc != null : "document must have save cookie " + dob;
//        if (sc != null) {
//            sc.save();
//        }
//        FileObject fo = dob.getPrimaryFile();
//        if (fo != null) {
//            InputStream is = fo.getInputStream();
//            int ch;
//            StringBuilder fileText = new StringBuilder();
//            while ((ch = is.read()) != -1) {
//                fileText.append((char)ch);
//            }
//            is.close();
//            String text = fileText.toString();
//            if (!text.equals(docText) && false) {
//                System.err.println("file after cookie saving " + fo.getPath() + "\ntext:\n" + fileText);
//                System.err.println("document after cookie saving " + fo.getPath() + "\ntext:\n" + docText);
//            }
//        }
        CsmFile csmFile = CsmUtilities.getCsmFile(dob, false, false);
        if (csmFile == null) {
            csmFile = CsmUtilities.getCsmFile(dob, false, false);
        }
        assert csmFile != null : "Must be csmFile for data object " + dob;
        CsmProject prj = csmFile.getProject();
        assert prj != null : "Must be project for csm file " + csmFile;
        prj.waitParse();
        assert csmFile.isParsed() : " file must be parsed: " + csmFile;
        assert prj.isStable(null) : " full project must be parsed" + prj;
    }
}
