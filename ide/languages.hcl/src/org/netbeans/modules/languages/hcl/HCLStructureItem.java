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
package org.netbeans.modules.languages.hcl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLAttribute;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLContainer;
import org.netbeans.modules.languages.hcl.ast.HCLElement;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public class HCLStructureItem implements ElementHandle, StructureItem {

    final HCLElement element;
    final SourceRef references;
    private List<? extends StructureItem> nestedCache;

    public HCLStructureItem(HCLElement element, SourceRef references) {
        this.element = element;
        this.references = references;
    }

    @Override
    public FileObject getFileObject() {
        return references.getFileObject();
    }

    @Override
    public String getMimeType() {
        return getFileObject().getMIMEType();
    }

    @Override
    public String getName() {
        if (element instanceof HCLAttribute a) {
            return a.id();
        } else if (element instanceof HCLBlock b) {
            return b.id();
        }
        return  "<" + element.getClass().getSimpleName() + ">";
    }

    @Override
    public String getIn() {
        return null;
    }


    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return references.getOffsetRange(element).orElse(OffsetRange.NONE);
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        return getName();
    }

    @Override
    public ElementHandle getElementHandle() {
        return this;
    }

    @Override
    public boolean isLeaf() {
        return element instanceof HCLAttribute;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        if (nestedCache == null) {
            if (element instanceof HCLContainer) {
                HCLContainer c = (HCLContainer) element;
                List<HCLStructureItem> nested = new ArrayList<>();
                for (HCLBlock block : c.blocks()) {
                    nested.add(new HCLStructureItem(block, references));
                }
                for (HCLAttribute attribute : c.attributes()) {
                    nested.add(new HCLStructureItem(attribute, references));
                }
                nestedCache = nested;
            } else {
                nestedCache = Collections.emptyList();
            }
        }
        return nestedCache;
    }

    @Override
    public long getPosition() {
        return getOffsetRange(null).getStart();
    }

    @Override
    public long getEndPosition() {
        return getOffsetRange(null).getEnd();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return element instanceof HCLAttribute ? ElementKind.ATTRIBUTE : ElementKind.TAG;
    }

    
}
