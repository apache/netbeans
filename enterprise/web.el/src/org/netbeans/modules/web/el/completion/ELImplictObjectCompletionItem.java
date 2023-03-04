/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.el.completion;

import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;

/**
 * Code completion item for EL implicit objects.
 *
 *
 * @author erno
 */
final class ELImplictObjectCompletionItem extends DefaultCompletionProposal {

    private final String name;
    private final String clazz;

    public ELImplictObjectCompletionItem(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @Override
    public ElementHandle getElement() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CLASS;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return clazz;
    }

}
