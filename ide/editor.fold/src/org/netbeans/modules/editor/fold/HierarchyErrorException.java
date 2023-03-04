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

import org.netbeans.api.editor.fold.Fold;

/**
 * Exception thrown internally to indicate bad things inside
 * the fold hierarchy.
 * 
 * @author sdedic
 */
public class HierarchyErrorException extends IllegalStateException {
    private final Fold    parentFold;
    private final Fold    insertOrRemove;
    private final int     opAtIndex;
    private final boolean add;

    public HierarchyErrorException(Fold parentFold, Fold insertOrRemove, int opAtIndex, boolean add, String s) {
        super(s);
        this.parentFold = parentFold;
        this.insertOrRemove = insertOrRemove;
        this.opAtIndex = opAtIndex;
        this.add = add;
    }

    public Fold getParentFold() {
        return parentFold;
    }

    public Fold getInsertOrRemove() {
        return insertOrRemove;
    }

    public int getOpAtIndex() {
        return opAtIndex;
    }

    public boolean isAdd() {
        return add;
    }
}
