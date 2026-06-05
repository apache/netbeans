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
package org.netbeans.modules.web.el.completion;

import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;

/**
 *
 * @author mfukala@netbeans.org
 */
final class ELRawObjectPropertyCompletionItem extends DefaultCompletionProposal {

    private final String propertyName;
    private final String clazz;

    public ELRawObjectPropertyCompletionItem(String propertyName, String clazz) {
        this.propertyName = propertyName;
        this.clazz = clazz;
    }

    @Override
    public ElementHandle getElement() {
        return null;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PROPERTY;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        formatter.type(true);
        formatter.appendText(clazz);
        formatter.type(false);
        return formatter.getText();
    }

}
