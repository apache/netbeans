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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences.Visitor;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Semantic C/C++ code highlighter responsible for "graying out"
 * inactive code due to preprocessor definitions and highlighting of unobvious
 * language elements.
 *
 */
public final class SemanticHighlighter extends HighlighterBase {
    private static final String SLOW_POSITION_BAG = "CndSemanticHighlighterSlow"; // NOI18N
    private static final String FAST_POSITION_BAG = "CndSemanticHighlighterFast"; // NOI18N
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("SemanticHighlighter profiler",1); //NOI18N
    private static final RequestProcessor WORKER = new RequestProcessor("SemanticHighlighter worker",1); //NOI18N
    private static final boolean VALIDATE = false;
    private final TaskContext taskContext;
    private final RequestProcessor.Task task;
    
    private final CancelSupport cancel = CancelSupport.create(this);
    private InterrupterImpl interrupter = new InterrupterImpl();
    private Parser.Result lastParserResult;
    private CountDownLatch latch = new CountDownLatch(1);
    private Set<Map.Entry<Thread, StackTraceElement[]>> stack;
    private AtomicBoolean done = new AtomicBoolean(false);

    public SemanticHighlighter(String mimeType) {
        init(mimeType);
        taskContext = new TaskContext(this);
        task = WORKER.create(taskContext);
    }

    @Override
    protected void updateFontColors(FontColorProvider provider) {
        for (SemanticEntity semanticEntity : SemanticEntitiesProvider.instance().get()) {
            semanticEntity.updateFontColors(provider);
        }
    }

    public static PositionsBag getHighlightsBag(Document doc, boolean fast) {
        if (doc == null) {
            return null;
        }
        final String name = fast ? FAST_POSITION_BAG : SLOW_POSITION_BAG;

        PositionsBag bag = (PositionsBag) doc.getProperty(name);

        if (bag == null) {
            doc.putProperty(name, bag = new PositionsBag(doc));
        }

        return bag;
    }

    private static final int MAX_LINE_NUMBER;

    static {
        String limit = System.getProperty("cnd.semantic.line.limit"); // NOI18N
        int userInput = 5000;
        if (limit != null) {
            try {
                userInput = Integer.parseInt(limit);
            } catch (Exception e) {
                // skip
            }
        }
        MAX_LINE_NUMBER = userInput;
    }

    public static boolean isVeryBigDocument(Document doc) {
        if (!(doc instanceof BaseDocument) || MAX_LINE_NUMBER < 0) {
            return false;
        }
        try {
            if (doc.getLength() < MAX_LINE_NUMBER) {
                return false;
            }
            return LineDocumentUtils.getLineIndex((BaseDocument)doc, doc.getLength() - 1) > MAX_LINE_NUMBER;
        } catch (BadLocationException ex) {
            // skip
            return true;
        }
    }

    public static PositionsBag getSemanticBagForTests(Document doc, InterrupterImpl interrupter, boolean fast) {
        if (doc != null) {
            SemanticHighlighter semanticHighlighter = new SemanticHighlighter(DocumentUtilities.getMimeType(doc));
            updateImpl(semanticHighlighter, doc, interrupter);
        }
        return getHighlightsBag(doc, fast);
    }

    private static void updateImpl(SemanticHighlighter provider, Document doc, final InterrupterImpl interrupter) {
        if (doc == null) {
            return;
        }
        boolean macroExpansionView = (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null);
        PositionsBag newBagFast = new PositionsBag(doc);
        PositionsBag newBagSlow = new PositionsBag(doc);
        final CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
        long start = System.currentTimeMillis();
        if (csmFile != null && csmFile.isParsed()) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Semantic Highlighting update() have started for file {0}", csmFile.getAbsolutePath());
            }
            final List<SemanticEntity> entities = new ArrayList<>(SemanticEntitiesProvider.instance().get());
            final List<ReferenceCollector> collectors = new ArrayList<>(entities.size());
            // the following loop deals with entities without collectors
            // and gathers collectors for the next step
            for (Iterator<SemanticEntity> i = entities.iterator(); i.hasNext(); ) {
                if (interrupter.cancelled()) {
                    break;
                }
                SemanticEntity se = i.next();
                if (NamedOption.getAccessor().getBoolean(se.getName()) && 
                        (!macroExpansionView || !se.getName().equals(SemanticEntitiesProvider.MacrosCodeProvider.NAME))) { // NOI18N
                    ReferenceCollector collector = se.getCollector(doc, interrupter);
                    if (collector != null) {
                        // remember the collector for future use
                        collectors.add(collector);
                    } else {
                        // this is simple entity without collector,
                        // let's add its blocks right now
                        provider.addHighlightsToBag(doc, newBagFast, se.getBlocks(csmFile, doc, interrupter), se);
                        i.remove();
                    }
                } else {
                    // skip disabled entity
                    i.remove();
                }
            }
            // to show inactive code and macros first
            if (!interrupter.cancelled()){
                getHighlightsBag(doc, true).setHighlights(newBagFast);
                // here we invoke the collectors
                // but not for huge documents
                if (!entities.isEmpty() && !isVeryBigDocument(doc)) {
                    CsmFileReferences.getDefault().accept(csmFile, doc, new Visitor() {
                        @Override
                        public void visit(CsmReferenceContext context) {
                            CsmReference ref = context.getReference();
                            for (ReferenceCollector c : collectors) {
                                if (interrupter.cancelled()) {
                                    break;
                                }
                                c.visit(ref, csmFile);
                            }
                        }

                        @Override
                        public boolean cancelled() {
                            return interrupter.cancelled();
                        }
                    }, CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE_AND_PREPROCESSOR);
                    // here we apply highlighting to discovered blocks
                    for (int i = 0; i < entities.size(); ++i) {
                        provider.addHighlightsToBag(doc, newBagSlow, collectors.get(i).getReferences(), entities.get(i));
                    }
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Semantic Highlighting update() done in {0}ms for file {1}", new Object[]{System.currentTimeMillis() - start, csmFile.getAbsolutePath()});
                }
                if (!interrupter.cancelled()){
                    getHighlightsBag(doc, false).setHighlights(newBagSlow);
                }
            }
        }
    }

    private void addHighlightsToBag(Document doc, PositionsBag bag, List<? extends CsmOffsetable> blocks, SemanticEntity entity) {
        if (doc != null) {
            String mimeType = DocumentUtilities.getMimeType(doc);
            if (mimeType == null) {
                mimeType = MIMENames.CPLUSPLUS_MIME_TYPE;
            }
           for (CsmOffsetable block : blocks) {
                int startOffset = block.getStartOffset();
                int endOffset = block.getEndOffset();
                if (endOffset == Integer.MAX_VALUE) {
                    endOffset = doc.getLength() + 1;
                }
                if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null || entity.isCsmFileBased()) {
                    startOffset = getDocumentOffset(doc, startOffset);
                    endOffset = getDocumentOffset(doc, endOffset);
                }
                if (startOffset < doc.getLength() && startOffset >= 0 && startOffset < endOffset) {
                    final AttributeSet attributes = entity.getAttributes(block, mimeType);
                    if (attributes == null) {
                        assert false : "Color attributes set is not found for MIME "+mimeType+". Document "+doc;
                        return;
                    }
                    addHighlightsToBag(doc, bag, startOffset, endOffset, attributes, entity.getName());
                    if (VALIDATE) {
                        // unfortunately check does not work for destructors and operators.
                        try {
                            String text = doc.getText(startOffset, endOffset-startOffset);
                            if (!text.equals(block.getText().toString())) {
                                System.err.println(getInfo(doc, block, startOffset, endOffset));
                                System.err.println("Should be "+text); //NOI18N
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } else if (startOffset < doc.getLength() && startOffset >= 0 && startOffset > endOffset) {
                    // something wrong in transformation table?
                    CndUtils.assertTrueInConsole(false, getInfo(doc, block, startOffset, endOffset));
                }
            }
        }
    }

    private String getInfo(Document doc, CsmOffsetable block, int startOffset, int endOffset) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
        String start = Utilities.offsetToLineColumnString((BaseDocument) doc, startOffset);
        String end = Utilities.offsetToLineColumnString((BaseDocument) doc, endOffset);
        return "Block ["+block.getStartPosition().getLine()+":"+block.getStartPosition().getColumn()+"-"+ //NOI18N
                         block.getEndPosition().getLine()+":"+block.getEndPosition().getColumn()+"] "+ //NOI18N
                         block.getText().toString()+" transformed to ["+start+"-"+end+"] in file "+csmFile.getAbsolutePath(); //NOI18N
    }
    
    private void addHighlightsToBag(Document doc, PositionsBag bag, int start, int end, AttributeSet attr, String nameToStateInLog) {
        try {
            if (doc != null) {
                if (SemanticEntitiesProvider.InactiveCodeProvider.INACTIVE_NAME.equals(nameToStateInLog)) {
                    // inactive code bias is <-[code]->
                    bag.addHighlight(NbDocument.createPosition(doc, start, Position.Bias.Backward), 
                                     NbDocument.createPosition(doc, end, Position.Bias.Forward), attr);
                } else {
                    // all others use bias as [-> ID <-]
                    bag.addHighlight(NbDocument.createPosition(doc, start, Position.Bias.Forward), 
                                     NbDocument.createPosition(doc, end, Position.Bias.Backward), attr);
                }
            }
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Can't add highlight <" + start + ", " + end + ", " + nameToStateInLog + ">", ex);
        }
    }

    private static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized(this) {
            if (lastParserResult == result) {
                return;
            }
            interrupter.cancel();
            latch.countDown();
            
            interrupter = new InterrupterImpl();
            latch = new CountDownLatch(1);
            if (cancel.isCancelled()) {
                lastParserResult = null;
                LOG.log(Level.FINE, "SemanticHighlighter have been canceled before start, Task={0}, Result={1}", new Object[]{System.identityHashCode(this), System.identityHashCode(result)}); //NOI18N
                return;
            } else {
                lastParserResult = result;
            }
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "SemanticHighlighter started, Task={0}, Result={1}", new Object[]{System.identityHashCode(this), System.identityHashCode(result)}); //NOI18N
            time = System.currentTimeMillis();
            done = new AtomicBoolean(false);
        }
        // no sync needed: all these fields are assigned only in this method
        // and parser API calls this in a single parsing loop thread ("Editor Parsing Loop")
        taskContext.prepare(result.getSnapshot().getSource().getDocument(false), interrupter, latch);
        task.schedule(0);
        try {
            latch.await();
        } catch (InterruptedException ex) {
        }
        if (LOG.isLoggable(Level.FINE)) {
            done.set(true);
            LOG.log(Level.FINE, "SemanticHighlighter finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
        }
    }

    @Override
    public void cancel() {
        synchronized(this) {
            interrupter.cancel();
            lastParserResult = null;
            latch.countDown();
        }
        if (LOG.isLoggable(Level.FINE)) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    AtomicBoolean aDone = SemanticHighlighter.this.done;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        //
                    }
                    if (!aDone.get()) {
                        stack = Thread.getAllStackTraces().entrySet();
                        StringBuilder buf = new StringBuilder();
                        boolean printHeader = false;
                        for (Map.Entry<Thread, StackTraceElement[]> entry : stack) {
                            if (entry.getKey().getName().startsWith("SemanticHighlighter worker")|| //NOI18N
                                entry.getKey().getName().startsWith("Editor Parsing Loop")) { //NOI18N
                                if (!printHeader) {
                                    buf.append("What have been semantic provider doing for 100 ms after canceling?"); //NOI18N
                                }
                                printHeader = true;
                                buf.append("\nThread ").append(entry.getKey().getName()); //NOI18N
                                for (StackTraceElement element : entry.getValue()) {
                                    buf.append("\n\tat " + element.toString()); //NOI18N
                                }
                            }
                        }
                        if (buf.length()>0) {
                            LOG.log(Level.FINE, buf.toString());
                        }
                    }
                }
            });
            LOG.log(Level.FINE, "SemanticHighlighter canceled in {0}, Task={1}", new Object[]{Thread.currentThread().getName(), System.identityHashCode(this)}); //NOI18N
        }
    }

    @Override
    public int getPriority() {return 300;}

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
    
    @Override
    public String toString() {
        return "SemanticHighlighter runner"; //NOI18N
    }
    
    private static final class TaskContext implements Runnable {
        private final SemanticHighlighter provider;
        private volatile InterrupterImpl interrupter;
        private volatile Document doc;
        private volatile CountDownLatch latch;
        private TaskContext(SemanticHighlighter provider) {
            this.provider = provider;
        }
        
        private void prepare(Document doc, InterrupterImpl interrupter, CountDownLatch latch) {
            this.interrupter = interrupter;
            this.doc = doc;
            this.latch = latch;
        }

        @Override
        public void run() {
            CountDownLatch aLatch = latch;
            try {
                SemanticHighlighter.updateImpl(provider, doc, interrupter);
            } catch (Throwable ex) {
                CndUtils.printStackTraceOnce(ex);
            } finally {
                aLatch.countDown();
            }
        }
    }
    
}
