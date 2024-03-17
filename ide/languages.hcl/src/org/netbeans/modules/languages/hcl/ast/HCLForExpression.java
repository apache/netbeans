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
package org.netbeans.modules.languages.hcl.ast;

import java.util.List;

/**
 *
 * @author lkishalmi
 */
public sealed interface HCLForExpression extends HCLExpression {
    
    HCLIdentifier keyVar();
    HCLIdentifier valueVar();

    HCLExpression iterable();
    HCLExpression condition();
    
    public record Tuple(HCLIdentifier keyVar, HCLIdentifier valueVar, HCLExpression iterable, HCLExpression condition, HCLExpression result) implements HCLForExpression {

        @Override
        public String asString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[for ");
            if (keyVar != null) {
                sb.append(keyVar).append(',');
            }
            sb.append(valueVar).append(" in ").append(iterable.asString()).append(':');
            sb.append(result.asString());
            if (condition != null) { 
                sb.append(" if ").append(condition.asString());
            }
            sb.append(']');
            return sb.toString();
        }
        
        @Override
        public List<? extends HCLExpression> elements() {
            return List.of(iterable, result, condition);
        }
    }
    
    public record Object(HCLIdentifier keyVar, HCLIdentifier valueVar, HCLExpression iterable, HCLExpression condition, HCLExpression resultKey, HCLExpression resultValue, boolean grouping) implements HCLForExpression {
        @Override
        public String asString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{for ");
            if (keyVar != null) {
                sb.append(keyVar).append(',');
            }
            sb.append(valueVar).append(" in ").append(iterable.asString()).append(':');
            sb.append(resultKey.asString()).append("=>").append(resultValue.asString());
            if (grouping) {
                sb.append("...");
            }
            if (condition != null) { 
                sb.append(" if ").append(condition.asString());
            }
            sb.append('}');
            return sb.toString();
        }

        @Override
        public List<? extends HCLExpression> elements() {
            return List.of(iterable, resultKey, resultValue, condition);
        }
        
    }
    
}
