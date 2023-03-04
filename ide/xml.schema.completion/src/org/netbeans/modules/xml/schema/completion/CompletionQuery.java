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
package org.netbeans.modules.xml.schema.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Completion query that blocks the completion only for a defined time, then
 * displays a 'teaser' completion item, and continues to gather data in the background.
 * The actual completion data fetching is done in a dedicated 1-task RequestProcessor. If the computation does
 * not finish in time ({@link #MAX_COLLECT_TIME}), the completion result set is finished and a special item
 * is added to the end - something like "here will be more suggestions, after I'm done". If the completion remains
 * visible until after the data is prepared, the completion is redisplayed with the new data.
 * <p/>
 * The implementation is rather dumb, so new tasks are posted each time the user invokes completion on a new offset;
 * no effort is made to cancel tasks - in a hope that once the XML schema model will be created, completion should
 * be fairly quick.
 * <p/>
 * For each Document, just the last posted task is tracked; the previous are considered obsolete, and their results
 * will be discarded.
 * 
 * @author Svata Dedic (sdedic@netbeans.org)
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 * @author Alex Petrov (Alexey.Petrov@Sun.Com)
 */
public class CompletionQuery extends AsyncCompletionQuery {
    private static final Logger LOG = Logger.getLogger(CompletionQuery.class.getName()); 
    
    /**
     * RP which actually populates the CC. If the RP task does not finish in time,
     * the main thread will detach from the task and return at least some 'please wait'
     */
    private static final RequestProcessor RP = new RequestProcessor("Schema completion downloader", 1); // NOI18N
    
    /**
     * milliseconds to wait for CC list population. Will finish after that time, so the cc does not block further.
     */
    private static final int MAX_COLLECT_TIME = 5 * 1000; // [ms]
    
    /**
     * Creates a new instance of CompletionQuery
     */
    public CompletionQuery(FileObject primaryFile) {
        this.primaryFile = primaryFile;
    }    

    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }

    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        if (support == null) {
            resultSet.finish();
            return;
        }

        CompletionResultItem endTagResultItem = CompletionUtil.getEndTagCompletionItem(
            component, (BaseDocument) doc);
        List<CompletionResultItem> completionItems = null;
        if (!CompletionUtil.noCompletion(component) &&
           (CompletionUtil.canProvideCompletion((BaseDocument) doc))) {
            
            resultSet.setWaitText(NbBundle.getMessage(CompletionQuery.class, "MSG_PreparingXmlSchemas")); // NOI18N
            
            PreparedResults res = createResultsAndTimedWait(doc, caretOffset);
            
            if (res.isRunning()) {
                resultSet.addItem(new FinishDownloadItem());
            } else {
                completionItems = res.items;
            }
        } else {
            clearPreparedItems(doc);
        }
        
        if (isTaskCancelled()) {
            resultSet.finish();
            return;
        }
        if (endTagResultItem != null) resultSet.addItem(endTagResultItem);
        if ((completionItems != null) && (completionItems.size() > 0)) {
            resultSet.addAllItems(completionItems);
        } else if ((endTagResultItem != null) &&
                   (! (endTagResultItem instanceof TagLastCharResultItem))) {
            endTagResultItem.setExtraPaintGap(-CompletionPaintComponent.DEFAULT_ICON_TEXT_GAP);
        }
        resultSet.finish();
    }
    
    private PreparedResults createResultsAndTimedWait(Document doc, int caretOffset) {
        PreparedResults res;
        boolean stillRunning;
        Task t = null;
        ModelTask mtask = null;
        
        synchronized (preparedCompletions) {
            res = preparedCompletions.get(doc);
            if (res != null && !res.accept(caretOffset)) {
                LOG.log(Level.FINE, "Got task for a different caretOffset; ignoring"); // NOI18N
                res = null;
            }
            if (res != null) {
                if (res.runningTask != null) {
                    stillRunning = res.runningTask.get() != null;
                    // apparently still running
                    LOG.log(Level.FINE, "Prepared data for document {0} contain task ref, running: {1}", new Object[] { // NOI18N
                        doc, stillRunning
                    });
                } else {
                    LOG.log(Level.FINE, "Prepared data ready, got {0} items", res.items == null ? -1 : res.items.size()); // NOI18N
                    clearPreparedItems(doc);
                }
            } else {
                LOG.log(Level.FINE, "Executing task for document {0}", doc); // NOI18N
                mtask = new ModelTask(doc, caretOffset);
                preparedCompletions.put(doc, res = new PreparedResults(caretOffset, mtask));
                t = RP.post(mtask);
            }
        }
        
        if (t != null) {
            LOG.log(Level.FINE, "Scheduling completion dowloader"); // NOI18N
            try {
                t.waitFinished(MAX_COLLECT_TIME);
            } catch (InterruptedException ex) {
            }
            if (!res.fetchCompletionItems()) {
                LOG.log(Level.FINE, "Clearing data cache for {0}", doc); // NOI18N
                clearPreparedItems(doc);
            }
            LOG.log(Level.FINE, "Got {0} within timeout",res.items == null ? -1 : res.items.size()); // NOI18N
        }
        
        return res;
    }
    
    /**
     * Invokes the CC in the AWT thread and records items which should be displayed.
     * The recorded items will be fetched in the completion query and if still valid,
     * displayed without any model computation.
     * 
     * @param doc document for the completion
     * @param offset offset for which the items were completed
     * @param items completion items
     */
    private static void postPreparedItems(Document doc, int offset, List<CompletionResultItem> items) {
        PreparedResults res = new PreparedResults(offset, items);
        synchronized (preparedCompletions) {
            PreparedResults r1 = preparedCompletions.get(doc);
            LOG.log(Level.FINE, "Prepared results: {0}", r1);
            if (r1 != null && r1.accept(offset)) {
                LOG.log(Level.FINE, "Will re-invoke completion for {0} items", items.size());
                preparedCompletions.put(doc, res);
            } else {
                LOG.log(Level.FINE, "Task has been obsoleted, ignoring");
                return;
            }
        }
        // schedule completion appearance
        Completion.get().showCompletion();
    }
    
    /**
     * Clears items prepared for the given document. Should be called during completion query
     * to free memory of garbage
     * 
     * @param doc document instance
     */
    private static void clearPreparedItems(Document doc) {
        synchronized (preparedCompletions) {
            preparedCompletions.remove(doc);
        }
    }
    
    /**
     * Records prepared completion results for a document. When a completion task
     * finishes on background, it records the results here and schedules completion display
     * in AWT. The completion query then only uses the prepared results and clears the
     * document's entry.
     * <p/>
     * It's important that {@link PreparedResults} does not hard-reference the Document,
     * directly or indirectly, so Document is eventually GCed.
     */
    private static final Map<Document, PreparedResults>  preparedCompletions = new WeakHashMap<Document, PreparedResults>();
    
    /**
     * Completion results prepared for the immediate next invocation. 
     */
    private static class PreparedResults {
        private Reference<ModelTask> runningTask;
        private int caretOffset;
        private List<CompletionResultItem> items;
        
        public PreparedResults(int caretOffset, List<CompletionResultItem> items) {
            this.caretOffset = caretOffset;
            this.items = items;
        }

        public PreparedResults(int caretOffset, ModelTask runningTask) {
            this.caretOffset = caretOffset;
            this.runningTask = new WeakReference<ModelTask>(runningTask);
        }
        
        public boolean isRunning() {
            return runningTask != null;
        }
        
        public boolean hasItems() {
            return items != null && !items.isEmpty();
        }
        
        public boolean accept(int caretOffset) {
            return caretOffset == this.caretOffset;
        }
        
        public ModelTask getRunningTask() {
            return runningTask == null ? null : runningTask.get();
        }
        
        public boolean fetchCompletionItems() {
            ModelTask t = getRunningTask();
            if (t != null) {
                this.items = t.fetchCompletionItems();
                if (this.items != null) {
                    this.runningTask = null;
                }
            }
            return isRunning();
        }
    }
    
    /**
     * Task, which prepares the completion model. The task is executed in a RP while the
     * main completion thread waits for a certain time ({@link CompletionQuery#MAX_COLLECT_TIME})
     * before showing the user an unfinished completion results.
     */
    private class ModelTask implements Runnable {
        private final int caretOffset;
        private final Document doc;
        private List<CompletionResultItem> items;
        private boolean ccCompleted;

        public ModelTask(Document doc, int caretOffset) {
            this.doc = doc;
            this.caretOffset = caretOffset;
        }
        
        public synchronized List<CompletionResultItem> fetchCompletionItems() {
            ccCompleted = true;
            return items;
        }
        
        @Override
        public void run() {
            List<CompletionResultItem> completionItems = getCompletionItems(doc, caretOffset);
            if (completionItems == null) {
                completionItems = Collections.emptyList();
            }
            synchronized (this) {
                if (!ccCompleted) {
                    LOG.log(Level.FINE, "Completion task finished before timeout");
                    items = completionItems;
                    return;
                }
            }
            if (isTaskCancelled()) {
                clearPreparedItems(doc);
            } else {
                postPreparedItems(doc, caretOffset, completionItems);
            }
        }
    }
        
    /**
     * This method is needed for unit testing purposes.
     */
    List<CompletionResultItem> getCompletionItems(Document doc, int caretOffset) {
        List<CompletionResultItem> completionItems = null;
        
        //Step 1: create a context
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        if (support == null) {
            return null;
        }
        context = new CompletionContextImpl(primaryFile, support, caretOffset);
        
        //Step 2: Accumulate all models and initialize the context
        if(!context.initContext() || !context.initModels() ) {
            return null;
        }
                
        //Step 3: Query
        switch (context.getCompletionType()) {
            case COMPLETION_TYPE_ELEMENT_VALUE:
                completionItems = CompletionUtil.getElementValues(context);
                if ((completionItems != null) && (completionItems.size() > 0)) {
                    break;
                }

            case COMPLETION_TYPE_ELEMENT:
                completionItems = CompletionUtil.getElements(context);
                break;
                
            case COMPLETION_TYPE_ATTRIBUTE:
                completionItems = CompletionUtil.getAttributes(context);
                break;
            
            case COMPLETION_TYPE_ATTRIBUTE_VALUE:
                completionItems = CompletionUtil.getAttributeValues(context);
                break;            
            
            case COMPLETION_TYPE_ENTITY:
                break;
            
            case COMPLETION_TYPE_NOTATION:
                break;
                
            default:
                break;
        }
        
        return completionItems;
    }
            
    private JTextComponent component;
    private FileObject primaryFile;
    private CompletionContextImpl context;
    
    private static final ImageIcon LOADING_ICON = new ImageIcon(ImageUtilities.loadImage(
            "org/netbeans/modules/xml/schema/completion/resources/element.png")); // NOI18N

    private static final class FinishDownloadItem implements CompletionItem {
        @Override
        public void defaultAction(JTextComponent component) {
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(getText(), null, g, defaultFont);
        }
        
        private String getText() {
            return NbBundle.getMessage(CompletionQuery.class, "COMPL_SchemasLoading"); // NOI18N
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(LOADING_ICON, getText(), 
                    null, 
                    g, 
                    defaultFont, 
                    LFCustoms.shiftColor(defaultColor),
                    width, 
                    height, 
                    selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        public int getSortPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public CharSequence getSortText() {
            return ""; // NOI18N
        }

        @Override
        public CharSequence getInsertPrefix() {
            return ""; // NOI18N
        }
        
    }
}