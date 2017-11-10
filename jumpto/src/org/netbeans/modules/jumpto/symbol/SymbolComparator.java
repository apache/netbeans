/**
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

package org.netbeans.modules.jumpto.symbol;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;

/**
 * The {@code SymbolComparator} establishes the sort order of the symbols.
 * It is used for ordering a list that will be displayed in:
 * <ul>
 *   <li>the field "Symbols Found" of the dialog "Go to Symbol"
 *      (Ctrl+Alt+Shift+O)</li>
 * </ul>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public class SymbolComparator extends EntityComparator<SymbolDescriptor> {

    /**
     * Compares its two {@code SymbolDescriptor}s for order.
     * <p>
     * This method establishes the following groups for order
     * (from lowest to highest):
     * <ul>
     * <li>Symbols being defined in the main project (if any)</li>
     * <li>Symbols being defined in the projects that are opened in the
     *     IDE's GUI</li>
     * <li>Symbols being defined in other accessible projects.</li>
     * </ul>
     * </p>
     * The alphabetical order of the symbol names is established inside each
     * group.<br/>
     * If the symbols names are the same then the alphabetical order of
     * the owner names of the symbols is used.<br/>
     *
     * @param e1 the first {@code SymbolDescriptor} to be compared.
     * @param e2 the second {@code SymbolDescriptor} to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * 	   first argument is less than, equal to, or greater than the
     *	   second.
     */
    @Override
    public int compare(SymbolDescriptor e1, SymbolDescriptor e2) {
        String e1projectName = e1.getProjectName();
        String e2projectName = e2.getProjectName();
        int result = compareProjects(e1projectName, e2projectName);
        if(result != 0) {
            return result; // e1projectName NOT equals to e2projectName
        }
        // here: e1projectName equals to e2projectName
        result = compare(getSortName(e1), getSortName(e2));
        if ( result != 0 ) {
           return result;
        }
        // here: e1Name equals to e2Name
        return compare(e1.getOwnerName(), e2.getOwnerName());
    }

    @NonNull
    private static String getSortName(@NonNull final SymbolDescriptor d) {
        String res = d.getSimpleName();
        if (res == null) {
            res = d.getSymbolName();
        }
        return res;
    }
}
