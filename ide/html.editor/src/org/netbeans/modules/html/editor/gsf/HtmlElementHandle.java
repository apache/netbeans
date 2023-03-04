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
package org.netbeans.modules.html.editor.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 * Represents a handle for OpenTags obtained from HtmlParserResult. 
 * 
 * The handle may be held out of the parsing task and later resolved
 * back to node by {@link #resolve(org.netbeans.modules.csl.spi.ParserResult) 
 *
 * @author mfukala@netbeans.org
 */
public class HtmlElementHandle implements ElementHandle {

    private final FileObject fo;
    private final String name;
    private final String elementPath;
    private final int attributesHash;

    public HtmlElementHandle(OpenTag node, FileObject fo) {
        this.fo = fo;
        this.name = node.id().toString();
        this.elementPath = ElementUtils.encodeToString(new TreePath(node));
        this.attributesHash = computeAttributesHash(node);
    }
    
    private int computeAttributesHash(OpenTag node) {
        int hash = 11;
        for(Attribute a : node.attributes()) {
           hash = 37 * hash + a.name().hashCode();
           CharSequence value = a.value();
           hash = 37 * hash + (value != null ? value.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public FileObject getFileObject() {
        return fo;
    }

    @Override
    public String getMimeType() {
        return HtmlKit.HTML_MIME_TYPE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.TAG;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        if (!(handle instanceof HtmlElementHandle)) {
            return false;
        }
        HtmlElementHandle htmlHandle = (HtmlElementHandle)handle;
        return htmlHandle.elementPath.equals(elementPath);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.elementPath != null ? this.elementPath.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HtmlElementHandle other = (HtmlElementHandle) obj;
        if ((this.elementPath == null) ? (other.elementPath != null) : !this.elementPath.equals(other.elementPath)) {
            return false;
        }
        if ((this.attributesHash != other.attributesHash)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return elementPath;
    }
    
    public OpenTag resolve(ParserResult result) {
        if(!(result instanceof HtmlParserResult)) {
            return null;
        }
        
        HtmlParserResult htmlParserResult = (HtmlParserResult)result;
        Node root = htmlParserResult.root();
        
        return ElementUtils.query(root, elementPath);
    }
        
    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        OpenTag node = resolve(result);
        if(node == null) {
            return OffsetRange.NONE;
        }
        
        Snapshot snapshot = result.getSnapshot();
        int dfrom = snapshot.getOriginalOffset(node.from());
        int dto = snapshot.getOriginalOffset(node.semanticEnd());
        
        return dfrom != -1 && dto != -1 ? new OffsetRange(dfrom, dto) : OffsetRange.NONE;
    }
    
}
