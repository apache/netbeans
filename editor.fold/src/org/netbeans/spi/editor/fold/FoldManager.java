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

package org.netbeans.spi.editor.fold;

import javax.swing.event.DocumentEvent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;

/**
 * Fold manager maintains folds in the hierarchy for which it is constructed.
 * <br>
 * There can be multiple individually operating managers
 * over a single fold hierarchy each managing its own set of folds.
 * <br>
 * The only time when the fold managers can collide is when they
 * attempt to add overlapping folds into fold hierarchy.
 * <br>
 * In that case the fold from the manager with a higher priority
 * (will be explained later) will be added to the hierarchy
 * and the other one will remain outside of the hierarchy
 * until the colliding fold will get removed from the hierarchy.
 * <br>
 * The priority of the manager (and all its folds)
 * in the list of the managers for the particular hierarchy
 * is defined by the order of the fold manager's factories
 * in the layer
 * (see {@link FoldManagerFactory} for more information).
 *
 * <p>
 * The fold manager typically creates an initial set of folds
 * during the {@link #initFolds(FoldHierarchyTransaction)}.
 * Those folds typically mimic certain "primary data structure"
 * e.g. java folds mimic certain AST nodes created by a java parser.
 * <br>
 * Typically the fold manager attaches a listener
 * to a primary data structure and once it gets
 * notified about its change it should rebuild the folds accordingly.
 * <br>
 * That set can later be modified upon notifications
 * from primary data structure.
 *
 * <p>
 * Upon notification the folds can be updated synchronously
 * but that can potentially lead to deadlocks in case the view
 * hierarchy (which shares the same lock with fold hierarchy) would 
 * access the primary data structure at the same time.
 * <br>
 * A safer approach is to remember the changes during notification
 * from the primary data structure
 * and schedule the updates to the fold hierarchy to be done
 * independently.
 * <br>
 * Ideally the physical creation of folds should be done in EDT
 * (Event Dispatch Thread) because there would be no risk
 * of the document switching in the text component
 * by {@link javax.swing.text.JTextComponent#setDocument(javax.swing.text.Document)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface FoldManager {
    
    /**
     * Initialize this manager.
     *
     * @param operation fold hierarchy operation dedicated to the fold manager.
     */
    void init(FoldOperation operation);
    
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
    void initFolds(FoldHierarchyTransaction transaction);
    
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
    void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction);
    
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
    void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction);
    
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
    void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction);
    
    /**
     * Notify that the fold was removed from hierarchy automatically
     * by fold hierarchy infrastructure processing
     * because it became empty (by a document modification).
     */
    void removeEmptyNotify(Fold epmtyFold);
    
    /**
     * Notify that the fold was removed from hierarchy automatically
     * by fold hierarchy infrastructure processing
     * because it was damaged by a document modification.
     */
    void removeDamagedNotify(Fold damagedFold);
    
    /**
     * Notify that the fold was expanded automatically
     * by fold hierarchy infrastructure processing
     * because its <code>isExpandNecessary()</code>
     * return true.
     */
    void expandNotify(Fold expandedFold);

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
    void release();

}
