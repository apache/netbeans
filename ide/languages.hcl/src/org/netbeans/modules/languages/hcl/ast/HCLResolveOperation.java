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
public sealed interface HCLResolveOperation extends HCLExpression {


    HCLExpression base();
    default List<? extends HCLExpression> elements() {
            return List.of(base());
    }

    public record Attribute(HCLExpression base, HCLIdentifier attr) implements HCLResolveOperation {
        @Override
        public String asString() {
            return base.asString() + "." + attr;
        }

    }

    public record Index(HCLExpression base, HCLExpression index, boolean legacy) implements HCLResolveOperation {
        @Override
        public String asString() {
            return base.asString() + (legacy ? "." + index : "[" + index + "]");
        }
        @Override
        public List<? extends HCLExpression> elements() {
            return List.of(base, index);
        }
    }
    
    public record AttrSplat(HCLExpression base) implements HCLResolveOperation {
        @Override
        public String asString() {
            return base.asString() + ".*";
        }
    }

    public record FullSplat(HCLExpression base) implements HCLResolveOperation {
        @Override
        public String asString() {
            return base.asString() + "[*]";
        }
    }
}
