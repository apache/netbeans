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

import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author bogdan
 */
public class BladeComponentTagOccurences {

    private final Set<OffsetRange> bladeComponentTagOcurences = new TreeSet<>();

    public void markPhpExpressionOccurence(OffsetRange range) {
        bladeComponentTagOcurences.add(range);
    }

    public OffsetRange findOffsetBladeComponentTag(int offset) {
        for (OffsetRange range : bladeComponentTagOcurences) {

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return range;
            }
        }

        return null;
    }
}
