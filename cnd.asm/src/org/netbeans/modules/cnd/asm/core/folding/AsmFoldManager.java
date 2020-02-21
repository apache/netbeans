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
