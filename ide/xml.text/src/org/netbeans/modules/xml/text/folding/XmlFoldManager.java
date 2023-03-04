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

package org.netbeans.modules.xml.text.folding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.NbBundle;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.xml.text.api.XMLTextUtils;
import org.netbeans.modules.xml.text.folding.TokenElement.Token;
import org.netbeans.modules.xml.text.folding.TokenElement.TokenType;

/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds in XML documents.
 *
 * @author  Ayub Khan
 * @author  Samaresh Panda
 */
public class XmlFoldManager implements FoldManager {

    private FoldOperation operation;
    // synchronized (this)
    private long dirtyTimeMillis = 0;
    private Timer timer;
    private TimerTask timerTask;
   
    public static final int DELAY_SYNCER = 2000;  // milisecs.
    public static final int DELAY_DIRTY = 1000;  // milisecs.

    private Preferences prefs;

    public XmlFoldManager() {
        prefs = MimeLookup.getLookup(XMLTextUtils.XML_MIME).lookup(Preferences.class);
    }


    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    /**
     * Ideally release should get called, but god knows why it doesn't.
     */
    public void release() {
        synchronized (this) {
            releaseTimerTask();
            timer = null;
        }
    }

    protected FoldOperation getOperation() {
        return operation;
    }
      
    /**
     * Do NOT update folds here. For some reason, three fold managers get
     * instantiated by the infrastructure and three initFolds() get called.
     *
     * Schedule fold updates in insertUpdate removeUpdate and changedUpdate.
     * First fold will be created by changedUpdate.
     * @param transaction
     */
    @Override
    public void initFolds(FoldHierarchyTransaction transaction) {
        /* fix for issue #184452 (http://netbeans.org/bugzilla/show_bug.cgi?id=184452)
         * -------------------------------------------------------------------------
         * In NB 6.9 (April, 2010) the default behaviour has been changed: now only
         * 1 fold manager is instantiated by the infrastructure and the method "initFolds()"
         * is called once (the parent interface org.netbeans.spi.editor.fold.FoldManager:
         * "... this method is by default called at the file opening time"), but the
         * method "changedUpdate()" is not called at the file opening time.
         */
        scheduleFoldUpdate();
    }
    
    private BaseDocument getDocument() {
        return (BaseDocument) getOperation().getHierarchy().getComponent().getDocument();
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        scheduleFoldUpdate();
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        scheduleFoldUpdate();
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        scheduleFoldUpdate();
    }

    public void removeEmptyNotify(Fold epmtyFold) {
    }

    public void removeDamagedNotify(Fold damagedFold) {
    }

    public void expandNotify(Fold expandedFold) {
    }
   
    private synchronized void scheduleFoldUpdate() {
        if (timer == null) {
            timer = new Timer();
        }
        dirtyTimeMillis = System.currentTimeMillis();

        //dump the old timerTask
        releaseTimerTask();

        timerTask = new TimerTask() {
            public void run() {
                long delay;
                synchronized (XmlFoldManager.this) {
                    if (timerTask != this) {
                        return;
                    }
                    delay = dirtyIntervalMillis();
                }
                if (delay > DELAY_DIRTY) {
                    updateFolds();
                    unsetDirty();
                }
            }
        };
        timer.schedule(timerTask, DELAY_SYNCER);
    }
    
    private synchronized void releaseTimerTask() {
        if(timerTask == null)
                return;
        timerTask.cancel();
        timerTask = null;
    }
   
    public void unsetDirty() {
        synchronized (this) {
            dirtyTimeMillis = 0;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }
   
    // always called from synchronized block
    private long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }

    /**
     * This method parses the XML document using Lexer and creates/recreates
     * the fold hierarchy.
     */
    private void updateFolds() {
        FoldHierarchy foldHierarchy = getOperation().getHierarchy();
        //lock the document for changes
        BaseDocument d = getDocument();
        d.readLock();
        try {
            //lock the hierarchy
            foldHierarchy.lock();
            try {
                //open new transaction
                FoldHierarchyTransaction fht = getOperation().openTransaction();
                try {
                    ArrayList<Fold> existingFolds = new ArrayList<Fold>();
                    collectExistingFolds(foldHierarchy.getRootFold(), existingFolds);
                    for(Fold f : existingFolds) {
                        getOperation().removeFromHierarchy(f, fht);
                    }
                    createFolds(fht);
                } catch (Exception ex) {
//                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
//                            NbBundle.getMessage(XmlFoldManager.class, "MSG_FOLDS_DISABLED"));
                } finally {
                    fht.commit();
                }
            } finally {
                //printFoldHierarchy(foldHierarchy.getRootFold(),"");
                foldHierarchy.unlock();
            }
        } finally {
            d.readUnlock();
        }
    }
   
    /**
     * Prints the fold hierarchy.
     * @param fold
     * @param tab
     */
    private void printFoldHierarchy(Fold fold, String tab) {
        //System.out.println(tab + fold.getDescription());
        for(int i=0; i<fold.getFoldCount(); i++) {
            printFoldHierarchy(fold.getFold(i), tab+"==");
        }
    }
   
    /**
     * Collects all folds from the hierarchy that were created by this manager
     * and are not the root fold.
     *
     * @param fold
     * @param list 
     */
    private void collectExistingFolds(Fold fold, List<Fold> list) {
        for(int i=0; i<fold.getFoldCount(); i++) {
            collectExistingFolds(fold.getFold(i), list);
        }
        if(!FoldUtilities.isRootFold(fold) && getOperation().owns(fold)) {
            list.add(fold);
        }
    }
   
    /**
     * Creates a new fold and adds to the fold hierarchy.
     */
    private Fold createFold(FoldType type, String description, boolean collapsed,
            int startOffset, int endOffset, FoldHierarchyTransaction transaction)
                throws BadLocationException {
        Fold fold = null;
        if ( startOffset >= 0 &&
             endOffset >= 0 &&
             startOffset < endOffset &&
             endOffset <= getDocument().getLength() ) {
            fold = getOperation().addToHierarchy(
                    type,
                    description.intern(), //save some memory
                    collapsed,
                    startOffset,
                    endOffset,
                    description.length(),
                    0,
                    null,
                    transaction);
        }
        return fold;
    }

    /**
     * This is the core of the fold creation algorithm.
     * This method parses the document using lexer and creates folds and adds
     * them to the fold hierarchy.
     */
    private void createFolds(FoldHierarchyTransaction fhTran)
            throws BadLocationException, IOException {
        BaseDocument basedoc = getDocument();
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(basedoc);
        TokenSequence<XMLTokenId> tokenSequence = tokenHierarchy.tokenSequence();
        org.netbeans.api.lexer.Token<XMLTokenId> token = tokenSequence.token();
        // Add the text token, if any, before xml decalration to document node
        if(token != null && token.id() == XMLTokenId.TEXT) {
            if(tokenSequence.moveNext()) {
                token = tokenSequence.token();
            }
        }
        int currentTokensSize = 0;
        Stack<TokenElement> stack = new Stack<TokenElement>();
        String currentNode = null;
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            XMLTokenId tokenId = token.id();
            String image = token.text().toString();
            TokenType tokenType = TokenType.TOKEN_WHITESPACE;
            switch(tokenId) {
                case TAG:
                {
                    int len = image.length();
                    if (image.charAt(len-1) == '>') {
                        TokenElement tokenElem = null;
                        if(len == 2) {
                            if(!stack.empty())
                                stack.pop();
                        } else {
                            if(!stack.empty()) {
                                if(stack.peek().getName().equals(currentNode))
                                    tokenElem = stack.pop();
                            }
                        }
                        if(tokenElem != null) {
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            //do not create fold if start and end tags are
                            //in the same line
                            if(isOneLiner(so, eo))
                                break;
                            String foldName = "<" + currentNode + ">";
                            boolean collapseByDefault = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_TAGS, false);
                            Fold f = createFold(XmlFoldTypes.TAG, foldName, collapseByDefault, so, eo, fhTran);
                            currentNode = null;
                        }
                    } else {
                        tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                        if(image.startsWith("</")) {
                            String tagName = image.substring(2);
                            currentNode = tagName;
                        } else {
                            String tagName = image.substring(1);
                            stack.push(new TokenElement(tokenType, tagName,
                                    currentTokensSize, currentTokensSize+image.length(), -1));
                        }
                    }
                    break;
                }
                case BLOCK_COMMENT:
                {
                    tokenType = TokenType.TOKEN_COMMENT;
                    if (!(image.startsWith(Token.COMMENT_START.getValue()) &&
                            image.endsWith(Token.COMMENT_END.getValue()))) {
                        if (image.startsWith(Token.COMMENT_START.getValue())) {
                            String foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_COMMENT"); //NOI18N
                            stack.push(new TokenElement(tokenType, foldName,
                                    currentTokensSize, currentTokensSize+image.length(), -1));
                        } else if(image.endsWith(Token.COMMENT_END.getValue())) {
                            TokenElement tokenElem = stack.pop();
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            boolean collapseByDefault = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_COLLAPSE_JAVADOC, false);
                            Fold f = createFold(XmlFoldTypes.COMMENT, tokenElem.getName(), collapseByDefault, so, eo, fhTran);
                            //myFolds.add(f);
                        }
                    }
                    break;
                }
                case CDATA_SECTION:
                {
                    tokenType = TokenType.TOKEN_CDATA_VAL;
                    if (!(image.startsWith(Token.CDATA_START.getValue()) &&
                            image.endsWith(Token.CDATA_END.getValue()))) {
                        if (image.startsWith(Token.CDATA_START.getValue())) {
                            String foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_CDATA"); //NOI18N
                            stack.push(new TokenElement(tokenType, foldName,
                                    currentTokensSize, currentTokensSize+image.length(), -1));
                        } else if(image.endsWith(Token.CDATA_END.getValue())) {
                            TokenElement tokenElem = stack.pop();
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            Fold f = createFold(XmlFoldTypes.CDATA, tokenElem.getName(), false, so, eo, fhTran);
                            //myFolds.add(f);
                        }
                    }
                    break;
                }
                   
                case PI_START:
                case PI_TARGET:
                case PI_CONTENT:
                case PI_END:
                case ARGUMENT: //attribute of an element
                case VALUE:
                case TEXT:                   
                case CHARACTER:
                case WS:
                case OPERATOR:
                case DECLARATION:
                    break; //Do nothing for above case's
               
                case ERROR:
                case EOL:
                default:
                    break;
            }
            currentTokensSize += image.length();
        }
    }
   
    public boolean isOneLiner(int start, int end) {
        try {
            BaseDocument doc = getDocument();
            return Utilities.getLineOffset(doc, start) ==
                   Utilities.getLineOffset(doc, end);
        } catch (BadLocationException ex) {
            //Exceptions.printStackTrace(ex);
            return false;
        }
    }      
}
