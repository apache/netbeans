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

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

/**
 * Information about column/row changes.
 *
 * @author Jan Stola
 */
public class GridBoundsChange {
    /** Old column bounds. */
    private int[] oldColumnBounds;
    /** Old row bounds. */
    private int[] oldRowBounds;
    /** New column bounds. */
    private int[] newColumnBounds;
    /** New row bounds. */
    private int[] newRowBounds;

    /**
     * Creates new {@code GridBoundsChange}.
     *
     * @param oldColumnBounds old column bounds.
     * @param oldRowBounds old row bounds.
     * @param newColumnBounds new column bounds.
     * @param newRowBounds new row bounds.
     */
    public GridBoundsChange(int[] oldColumnBounds, int[] oldRowBounds,
            int[] newColumnBounds, int[] newRowBounds) {
        this.oldColumnBounds = oldColumnBounds;
        this.oldRowBounds = oldRowBounds;
        this.newColumnBounds = newColumnBounds;
        this.newRowBounds = newRowBounds;
    }

    /**
     * Returns new column bounds.
     *
     * @return new column bounds.
     */
    public int[] getNewColumnBounds() {
        return newColumnBounds;
    }

    /**
     * Returns new row bounds.
     *
     * @return new row bounds.
     */
    public int[] getNewRowBounds() {
        return newRowBounds;
    }

    /**
     * Returns old column bounds.
     *
     * @return old column bounds.
     */
    public int[] getOldColumnBounds() {
        return oldColumnBounds;
    }

    /**
     * Returns old row bounds.
     *
     * @return old row bounds.
     */
    public int[] getOldRowBounds() {
        return oldRowBounds;
    }

}
