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
 * @author Laszlo Kishalmi
 */
public sealed abstract class HCLContainer implements HCLElement permits HCLBlock, HCLDocument {

    protected final List<HCLElement> elements;
    private final List<HCLBlock> blocks;
    private final List<HCLAttribute> attributes;

    protected HCLContainer(List<HCLElement> elements) {
        this.elements = List.copyOf(elements);
        this.blocks = elements.stream().filter(HCLBlock.class::isInstance).map(HCLBlock.class::cast).toList();
        this.attributes = elements.stream().filter(HCLAttribute.class::isInstance).map(HCLAttribute.class::cast).toList();
    }

    public boolean hasBlock() {
        return !blocks.isEmpty();
    }

    public boolean hasAttribute() {
        return !attributes.isEmpty();
    }

    public List<HCLBlock> blocks() {
        return blocks;
    }

    public List<HCLAttribute> attributes() {
        return attributes;
    }
    
    @Override
    public List<? extends HCLElement> elements() {
        return elements;
    }
}
