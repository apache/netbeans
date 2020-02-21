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


package org.netbeans.modules.cnd.asm.core.folding;

import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;

import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;

public class AsmFoldManager implements FoldManager, AsmModelAccessor.ParseListener {
     
    private FoldOperation operation;    
    private AsmModelAccessor acc;
    
    private static final FoldType fold = new FoldType("ASM_FUNCTION"); // NOI18N

    public void init(FoldOperation operation) {
        this.operation = operation; 
        acc = (AsmModelAccessor) getDocument().getProperty(AsmModelAccessor.class);
        if (acc != null) {
            //acc.addParseListener(this);
        }
    }
    
    private Document getDocument() {
        return operation.getHierarchy().getComponent().getDocument();
    }

    public void initFolds(FoldHierarchyTransaction foldHierarchyTransaction) {
    }

    public void insertUpdate(DocumentEvent documentEvent, FoldHierarchyTransaction foldHierarchyTransaction) {
    }

    public void removeUpdate(DocumentEvent documentEvent, FoldHierarchyTransaction foldHierarchyTransaction) {
    }

    public void changedUpdate(DocumentEvent documentEvent, FoldHierarchyTransaction foldHierarchyTransaction) {
    }

    public void removeEmptyNotify(Fold fold) {
    }

    public void removeDamagedNotify(Fold fold) {
    }

    public void expandNotify(Fold fold) {
    }

    public void release() {
    }
    
    public static final class Factory implements FoldManagerFactory {
        public FoldManager createFoldManager() {
            return new AsmFoldManager();
        }

     }
    
    public void notifyParsed() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<AsmOffsetable> off = Collections.<AsmOffsetable>emptyList();
                              
                FoldHierarchyTransaction transaction = operation.openTransaction();
                try {
                    List folds = FoldUtilities.findRecursive(operation.getHierarchy().getRootFold());                    
                    for (Object fold: folds) {
                        operation.removeFromHierarchy((Fold) fold, transaction);
                    }
                } finally {
                    transaction.commit();
                }
                
                transaction = operation.openTransaction();
                try {   
                    AsmOffsetable begin, end;
                    for (int i = 0; i < off.size() - 1; i++) {
                       begin = off.get(i);
                       end = off.get(i + 1);                        
                        try {
                            int num1 = Utilities.getLineOffset((BaseDocument) getDocument(), begin.getEndOffset() + 2);
                            int num2 = Utilities.getLineOffset((BaseDocument) getDocument(), end.getStartOffset() - 2);
                            if (num2 - num1 >= 2) {
                                operation.addToHierarchy(fold, "",  true, begin.getEndOffset() - 1, end.getStartOffset(), 0, 5, null, 
                                                         transaction);
                            }                            
                        } catch (BadLocationException ex) {                           
                        }                                                                     
                    }
                } finally {
                    transaction.commit();
                }
            }
        });
    }             
}
