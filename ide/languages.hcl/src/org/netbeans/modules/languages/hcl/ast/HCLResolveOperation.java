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
public abstract class HCLResolveOperation extends HCLExpression {

    public final HCLExpression base;

    public HCLResolveOperation(HCLExpression base) {
        this.base = base;
    }

    @Override
    public List<? extends HCLExpression> getChildren() {
        return Collections.singletonList(base);
    }

    public final static class Attribute extends HCLResolveOperation {
        public final HCLIdentifier attr;

        public Attribute(HCLExpression base, HCLIdentifier attr) {
            super(base);
            this.attr = attr;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + ": ." + attr;
        }

        
        @Override
        public String asString() {
            return base.asString() + "." + attr;
        }
        
    }

    public final static class Index extends HCLResolveOperation {
        public final HCLExpression index;
        public final boolean legacy;

        public Index(HCLExpression base, HCLExpression index, boolean legacy) {
            super(base);
            this.index = index;
            this.legacy = legacy;
        }

        @Override
        public String asString() {
            return base.asString() + (legacy ? "." + index : "[" + index + "]");
        }

        @Override
        public List<? extends HCLExpression> getChildren() {
            return Arrays.asList(base, index);
        }
    }
    
    public final static class AttrSplat extends HCLResolveOperation {

        public AttrSplat(HCLExpression base) {
            super(base);
        }

        @Override
        public String asString() {
            return base.asString() + ".*";
        }
        
    }

    public final static class FullSplat extends HCLResolveOperation {

        public FullSplat(HCLExpression base) {
            super(base);
        }

        @Override
        public String asString() {
            return base.asString() + "[*]";
        }
        
    }
}
