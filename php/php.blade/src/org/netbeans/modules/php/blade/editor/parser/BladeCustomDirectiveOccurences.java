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
 *
 * @author bogdan
 */
public class BladeCustomDirectiveOccurences {

    private final Map<OffsetRange, String> customDirectiveOccurences = new TreeMap<>();

    public void markPhpExpressionOccurence(OffsetRange range, String directiveLabel) {
        customDirectiveOccurences.put(range, directiveLabel);
    }

    public Map<OffsetRange, String> getAll() {
        return customDirectiveOccurences;
    }
    
    public CustomDirectiveOccurence findCustomDirectiveOccurence(int offset) {
        for (Map.Entry<OffsetRange, String> entry : customDirectiveOccurences.entrySet()) {

            if (offset < entry.getKey().getStart()) {
                //excedeed the offset range
                break;
            }

            if (entry.getKey().containsInclusive(offset)) {
                return new CustomDirectiveOccurence(entry.getKey(), entry.getValue());
            }
        }

        return null;
    }
    
    public static class CustomDirectiveOccurence{
        public final OffsetRange range;
        public final String directiveName;
        
        public CustomDirectiveOccurence(OffsetRange range, String directiveName){
            this.range = range;
            this.directiveName = directiveName;
        }
    }
}
