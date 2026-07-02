/*
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
package org.netbeans.modules.php.blade.editor.parser;

import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * helps identifying block directive context.
 * Ex: identifying if we have nested loops for foreach variable autocomplete
 *    @foreach($array1 as $item1)
 *       @foreach($array2 as $item2)
 * 
 * @author bogdan
 */
public class BladeScope {

    private final Map<OffsetRange, BladeDirectiveScope> scopeRange = new TreeMap<>();


    public void markScope(OffsetRange range, BladeDirectiveScope scope) {
        scopeRange.put(range, scope);
    }

    public BladeDirectiveScope findScope(int offset) {
        for (Map.Entry<OffsetRange, BladeDirectiveScope> entry : scopeRange.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return entry.getValue();
            }
        }

        return null;
    }

}
