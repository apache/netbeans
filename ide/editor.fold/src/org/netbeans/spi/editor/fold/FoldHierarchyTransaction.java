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

package org.netbeans.spi.editor.fold;

import org.netbeans.modules.editor.fold.FoldHierarchyTransactionImpl;

/**
 * Class encapsulating a modification
 * of the code folding hierarchy.
 * <br>
 * It's provided by {@link FoldOperation#openTransaction()}.
 * <br>
 * It can accumulate arbitrary number of changes of various folds
 * by being passed as argument to particular methods in the FoldOperation.
 * <br>
 * Only one transaction can be active at the time.
 * <br>
 * Once all the modifications are done the transaction must be
 * committed by {@link #commit()} which creates
 * a {@link org.netbeans.api.editor.fold.FoldHierarchyEvent}
 * and fires it to the listeners automatically.
 * <br>
 * Once the transaction is committed no additional
 * changes can be made to it.
 * <br>
 * There is currently no way to rollback the transaction.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyTransaction {
    
    private final FoldHierarchyTransactionImpl impl;

    FoldHierarchyTransaction(FoldHierarchyTransactionImpl impl) {
        this.impl = impl;
    }

    /**
     * Commit this transaction.
     * <br>
     * Transaction can only be committed once.
     *
     * @throws IllegalStateException if the transaction is attempted
     *  to be commited more than once.
     */
    public void commit() {
        impl.commit();
    }
    
    FoldHierarchyTransactionImpl getImpl() {
        return impl;
    }

}
