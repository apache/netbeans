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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
