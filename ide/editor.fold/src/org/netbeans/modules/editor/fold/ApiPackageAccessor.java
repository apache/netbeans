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

package org.netbeans.modules.editor.fold;

import java.util.Collection;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.fold.FoldHierarchyMonitor;
import org.openide.util.Lookup;

/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class ApiPackageAccessor {
    
    private static ApiPackageAccessor INSTANCE;
    
    public static ApiPackageAccessor get() {
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(ApiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
        // this is a HACK; forces registration of the view.
        MimeLookup.getLookup(MimePath.EMPTY).lookup(FoldHierarchyMonitor.class);
    }
    
    public abstract FoldHierarchy createFoldHierarchy(FoldHierarchyExecution execution);

    public abstract Fold createFold(FoldOperationImpl operation,
    FoldType type, String description, boolean collapsed,
    Document doc, int startOffset, int endOffset,
    int startGuardedLength, int endGuardedLength,
    Object extraInfo)
    throws BadLocationException;
    
    public abstract FoldHierarchyEvent createFoldHierarchyEvent(FoldHierarchy source,
    Fold[] removedFolds, Fold[] addedFolds, FoldStateChange[] foldStateChanges,
    int affectedStartOffset, int affectedEndOffset);

    public abstract FoldStateChange createFoldStateChange(Fold fold);

    public abstract void foldSetCollapsed(Fold fold, boolean collapsed);
    
    public abstract void foldSetParent(Fold fold, Fold parent);
    
    public abstract void foldTearOut(Fold f, Collection c);

    public abstract void foldExtractToChildren(Fold fold, int index, int length, Fold targetFold);

    public abstract Fold foldReplaceByChildren(Fold fold, int index);

    public abstract void foldSetDescription(Fold fold, String description);

    public abstract void foldSetStartOffset(Fold fold, Document doc, int startOffset)
    throws BadLocationException;
    
    public abstract void foldSetEndOffset(Fold fold, Document doc, int endOffset)
    throws BadLocationException;
    
    public abstract boolean foldIsStartDamaged(Fold fold);

    public abstract boolean foldIsEndDamaged(Fold fold);

//    public abstract boolean foldIsExpandNecessary(Fold fold);

    public abstract void foldInsertUpdate(Fold fold, DocumentEvent evt);

    public abstract void foldRemoveUpdate(Fold fold, DocumentEvent evt);
    
    public abstract FoldOperationImpl foldGetOperation(Fold fold);
    
    public abstract int foldGetRawIndex(Fold fold);

    public abstract void foldSetRawIndex(Fold fold, int rawIndex);

    public abstract void foldUpdateRawIndex(Fold fold, int rawIndexDelta);

    public abstract Object foldGetExtraInfo(Fold fold);
    
    public abstract void foldSetExtraInfo(Fold fold, Object info);

    public abstract void foldStateChangeCollapsedChanged(FoldStateChange fsc);
    
    public abstract void foldStateChangeDescriptionChanged(FoldStateChange fsc);

    public abstract void foldStateChangeStartOffsetChanged(FoldStateChange fsc,
    int originalStartOffset);
    
    public abstract void foldStateChangeEndOffsetChanged(FoldStateChange fsc,
    int originalEndOffset);
    
    public abstract FoldHierarchyExecution foldGetExecution(FoldHierarchy fh);
    
    public abstract void foldMarkDamaged(Fold f, int dmgBits);
    
    public abstract int foldStartGuardedLength(Fold f);
    
    public abstract int foldEndGuardedLength(Fold f);

}
