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

package org.netbeans.modules.jumpto.type;

import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.spi.jumpto.type.TypeDescriptor;

/**
 * The {@code TypeComparator} establishes the sort order of the types.
 * It is used for ordering a list that will be displayed in:
 * <ul>
 *   <li>the field "Types Found" of the dialog "Go to Type" (Ctrl+O)</li>
 *   <li>the results of the quick Search (Ctrl+I) in category "Go To Type"</li>
 * </ul>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public class TypeComparator extends EntityComparator<TypeDescriptor> {

    private final boolean caseSensitive;

    public TypeComparator(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public TypeComparator() {
        this(true);
    }

    /**
     * Compares its two {@code TypeDescriptor}s for order.
     * <p>
     * This method establishes the following groups for order
     * (from lowest to highest):
     * <ul>
     * <li>Types being defined in the main project (if any)</li>
     * <li>Types being defined in the projects that are opened in the
     *     IDE's GUI</li>
     * <li>Types being defined in other accessible projects.</li>
     * </ul>
     * </p>
     * The alphabetical order of the type names is established inside each 
     * group.<br/>
     * If the type names are the same then the alphabetical order of
     * the outer names of the types is used.<br/>
     * If the outer names are the same then alphabetical order of 
     * the context names of the types is used.
     *
     * @param e1 the first {@code TypeDescriptor} to be compared.
     * @param e2 the second {@code TypeDescriptor} to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * 	   first argument is less than, equal to, or greater than the
     *	   second.
     */
    @Override
    public int compare(TypeDescriptor e1, TypeDescriptor e2) {
        String e1projectName = e1.getProjectName();
        String e2projectName = e2.getProjectName();
        int result = compareProjects(e1projectName, e2projectName);
        if(result != 0) {
            return result; // e1projectName NOT equals to e2projectName
        }
        // here: e1projectName equals to e2projectName
        result = compare(e1.getTypeName(), e2.getTypeName(), caseSensitive);
        if ( result != 0 ) {
           return result;
        }
        // here: e1Name equals to e2Name
        result = compare(e1.getOuterName(), e2.getOuterName());
        if ( result != 0 ) {
           return result;
        }
        // here: e1OuterName equals to e2OuterName
        return compare(e1.getContextName(), e2.getContextName());
    }

}
