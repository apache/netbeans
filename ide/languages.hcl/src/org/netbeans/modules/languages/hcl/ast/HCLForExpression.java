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

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lkishalmi
 */
public abstract class HCLForExpression extends HCLExpression {
    
    public final HCLIdentifier keyVar;
    public final HCLIdentifier valueVar;

    public final HCLExpression iterable;
    public final HCLExpression condition;
    
    public HCLForExpression(HCLIdentifier keyVar, HCLIdentifier valueVar, HCLExpression iterable, HCLExpression condition) {
        this.keyVar = keyVar;
        this.valueVar = valueVar;
        this.iterable = iterable;
        this.condition = condition;
    }

    public final static class Tuple extends HCLForExpression {

        public final HCLExpression result;
        
        public Tuple(HCLIdentifier keyVar, HCLIdentifier valueVar, HCLExpression iterable, HCLExpression condition, HCLExpression result) {
            super(keyVar, valueVar, iterable, condition);
            this.result = result;
        }

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
        public List<? extends HCLExpression> getChildren() {
            return Arrays.asList(iterable, result, condition);
        }
    }
    
    public final static class Object extends HCLForExpression {
        public final HCLExpression resultKey;
        public final HCLExpression resultValue;
        
        public final boolean grouping;

        public Object(HCLIdentifier keyVar, HCLIdentifier valueVar, HCLExpression iterable, HCLExpression condition, HCLExpression resultKey, HCLExpression resultValue, boolean grouping) {
            super(keyVar, valueVar, iterable, condition);
            this.resultKey = resultKey;
            this.resultValue = resultValue;
            this.grouping = grouping;
        }

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
        public List<? extends HCLExpression> getChildren() {
            return Arrays.asList(iterable, resultKey, resultValue, condition);
        }
        
    }
    
}
