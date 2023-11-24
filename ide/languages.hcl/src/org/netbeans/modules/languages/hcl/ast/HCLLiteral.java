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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author lkishalmi
 */
public abstract class HCLLiteral extends HCLExpression {

    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);
    public static final Null NULL = new Null();

    @Override
    public final List<? extends HCLExpression> getChildren() {
        return Collections.emptyList();
    }
    
    public static final class Bool extends HCLLiteral {

        final boolean value;

        private Bool(boolean value) {
            this.value = value;
        }

        @Override
        public String asString() {
            return value ? "true" : "false";
        }
    }

    public static final class StringLit extends HCLLiteral {

        final String value;

        public StringLit(String value) {
            this.value = value;
        }

        @Override
        public String asString() {
            return "\"" + value + "\"";
        }
    }

    public static final class Null extends HCLLiteral {


        private Null() {
        }

        @Override
        public String asString() {
            return "null"; //NOI18N
        }
    }

    public static final class NumericLit extends HCLLiteral {

        final String value;

        public NumericLit(String value) {
            this.value = value;
        }

        @Override
        public String asString() {
            return value;
        }
    }
    
}
