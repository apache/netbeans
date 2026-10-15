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

package org.netbeans.modules.languages.features;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import org.netbeans.api.editor.document.LineDocumentUtils;

import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.Utilities;
import org.openide.text.NbDocument;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.ParserManagerImpl;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesFoldManager extends ASTEvaluator implements FoldManager {
    
    static final String         FOLD = "FOLD";
    private static final int    EVALUATING = 0;
    private static final int    STOPPED = 1;
    
    private FoldOperation       operation;
    private Document            doc;
    private ParserManagerImpl   parserManager;
    private int                 evalState = STOPPED;
    
    
    /** Creates a new instance of JavaFoldManager */
    public LanguagesFoldManager () {
    }
    
    /**
     * Initialize this manager.
     *
     * @param operation fold hierarchy operation dedicated to the fold manager.
     */
    public void init (FoldOperation operation) {
        Document d = operation.getHierarchy ().getComponent ().getDocument ();
        if (d instanceof NbEditorDocument) {
            this.doc = d;
            this.operation = operation;
            parserManager = ParserManagerImpl.getImpl (doc);
            parserManager.addASTEvaluator (this);
            parserManager.fire (
                parserManager.getState (), 
                null, 
                Collections.<String,Set<ASTEvaluator>>singletonMap (FOLD, Collections.<ASTEvaluator>singleton (this)),
                parserManager.getAST ()
            );
        }
    }
    
    /**
     * Initialize the folds provided by this manager.
     * <br>
     * The fold manager should create initial set of folds here
     * if it does not require too much resource consumption.
     * <br>
     * As this method is by default called at the file opening time
     * then it may be better to schedule the initial fold computations
     * for later time and do nothing here. 
     *
     * <p>
     * Any listeners necessary for the maintenance of the folds
     * can be attached here.
     * <br>
     * Generally there should be just weak listeners used
     * to not prevent the GC of the text component.
     *
     * @param transaction transaction in terms of which the intial
     *  fold changes can be performed.
     */
    public void initFolds (FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Called by hierarchy upon the insertion to the underlying document.
     * <br>
     * If there would be any fold modifications required they may be added
     * to the given transaction.
     *
     * @param evt document event describing the document modification.
     * @param transaction open transaction to which the manager can add
     *  the fold changes.
     */
    public void insertUpdate (DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Called by hierarchy upon the removal in the underlying document.
     * <br>
     * If there would be any fold modifications required they may be added
     * to the given transaction.
     *
     * @param evt document event describing the document modification.
     * @param transaction open transaction to which the manager can add
     *  the fold changes.
     */
    public void removeUpdate (DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Called by hierarchy upon the change in the underlying document.
     * <br>
     * If there would be any fold modifications required they may be added
     * to the given transaction.
     *
     * @param evt document event describing the document change.
     * @param transaction open transaction to which the manager can add
     *  the fold changes.
     */
    public void changedUpdate (DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Notify that the fold was removed from hierarchy automatically
     * by fold hierarchy infrastructure processing
     * because it became empty (by a document modification).
     */
    public void removeEmptyNotify (Fold epmtyFold) {
    }
    
    /**
     * Notify that the fold was removed from hierarchy automatically
     * by fold hierarchy infrastructure processing
     * because it was damaged by a document modification.
     */
    public void removeDamagedNotify (Fold damagedFold) {
    }
    
    /**
     * Notify that the fold was expanded automatically
     * by fold hierarchy infrastructure processing
     * because its <code>isExpandNecessary()</code>
     * return true.
     */
    public void expandNotify (Fold expandedFold) {
    }

    /**
     * Notification that this manager will no longer be used by the hierarchy.
     * <br>
     * The folds that it maintains are still valid but after this method
     * finishes they will be removed from the hierarchy.
     *
     * <p>
     * This method is not guaranteed to be called. Therefore the manager
     * must only listen weekly on the related information providers
     * so that it does not block the hierarchy from being garbage collected.
     */
    public void release () {
        //S ystem.out.println("release " + mimeType + " : " + operation + " : " + this);
        if (doc != null) {
            parserManager.removeASTEvaluator (this);
        }
        parserManager = null;
    }

    
    // ASTEvaluator methods ....................................................


    private static FoldType defaultFoldType = new FoldType ("default");
    private List<FoldItem> folds;
    
    public void beforeEvaluation (State state, ASTNode root) {
        evalState = EVALUATING;
        folds = null;
    }

    public void afterEvaluation (State state, ASTNode root) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () { 
                if (operation == null) {
                    evalState = STOPPED;
                    return;
                }
                FoldHierarchy fh = operation.getHierarchy ();

                try {
                    //get existing folds
                    Fold fold = operation.getHierarchy ().getRootFold ();
                    List<Fold> existingFolds = new ArrayList<Fold> ();
                    collectFolds(fold, existingFolds);

                    List<FoldItem> generated = folds != null ? folds : new ArrayList<FoldItem>();
                    
                    //...and generate a list of new folds and a list of folds to be removed
                    final HashSet newborns = new HashSet(generated.size() / 2);
                    final HashSet zombies = new HashSet(generated.size() / 2);

                    //go through all the parsed elements and compare it with the list of existing folds
                    Iterator genItr = generated.iterator();
                    Hashtable newbornsLinesCache = new Hashtable();
                    HashSet duplicateNewborns = new HashSet();
                    while(genItr.hasNext()) {
                        FoldItem fi = (FoldItem)genItr.next();
                        //do not add more newborns with the same lineoffset
                        int fiLineOffset = LineDocumentUtils.getLineIndex((BaseDocument)doc, fi.start);
                        FoldItem found = (FoldItem)newbornsLinesCache.get(Integer.valueOf(fiLineOffset));
                        if(found != null) {
                            //figure out whether the new element is a descendant of the already added one
                            if(found.end < fi.end) {
                                //remove the descendant and add the current
                                duplicateNewborns.add(found);
                            }
                        }
                        newbornsLinesCache.put(Integer.valueOf(fiLineOffset), fi); //add line mapping of the current element

                        //try to find a fold for the fold info
                        Fold fs = FoldUtilities.findNearestFold(fh, fi.start);
                        //hacky fix - we need to find a better solution
                        //how to check if the fold was created by me
                        if (fs != null) {
                            try {
                                operation.getExtraInfo(fs);
                                //no ISE thrown - my fold
                            } catch (IllegalStateException e) {
                                //not my fold
                                fs = null;
                            }
                        }
                        if(fs != null
                                && fs.getStartOffset() == fi.start
                                && fs.getEndOffset() == fi.end) {
                            //there is a fold with the same boundaries as the FoldInfo
                            if(fi.type != fs.getType() || !(fi.foldName.equals(fs.getDescription()))) {
                                //the fold has different type or/and description => recreate
                                zombies.add(fs);
                                newborns.add(fi);
                            }
                        } else {
                            //create a new fold
                            newborns.add(fi);
                        }
                    }
                    newborns.removeAll(duplicateNewborns);
                    existingFolds.removeAll(zombies);

                    Hashtable linesToFoldsCache = new Hashtable(); //needed by ***

                    //remove not existing folds
                    Iterator extItr = existingFolds.iterator();
                    while(extItr.hasNext()) {
                        Fold f = (Fold)extItr.next();
        //                if(!zombies.contains(f)) { //check if not alread scheduled to remove
                        Iterator genItr2 = generated.iterator();
                        boolean found = false;
                        while(genItr2.hasNext()) {
                            FoldItem fi = (FoldItem)genItr2.next();
                            if(f.getStartOffset() == fi.start
                                    && f.getEndOffset() == fi.end) {
                                found = true;
                                break;
                            }
                        }
                        if(!found) {
                            zombies.add(f);
                        } else {
                            //store the fold lineoffset 2 fold mapping
                            int lineoffset = LineDocumentUtils.getLineIndex((BaseDocument)doc, f.getStartOffset());
                            linesToFoldsCache.put(Integer.valueOf(lineoffset), f);
                        }
        //                }
                    }

                    //*** check for all newborns if there isn't any existing fold
                    //starting on the same line which is a descendant of this new fold
                    //if so remove it.
                    Iterator newbornsItr = newborns.iterator();
                    HashSet newbornsToRemove = new HashSet();
                    while(newbornsItr.hasNext()) {
                        FoldItem fi = (FoldItem)newbornsItr.next();
                        Fold existing = (Fold)linesToFoldsCache.get(Integer.valueOf(LineDocumentUtils.getLineIndex((BaseDocument)doc, fi.start)));
                        if(existing != null) {
                            //test if the fold is my descendant
                            if(existing.getEndOffset() < fi.end) {
                                //descendant - remove it
                                zombies.add(existing);
                            } else {
                                //remove the newborn
                                newbornsToRemove.add(fi);
                            }
                        }
                    }
                    newborns.removeAll(newbornsToRemove);
                    ((BaseDocument)doc).readLock();
                    try {
                        //lock the hierarchy
                        fh.lock();
                        try {
                            //open new transaction
                            FoldHierarchyTransaction transaction = operation.openTransaction ();
                            try {
                                //remove outdated folds
                                Iterator iter = zombies.iterator();
                                while(iter.hasNext()) {
                                    Fold f = (Fold)iter.next();
                                    //test whether the size of the document is greater than zero,
                                    //if it is then this means that the document has been closed in editor.
                                    if(doc.getLength() == 0) break;
                                    operation.removeFromHierarchy(f, transaction);
                                }
                                //add new folds
                                Iterator newFolds = newborns.iterator();
                                while(newFolds.hasNext()) {
                                    FoldItem f = (FoldItem)newFolds.next();
                                    //test whether the size of the document is greater than zero,
                                    //if it is then this means that the document has been closed in editor.
                                    if(doc.getLength() == 0) break;

                                    if(f.start >= 0
                                            && f.end >= 0
                                            && f.start < f.end
                                            && f.end <= doc.getLength()) {
                                        operation.addToHierarchy(f.type, f.foldName, false, f.start , f.end , 0, 0, fh, transaction);
                                    }
                                }
                            }catch(BadLocationException ble) {
                                ble.printStackTrace();
                            }finally {
                                transaction.commit();
                            }
                        } finally {
                            fh.unlock();
                        }
                    } finally {
                        ((BaseDocument)doc).readUnlock();
                    }
                
                } catch (BadLocationException ex) {
                    ex.printStackTrace ();
                } finally {
                    evalState = STOPPED;
                }
            }
        });
    }
    
    private void collectFolds(Fold fold, List<Fold> existingFolds) {
        int i, k = fold.getFoldCount();
        for (i = 0; i < k; i++) {
            Fold f = fold.getFold (i);
            //hacky fix - we need to find a better solution
            //how to check if the fold was created by me
            try {
                operation.getExtraInfo(f);
                //no ISE thrown - my fold
                existingFolds.add(f);
                collectFolds(f, existingFolds);
            } catch (IllegalStateException e) {
                //not my fold
            }
        }
    }
    
    public String getFeatureName () {
        return "FOLD";
    }

    public void evaluate (State state, List<ASTItem> path, Feature fold) {
        ASTItem item = path.get (path.size () - 1);
        int s = item.getOffset (),
            e = item.getEndOffset ();
        int sln = NbDocument.findLineNumber ((StyledDocument)doc, s),
            eln = NbDocument.findLineNumber ((StyledDocument)doc, e);
        if (sln == eln) return;
        String mimeType = item.getMimeType ();
        Language language = (Language) item.getLanguage ();
        boolean isTokenFold = ((item instanceof ASTToken) && 
                    fold == language.getFeatureList ().getFeature (FOLD, ((ASTToken) item).getTypeName ()));
        if (!isTokenFold) {
            TokenHierarchy th = TokenHierarchy.get (doc);
            if (doc instanceof NbEditorDocument)
                ((NbEditorDocument) doc).readLock ();
            try {
                TokenSequence ts = th.tokenSequence ();
                ts.move (e - 1);
                if (!ts.moveNext ()) return;
                while (!ts.language ().mimeType ().equals (mimeType)) {
                    ts = ts.embedded ();
                    if (ts == null) return;
                    ts.move (e - 1);
                    if (!ts.moveNext ()) return;
                }
                Token t = ts.token ();
                Set<Integer> skip = language.getAnalyser ().getSkipTokenTypes ();
                while (skip.contains (t.id ().ordinal ())) {
                    if (!ts.movePrevious ()) break;
                    t = ts.token ();
                }
                e = ts.offset () + t.length ();
                String tokenText = t.text ().toString ();
                if (tokenText.endsWith ("\n"))
                    e--;
                sln = NbDocument.findLineNumber ((StyledDocument)doc, s);
                eln = NbDocument.findLineNumber ((StyledDocument)doc, e);
                if (eln - sln < 1) return;
            } finally {
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readUnlock ();
            }
        }

        if (fold.hasSingleValue ()) {
            String foldName = LocalizationSupport.localize (language, (String) fold.getValue (SyntaxContext.create (doc, ASTPath.create (path))));
            if (foldName == null) return;            
            addFold (new FoldItem(foldName, s, e, defaultFoldType));
            return;
        }
        String foldName = LocalizationSupport.localize (language, (String) fold.getValue ("fold_display_name", SyntaxContext.create (doc, ASTPath.create (path))));
        if (foldName == null) {
            foldName = "..."; // NOI18N
        }
        String foldType = LocalizationSupport.localize (language, (String) fold.getValue ("collapse_type_action_name"));
        addFold (new FoldItem (foldName, s, e, Folds.getFoldType (foldType)));
    }
    
    private void addFold (FoldItem foldItem) {
        if (folds == null)
            folds = new CopyOnWriteArrayList<FoldItem> ();
        folds.add (foldItem);
    }
    
    // package private methods for unit tests...................................
    
    void init (Document doc) {
        this.doc = doc;
        this.operation = null;
        parserManager = ParserManagerImpl.getImpl (doc);
        parserManager.addASTEvaluator(this);
    }
    
    List<FoldItem> getFolds() {
        return folds;
    }
    
    boolean isEvaluating() {
        return evalState == EVALUATING;
    }
    
    // innerclasses ............................................................
    
    static final class FoldItem {
        String foldName;
        int start;
        int end;
        FoldType type;
        
        FoldItem (String foldName, int start, int end, FoldType type) {
            this.foldName = foldName;
            this.start = start;
            this.end = end;
            this.type = type;
        }
    } 

    public static final class Factory implements FoldManagerFactory {
        
        public FoldManager createFoldManager () {
            return new LanguagesFoldManager ();
        }

    }
}
