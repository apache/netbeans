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

package org.netbeans.modules.editor.guards;

import java.util.Comparator;
import org.netbeans.api.editor.guards.GuardedSection;

/** Comparator of the guarded sections. It compares the begin position
* of the sections.
*/
final class GuardedPositionComparator implements Comparator<GuardedSection> {
    /** Compare two objects. Both have to be either SimpleSection
    * either InteriorSection instance.
    */
    public int compare(GuardedSection o1, GuardedSection o2) {
        return getOffset(o1) - getOffset(o2);
    }

    /** Computes the offset of the begin of the section. */
    private int getOffset(GuardedSection o) {
        return o.getStartPosition().getOffset();
    }
}
