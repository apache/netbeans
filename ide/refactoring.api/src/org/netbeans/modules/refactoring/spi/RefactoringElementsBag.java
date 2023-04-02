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

package org.netbeans.modules.refactoring.spi;

import java.io.IOException;
import java.util.*;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.api.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.UserQuestionException;

/**
 * Container holding RefactoringElements
 * @author Jan Becicka
 */
public final class RefactoringElementsBag {
    ArrayList<Transaction> commits;
    ArrayList<RefactoringElementImplementation> fileChanges;
    boolean hasGuarded = false;
    boolean hasReadOnly = false;

    static {
        SPIAccessor.DEFAULT = new AccessorImpl();
    }
    
    private final List<RefactoringElementImplementation> delegate;
    private final RefactoringSession session;
    private Collection<FileObject> readOnlyFiles = new HashSet<FileObject>();
    
    /**
     * Creates an instance of RefactoringElementsBag
     */
    RefactoringElementsBag(RefactoringSession session, List<RefactoringElementImplementation> delegate) {
        this.session = session;
        this.delegate = delegate;
        this.commits = new ArrayList<Transaction>();
        this.fileChanges =  new ArrayList<RefactoringElementImplementation>();
    }
    
    /**
     * Adds RefactoringElementImplementation to this bag.
     * If RefactoringElementImplementation is in read-only file - status of this element is 
     * changes to RefactoringElement.READ_ONLY
     * If RefactoringElementImplementation is in guarded block, all registered GuardedBlockHandler
     * are asked, if they can replace given RefactoringElementImplementation by it's own 
     * RefactoringElementImplementation. If there is no suitable replacement found, 
     * given element is added and it's status is set to RefactringElement.GUARDED
     * 
     * @param refactoring refactoring, which adds this RefactoringElementImplementation
     * @param el element to add
     * @return instance of Problem or null
     */
    public Problem add(AbstractRefactoring refactoring, RefactoringElementImplementation el) {
        Problem p = null;
        if (el == null) throw new NullPointerException ();
        //isQuery should be used
        if (isReadOnly(el) && !(refactoring instanceof WhereUsedQuery) && !(refactoring instanceof SingleCopyRefactoring)) {
            FileObject file = el.getParentFile();
            readOnlyFiles.add(file);
            el.setEnabled(false);
            el.setStatus(el.READ_ONLY);
            hasReadOnly = true;
            delegate.add(el);
        //isQuery should be used
        } else if (!(refactoring instanceof WhereUsedQuery) && isGuarded(el)) {
            ArrayList<RefactoringElementImplementation> proposedChanges = new ArrayList<RefactoringElementImplementation>();
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            for (GuardedBlockHandler gbHandler: APIAccessor.DEFAULT.getGBHandlers(refactoring)) {
                el.setEnabled(false);
                p = APIAccessor.DEFAULT.chainProblems(gbHandler.handleChange(el, proposedChanges, transactions),  p);
                
                if (p != null && p.isFatal())
                    return p;
                
                delegate.addAll(proposedChanges);
                
                for (Transaction transaction:transactions) {
                    registerTransaction(transaction);
                }
                
                if (!proposedChanges.isEmpty() || !transactions.isEmpty())
                    return p;
                
            }
            el.setEnabled(false);
            el.setStatus(el.GUARDED);
            hasGuarded = true;
            delegate.add(el);
        } else {
            delegate.add(el);
        }
        return p;
    }
    
    /**
     * Adds all RefactringElements from given Collection using #add method
     * @param refactoring refactoring, which adds this RefactoringElement
     * @param elements Collection of RefactoringElements
     * @return instance of Problem or null
     */
    public Problem addAll(AbstractRefactoring refactoring, Collection<RefactoringElementImplementation> elements) {
	Problem p = null;
	for (RefactoringElementImplementation rei:elements) {
	    p = APIAccessor.DEFAULT.chainProblems(p, add(refactoring, rei));
            if (p!=null && p.isFatal())
                return p;
	}
        return p;
    }
    
    
    /**
     * 
     * @return RefactoringSession associated with this RefactoringElementsBag
     */
    public RefactoringSession getSession() {
        return session;
    }
    
    Collection<FileObject> getReadOnlyFiles() {
        return readOnlyFiles;
    }
    
    /**
     * commits are called after all changes are performed
     * @param commit Transaction to commit
     * @see Transaction
     * @see BackupFacility
     */
    public void registerTransaction(Transaction commit) {
        if (APIAccessor.DEFAULT.isCommit(session))
            if (!commits.contains(commit))
                commits.add(commit);
    }
    
    
    /**
     * fileChanges are performed after all element changes
     * @param refactoring changes to be performed
     * @see Transaction
     * @see BackupFacility
     */
    public Problem addFileChange(AbstractRefactoring refactoring, RefactoringElementImplementation el) {
        if (APIAccessor.DEFAULT.isCommit(session))
            fileChanges.add(el);
        return null;
    }    
    
    private boolean isReadOnly(RefactoringElementImplementation rei) {
        FileObject fileObject = rei.getParentFile();
        if (fileObject == null)
            throw new NullPointerException ("null parent file: " + rei.getClass ().getName ());
        return !rei.getParentFile().canWrite();
    }
    
    /**
     * TODO: GuardedQuery is still missing
     * this solution has performance issues.
     */ 
    private boolean isGuarded(RefactoringElementImplementation el) {
        if (el.getPosition()==null)
            return false;
        try {
            DataObject dob = DataObject.find(el.getParentFile());
            EditorCookie e = dob.getCookie(EditorCookie.class);
            if (e!=null) {
                GuardedSectionManager manager = GuardedSectionManager.getInstance(openDocument(e));
                if (manager != null) {
                    Position elementStart = el.getPosition().getBegin().getPosition();
                    Position elementEnd = el.getPosition().getEnd().getPosition();
                    for(GuardedSection section:manager.getGuardedSections()) {
                        if (section.contains(elementStart, true)
                                || (section.contains(elementEnd, true)
                                && section.getStartPosition().getOffset() != elementEnd.getOffset())) {
                            return true;
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, // NOI18N
                    ex.getMessage(), ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, // NOI18N
                    ex.getMessage(), ex);
        }
        return false;
    }

    private static StyledDocument openDocument(EditorCookie ec) throws IOException {
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (UserQuestionException ex) {
            // issue #156068 - open even big file, issue #232257 - encoding should not be a problem for guarded test
            if (ex.getMessage().startsWith("The file is too big.") || ex.getMessage().contains("cannot be safely opened with encoding")) { // NOI18N
                ex.confirmed();
                doc = ec.openDocument();
            } else {
                throw ex;
            }
        }
        return doc;
    }
}
