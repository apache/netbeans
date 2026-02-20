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
package org.netbeans.modules.languages.env.completion;

import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.languages.env.EnvKeyHandle;

public class KeyCompletionProposal extends DefaultCompletionProposal {

    private final ElementHandle element;

    public KeyCompletionProposal(EnvKeyHandle element, int anchorOffset) {
        this.element = element;
        this.anchorOffset = anchorOffset;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.name(getKind(), true);
        formatter.appendHtml("<font>"); // NOI18N
        formatter.appendHtml("<b>"); // NOI18N
        formatter.appendText(getName());
        formatter.appendHtml("</b>"); // NOI18N
        formatter.appendHtml("</font>"); // NOI18N
        formatter.name(getKind(), false); // NOI18N
        return formatter.getText();
    }
}
