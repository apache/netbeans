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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class HCLContainer extends HCLElement {
    final List<HCLElement> elements = new LinkedList<>();

    final List<HCLBlock> blocks = new LinkedList<>();
    final List<HCLAttribute> attributes = new LinkedList<>();

    public void add(HCLBlock block) {
        block.parent = this;
        elements.add(block);
        blocks.add(block);
    }

    public void add(HCLAttribute attr) {
        attr.parent = this;
        elements.add(attr);
        attributes.add(attr);
    }

    public Collection<? extends HCLBlock> getBlocks() {
        return Collections.unmodifiableCollection(blocks);
    }
}
