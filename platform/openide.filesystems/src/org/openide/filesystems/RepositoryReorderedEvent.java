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
package org.openide.filesystems;

import java.util.EventObject;


/** Fired when a filesystem pool is reordered.
 * @see Repository#reorder
 */
public class RepositoryReorderedEvent extends EventObject {
    static final long serialVersionUID = -5473107156345392581L;

    /** permutation */
    private int[] perm;

    /** Create a new reorder event.
     * @param fsp the filesystem pool being reordered
     * @param perm the permutation of filesystems in the pool
     */
    public RepositoryReorderedEvent(Repository fsp, int[] perm) {
        super(fsp);
        this.perm = perm;
    }

    /** Get the affected filesystem pool.
     * @return the pool
     */
    public Repository getRepository() {
        return (Repository) getSource();
    }

    /** Get the permutation of filesystems.
     * @return the permutation
     */
    public int[] getPermutation() {
        int[] nperm = new int[perm.length];
        System.arraycopy(perm, 0, nperm, 0, perm.length);

        return nperm;
    }
}
