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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Test for reaction on editor modifications
 */
public class ModifyDocumentTestCaseBase extends ProjectBasedTestCase {
    private final ObjectsChangeListener doListener = new ObjectsChangeListener();
    public ModifyDocumentTestCaseBase(String testName) {
        super(testName, true);
    }

    @Override
    protected void setUp() throws Exception {
        System.err.printf("setUp %s %d\n", getName(), System.currentTimeMillis());
        super.setUp();
        doListener.clear();
        DataObject.getRegistry().addChangeListener(doListener);
        ModelSupport.instance().startup();
        System.err.printf("setUp end %s %d\n", getName(), System.currentTimeMillis());
    }

    @Override
    protected void tearDown() throws Exception {
        System.err.printf("tearDown %s %d\n", getName(), System.currentTimeMillis());
        super.tearDown();
        ModelSupport.instance().shutdown();
        RepositoryTestUtils.deleteDefaultCacheLocation();
        TextIndexStorageManager.shutdown();
        DataObject.getRegistry().removeChangeListener(doListener);
        doListener.clear();
        System.err.printf("tearDown end %s %d\n", getName(), System.currentTimeMillis());
    }

    protected final void deleteTextThenUndo(final File sourceFile, final int startPos, final int endPos, int numDecls, int numDeclsAfterRemove) throws Exception {
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST UNDO OF DELETE BLOCK\n");
        }
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        CountDownLatch parse1 = new CountDownLatch(1);
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<>(parse1);
        Semaphore waitParseSemaphore = new Semaphore(0);
        final AtomicReference<Semaphore> semRef = new AtomicReference<>(waitParseSemaphore);
        final CsmProject project = super.getProject();
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        assertNotNull(doc);
        final int length = doc.getLength();
        assertTrue(length > 0);
        final int delLen;
        if (endPos < 0) {
            delLen = length;
        } else {
            delLen = Math.min(endPos, length)-startPos;
        }
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, semRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            int curNumDecls = fileImpl.getDeclarationsSize();
            assertEquals("different number of declarations", numDecls, curNumDecls);
            // insert dead code block
            // modify document
            final UndoManager urm = getUndoRedoManager(sourceFile);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        doc.remove(startPos, delLen);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            try {
                assertTrue("must have undo", urm.canUndo());
                checkModifiedObjects(1);
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    if (TraceFlags.TRACE_191307_BUG || TraceFlags.TRACE_191307_BUG) {
                        exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    }
                } else {
                    curNumDecls = fileImpl.getDeclarationsSize();
                    assertEquals("different number of declarations", numDeclsAfterRemove, curNumDecls);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                if (exRef.get() == null) {
                    // let's undo changes
                    closeDocument(sourceFile, urm, doc, project, null);
                    curNumDecls = fileImpl.getDeclarationsSize();
                    assertEquals("different number of declarations after undo", numDecls, curNumDecls);
                }
            }
        } finally {
            System.err.flush();
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    protected static interface DocumentModifier {
        public abstract void modify(BaseDocument doc) throws BadLocationException;
    }

    protected final void replaceText(final File sourceFile, final String text, boolean save) throws Exception {
        modifyText(sourceFile, new DocumentModifier() {
            @Override
            public void modify(BaseDocument doc) throws BadLocationException {
                doc.replace(0, doc.getLength(), text, null);
            }
        }, save);
    }

    protected final void modifyText(final File sourceFile, final DocumentModifier docModifier, final boolean save) throws Exception {
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        CountDownLatch parse1 = new CountDownLatch(1);
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<>(parse1);
        Semaphore waitParseSemaphore = new Semaphore(0);
        final AtomicReference<Semaphore> semRef = new AtomicReference<>(waitParseSemaphore);
        final CsmProject project = super.getProject();
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);        
        assertNotNull(doc);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, semRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        docModifier.modify(doc);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            try {
                checkModifiedObjects(1);
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    //if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    //    exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    //}
                } else {
                    //checkDeadBlocks(project, fileImpl, "2. text after inserting dead block:", doc, "File must have " + deadBlocksAfterModifications + " dead code block ", deadBlocksAfterModifications);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
                if (save) {
                    saveDocument(sourceFile, doc, project);
                    parse1.await(20, TimeUnit.SECONDS);
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                closeDocument(sourceFile, null, doc, project, listener);
            }
        } finally {
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }
    
    protected final void insertDeadBlockText(final File sourceFile, final String ifdefTxt, final int pos,
                                    int deadBlocksBeforeModifcation, int deadBlocksAfterModifications) throws Exception {
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("TEST INSERT DEAD BLOCK\n");
        }
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        CountDownLatch parse1 = new CountDownLatch(1);
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<>(parse1);
        Semaphore waitParseSemaphore = new Semaphore(0);
        final AtomicReference<Semaphore> semRef = new AtomicReference<>(waitParseSemaphore);
        final CsmProject project = super.getProject();
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        assertNotNull(doc);
        assertTrue(doc.getLength() > 0);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, semRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            checkDeadBlocks(project, fileImpl, "1. text before inserting dead block:", doc, "File must have " + deadBlocksBeforeModifcation + " dead code blocks ", deadBlocksBeforeModifcation);

            // insert dead code block
            // modify document
            final UndoManager urm = getUndoRedoManager(sourceFile);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (TraceFlags.TRACE_182342_BUG) {
                            System.err.printf("Inserting dead block in position %d: %s\n", pos, ifdefTxt);
                        }
                        doc.insertString(pos,
                                        ifdefTxt,
                                        null);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });

            try {
                assertTrue("must have undo", urm.canUndo());
                checkModifiedObjects(1);
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                        exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    }
                } else {
                    checkDeadBlocks(project, fileImpl, "2. text after inserting dead block:", doc, "File must have " + deadBlocksAfterModifications + " dead code block ", deadBlocksAfterModifications);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                closeDocument(sourceFile, urm, doc, project, listener);
            }
        } finally {
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    protected final void removeDeadBlock(final File sourceFile, int deadBlocksBeforeRemove, int deadBlocksAfterRemove) throws Exception {
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        CountDownLatch parse1 = new CountDownLatch(1);
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<>(parse1);
        Semaphore waitParseSemaphore = new Semaphore(0);
        final AtomicReference<Semaphore> semRef = new AtomicReference<>(waitParseSemaphore);
        final CsmProject project = super.getProject();
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        assertNotNull(doc);
        assertTrue(doc.getLength() > 0);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, semRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {

            final List<CsmOffsetable> unusedCodeBlocks = checkDeadBlocks(project, fileImpl, "1. text before deleting dead block:", doc, "File must have " + deadBlocksBeforeRemove + " dead code block ", deadBlocksBeforeRemove);
            // insert dead code block
            // modify document
            final UndoManager urm = getUndoRedoManager(sourceFile);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        for (CsmOffsetable block : unusedCodeBlocks) {
                            if (TraceFlags.TRACE_182342_BUG) {
                                System.err.printf("Removing dead block [%d-%d]\n", block.getStartOffset(), block.getEndOffset());
                            }
                            doc.remove(block.getStartOffset(), block.getEndOffset() - block.getStartOffset());
                        }
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            try {
                assertTrue("must have undo", urm.canUndo());
                checkModifiedObjects(1);
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                        exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    }
                } else {
                    checkDeadBlocks(project, fileImpl, "2. text after deleting dead block:", doc, "File must have " + deadBlocksAfterRemove + " dead code blocks ", deadBlocksAfterRemove);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                closeDocument(sourceFile, urm, doc, project, listener);
            }
        } finally {
            System.err.flush();
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    private void checkModifiedObjects(int expected) {
        assertEquals("unexpected number of modified objects:\n" + Arrays.toString(DataObject.getRegistry().getModified()) + "\n Our List:\n" + this.doListener.getModified() + "\n", expected, this.doListener.size());
    }

    private void saveDocument(final File sourceFile, final BaseDocument doc, final CsmProject project) throws DataObjectNotFoundException, BadLocationException, IOException {
        DataObject testDataObject = DataObject.find(CndFileUtils.toFileObject(sourceFile));
        SaveCookie save = testDataObject.getLookup().lookup(SaveCookie.class);
        assertNotNull(save);
        save.save();
        if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
            System.err.printf("document text after save\n==============\n%s\n===============\n", doc.getText(0, doc.getLength()));
        }
        project.waitParse();
    }
        
    private void closeDocument(final File sourceFile, final UndoManager urm, final BaseDocument doc, final CsmProject project, final CsmProgressListener listener) throws DataObjectNotFoundException, BadLocationException {
        if (listener != null) {
            CsmListeners.getDefault().removeProgressListener(listener);
        }
        if (urm != null) {
            urm.undo();
        }
        DataObject testDataObject = DataObject.find(CndFileUtils.toFileObject(sourceFile));
        CloseCookie close = testDataObject.getLookup().lookup(CloseCookie.class);
        if (close != null) {
            close.close();
        }
        if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
            System.err.printf("document text after close\n==============\n%s\n===============\n", doc.getText(0, doc.getLength()));
        }
        project.waitParse();
    }
    
    private CsmProgressListener createFileParseListener(final FileImpl fileImpl, final AtomicReference<CountDownLatch> condRef, final AtomicReference<Semaphore> semRef, final AtomicInteger parseCounter) {
        final CsmProgressListener listener = new CsmProgressAdapter() {

            @Override
            public void fileParsingFinished(CsmFile file) {
                if (TraceFlags.TRACE_182342_BUG) {
                    new Exception(getName() + " fileParsingFinished " + file).printStackTrace(System.err); // NOI18N
                }
                parseCounter.incrementAndGet();
                if (file.equals(fileImpl)) {
                    CountDownLatch cond = condRef.get();
                    cond.countDown();
                    Semaphore sem = semRef.get();
                    sem.release();
                }
            }
        };
        return listener;
    }

    protected static void checkDeclarationNumber(int num, final FileImpl fileImpl) {
        assertEquals("different number of declarations", num, fileImpl.getDeclarationsSize());
    }

    protected static List<CsmOffsetable> checkDeadBlocks(final CsmProject project, final FileImpl fileImpl, String docMsg, final BaseDocument doc, String msg, int expectedDeadBlocks) throws BadLocationException {
        project.waitParse();
        List<CsmOffsetable> unusedCodeBlocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(fileImpl, Interrupter.DUMMY);
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("%s\n==============\n%s\n===============\n", docMsg, doc.getText(0, doc.getLength()));
            if (unusedCodeBlocks.isEmpty()) {
                System.err.println("NO DEAD BLOCKS");
            } else {
                int i = 0;
                for (CsmOffsetable csmOffsetable : unusedCodeBlocks) {
                    System.err.printf("DEAD BLOCK %d: [%d-%d]\n", i++, csmOffsetable.getStartOffset(), csmOffsetable.getEndOffset());
                }
            }
        }
        assertEquals(msg + fileImpl.getAbsolutePath(), expectedDeadBlocks, unusedCodeBlocks.size());
        return unusedCodeBlocks;
    }
    
    protected static interface Checker {
        void checkBeforeModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException;
        void checkAfterModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException;
        void checkAfterParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException;
        void checkAfterUndo(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException;
        void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException;
    }
    
    protected static final class DeclarationsNumberChecker implements Checker {
        private final int numBefore;
        private final int numAfter;

        public DeclarationsNumberChecker(int numBefore, int numAfter) {
            this.numBefore = numBefore;
            this.numAfter = numAfter;
        }
        
        @Override
        public void checkBeforeModifyingFile(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) {
            checkDeclarationNumber(numBefore, fileToCheck);
        }

        @Override
        public void checkAfterModifyingFile(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) {
            checkDeclarationNumber(numAfter, fileToCheck);
        }

        @Override
        public void checkAfterParseFinished(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) {
            checkDeclarationNumber(numAfter, fileToCheck);
        }

        @Override
        public void checkAfterUndo(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) {
            checkBeforeModifyingFile(modifiedFile, fileToCheck, project, doc);
        }

        @Override
        public void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkAfterParseFinished(modifiedFile, fileToCheck, project, doc);
        }
    };
    
    protected static final class DeadBlocksNumberChecker implements Checker {
        private final int numBefore;
        private final int numAfter;

        public DeadBlocksNumberChecker(int numBefore, int numAfter) {
            this.numBefore = numBefore;
            this.numAfter = numAfter;
        }
        
        @Override
        public void checkBeforeModifyingFile(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) throws BadLocationException {
            checkDeadBlocks(project, fileToCheck, "1. text before:", doc, "File must have " + numBefore + " dead code block ", numBefore);
        }

        @Override
        public void checkAfterModifyingFile(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) throws BadLocationException {
        }

        @Override
        public void checkAfterParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkDeadBlocks(project, fileToCheck, "2. text after:", doc, "File must have " + numAfter + " dead code blocks ", numAfter);
        }

        @Override
        public void checkAfterUndo(FileImpl modifiedFile, final FileImpl fileToCheck, final CsmProject project, final BaseDocument doc) throws BadLocationException {
            checkBeforeModifyingFile(modifiedFile, fileToCheck, project, doc);
        }

        @Override
        public void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkAfterParseFinished(modifiedFile, fileToCheck, project, doc);
        }
    };

    protected void insertTextThenSaveAndCheck(File modifiedFile,
                                              final int insertLine,
                                              final String insertString,
                                              File checkedFile,
                                              Checker checker,
                                              boolean doUndoRedo) throws Exception {
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST INSERT/SAVE then UNDO/REDO\n");
        }
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        CountDownLatch parse1 = new CountDownLatch(1);
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<>(parse1);
        Semaphore waitParseSemaphore = new Semaphore(0);
        final AtomicReference<Semaphore> semRef = new AtomicReference<>(waitParseSemaphore);
        final FileImpl fileToModifyImpl = (FileImpl) getCsmFile(modifiedFile);
        final CsmProject project = fileToModifyImpl.getProject();
        final FileImpl fileToCheckImpl = (FileImpl) getCsmFile(checkedFile);
        assertNotNull(fileToModifyImpl);
        final BaseDocument modifiedDoc = getBaseDocument(modifiedFile);
        final UndoManager urm = getUndoRedoManager(modifiedFile);
        final int insertOffset = CndCoreTestUtils.getDocumentOffset(modifiedDoc, insertLine, 1);
        assertNotNull(modifiedDoc);
        final BaseDocument checkedDoc = getBaseDocument(modifiedFile);
        assertNotNull(checkedDoc);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileToModifyImpl, condRef, semRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            checker.checkBeforeModifyingFile(fileToModifyImpl, fileToCheckImpl, project, checkedDoc);
            // modify document
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        modifiedDoc.insertString(insertOffset, insertString, null);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            assertTrue("must have undo", urm.canUndo());
            checkModifiedObjects(1);
            if (!parse1.await(20, TimeUnit.SECONDS)) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    exRef.compareAndSet(null, new TimeoutException("not finished await"));
                }
                return;
            }            
            waitParseSemaphore.acquire();
//            waitParseSemaphore.acquire();
            assertTrue("file not yet parsed at this time" + fileToModifyImpl.getParsingStateFromTest() + fileToModifyImpl.getStateFromTest(), fileToModifyImpl.isParsed());
            checker.checkAfterModifyingFile(fileToModifyImpl, fileToCheckImpl, project, checkedDoc);
//            assertEquals("must be exactly one parse event", 1, parseCounter.get());
            project.waitParse();
            while(waitParseSemaphore.tryAcquire()) {}
            // let's save changes
            saveDocument(modifiedFile, modifiedDoc, project);
            waitParseSemaphore.acquire();
            project.waitParse();
            while(waitParseSemaphore.tryAcquire()) {}
            assertTrue("file not yet parsed at this time" + fileToModifyImpl.getParsingStateFromTest() + fileToModifyImpl.getStateFromTest(), fileToModifyImpl.isParsed());
            checker.checkAfterParseFinished(fileToModifyImpl, fileToCheckImpl, project, checkedDoc);
            checkModifiedObjects(0);
            
            assertTrue("must have undoable modification", urm.canUndo());
            if (doUndoRedo) {
                urm.undo();          
                waitParseSemaphore.acquire();
                project.waitParse();
                while(waitParseSemaphore.tryAcquire()) {}
                assertTrue("file not yet parsed at this time" + fileToModifyImpl.getParsingStateFromTest() + fileToModifyImpl.getStateFromTest(), fileToModifyImpl.isParsed());
                checker.checkAfterUndo(fileToModifyImpl, fileToCheckImpl, project, checkedDoc);
    //            assertEquals("must be exactly three parse events", 3, parseCounter.get());
                checkModifiedObjects(1);

                assertTrue("must have redoable modification", urm.canRedo());
                urm.redo();
                waitParseSemaphore.acquire();
                project.waitParse();
                while(waitParseSemaphore.tryAcquire()) {}
                assertTrue("file not yet parsed at this time" + fileToModifyImpl.getParsingStateFromTest() + fileToModifyImpl.getStateFromTest(), fileToModifyImpl.isParsed());
                checker.checkAfterParseFinished(fileToModifyImpl, fileToCheckImpl, project, checkedDoc);
                checkModifiedObjects(0);
                project.waitParse();
            }
        } finally {
            System.err.flush();
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    private static final class ObjectsChangeListener implements ChangeListener {
        private final Set<DataObject> modifiedDOs = new HashSet<>();
        @Override
        public synchronized void stateChanged(ChangeEvent e) {
            DataObject[] objs = DataObject.getRegistry().getModified();
            modifiedDOs.clear();
            modifiedDOs.addAll(Arrays.asList(objs));
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                System.err.println("ObjectsChangeListener: stateChanged " + e);
                ModelSupport.traceDataObjectRegistryStateChanged(e);
            }
        }

        public synchronized Collection<DataObject> getModified() {
            return new ArrayList<>(modifiedDOs);
        }
        
        public synchronized void clear() {
            modifiedDOs.clear();
        }

        public synchronized int size() {
            return modifiedDOs.size();
        }
    }
}
