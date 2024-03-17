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
public record HCLConditionalOperation(HCLExpression condition, HCLExpression trueValue, HCLExpression falseValue) implements HCLExpression {

    @Override
    public String asString() {
        return condition.toString() + "?" + trueValue.toString() + ":" + falseValue.toString();
    }

    @Override
    public List<? extends HCLExpression> elements() {
        return Arrays.asList(condition, trueValue, falseValue);
    }
        
    
}
