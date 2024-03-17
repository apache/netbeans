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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class HCLBlock extends HCLContainer  {

    private final String id;

    private final List<HCLIdentifier> declaration;

    public HCLBlock(List<HCLIdentifier> declaration, List<HCLElement> elements) {
        super(elements);
        this.declaration = List.copyOf(declaration);
        this.id = declaration.stream().map(d -> d.id()).collect(Collectors.joining("."));
    }

    public List<HCLIdentifier> declaration() {
        return declaration;
    }

    public String id() {
        return id;
    }
    @Override
    public String toString() {
        return "HCLBlock[declaration=" + declaration + ", elements=" + elements + "]";
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof HCLBlock that ? Objects.equals(this.declaration, that.declaration) && Objects.equals(this.elements, that.elements) : false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaration, elements);
    }
}
