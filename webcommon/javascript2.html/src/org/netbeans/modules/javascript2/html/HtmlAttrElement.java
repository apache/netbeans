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
package org.netbeans.modules.javascript2.html;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.javascript2.editor.spi.ElementDocumentation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
class HtmlAttrElement implements ElementHandle, ElementDocumentation {
    
    private final HtmlTagAttribute attribute;

    public HtmlAttrElement(HtmlTagAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public FileObject getFileObject() {
        return null;
    }

    @Override
    public String getMimeType() {
        return "";
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public String getIn() {
        return "";
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.<Modifier>emptySet();
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return OffsetRange.NONE;
    }

    @Override
    public Documentation getDocumentation() {
        HelpItem hi = attribute.getHelp();
        if (hi == null) {
            return null;
        }
        String content = attribute.getHelp().getHelpContent();
        if (content == null) {
            if (attribute.getHelp().getHelpResolver() != null && attribute.getHelp().getHelpURL() != null) {
                content = attribute.getHelp().getHelpResolver().getHelpContent(attribute.getHelp().getHelpURL());
            }
        }
        // PENDING: it's now possible to return URL, that will be loaded by editor infrastructure.
        return Documentation.create(content);
    }
    
}
