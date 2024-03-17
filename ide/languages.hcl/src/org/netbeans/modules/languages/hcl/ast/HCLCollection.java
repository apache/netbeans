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
import java.util.StringJoiner;

/**
 *
 * @author lkishalmi
 */
public sealed interface HCLCollection<T> extends HCLExpression {

    public record Tuple(List<HCLExpression> elements) implements  HCLCollection<HCLExpression> {

        public Tuple {
            elements = List.copyOf(elements);
        }

        @Override
        public String asString() {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            for (HCLExpression element : elements) {
                sj.add(element.asString());
            }
            return sj.toString();
        }

    }

    public record ObjectElement(HCLExpression key, HCLExpression value) implements HCLExpression {
        @Override
        public String asString() {
            return key.asString() + "=" + value.asString();
        }

        @Override
        public List<? extends HCLExpression> elements() {
            return List.of(key, value);
        }
    }
    
    public record Object(List<ObjectElement> elements) implements HCLCollection<ObjectElement> {

        public Object {
            elements = List.copyOf(elements);
        }

        @Override
        public String asString() {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            for (ObjectElement element : elements) {
                sj.add(element.toString());
            }
            return sj.toString();
        }        
    }
}
