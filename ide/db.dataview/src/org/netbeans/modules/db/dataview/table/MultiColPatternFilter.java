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
package org.netbeans.modules.db.dataview.table;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

/**
 *
 * @author ahimanikya
 */
public class MultiColPatternFilter extends SuperPatternFilter {

    private final int[] cols;

    public MultiColPatternFilter(final int... cols) {
        super(0);
        final int numCols = cols.length;
        this.cols = new int[numCols];
        System.arraycopy(cols, 0, this.cols, 0, numCols);
    }

    @Override
    public boolean include(RowFilter.Entry<? extends TableModel,? extends Integer> entry)  {
        for (int colIdx : cols) {
            Object val = entry.getValue(colIdx);
            if (testValue(val)) {
                    return true;
                }
            }
        return false;
    }
}

