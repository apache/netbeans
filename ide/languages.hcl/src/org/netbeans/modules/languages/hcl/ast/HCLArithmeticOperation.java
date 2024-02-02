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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author lkishalmi
 */
public abstract class HCLArithmeticOperation extends HCLExpression {

    public enum Operator {
        NOT("!"),
        MINUS("-"),

        MUL("*"),
        DIV("/"),
        MOD("%"),

        ADD("+"),
        SUB("-"),

        OR("||"),
        AND("&&"),

        LTE("<="),
        GTE(">="),
        LT("<"),
        GT(">"),
        EQUALS("=="),
        NOT_EQUALS("!=");

        final String op;

        private Operator(String op) {
          this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }

    public final Operator op;

    public HCLArithmeticOperation(Operator op) {
        this.op = op;
    }

    public final static class Binary extends HCLArithmeticOperation {
        public final HCLExpression left;
        public final HCLExpression right;

        public Binary(Operator op, HCLExpression left, HCLExpression right) {
            super(op);
            this.left = left;
            this.right = right;
        }

        @Override
        public String asString() {
            return left.toString() + op.toString() + right.toString();
        }

        @Override
        public List<? extends HCLExpression> getChildren() {
            return Arrays.asList(left, right);
        }
                
    } 

    public final static class Unary extends HCLArithmeticOperation {
        public final HCLExpression operand;

        public Unary(Operator op, HCLExpression operand) {
            super(op);
            this.operand = operand;
        }
        
        @Override
        public String asString() {
            return op.toString() + operand.toString();
        }

        @Override
        public List<? extends HCLExpression> getChildren() {
            return Collections.singletonList(operand);
        }
        
    } 
}
