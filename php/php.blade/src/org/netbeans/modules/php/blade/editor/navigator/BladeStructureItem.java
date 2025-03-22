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
package org.netbeans.modules.php.blade.editor.navigator;

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
import org.netbeans.modules.php.blade.editor.ResourceUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bhaidu
 */
public abstract class BladeStructureItem implements ElementHandle, StructureItem {

    final String name;
    final FileObject source;
    final int startOffset;
    final int stopOffset;

    public BladeStructureItem(String name, FileObject source, int startOffset, int stopOffset) {
        this.name = name;
        this.source = source;
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
    }

    @Override
    public String getSortText() {
        return String.format("[%8d]", this.startOffset).replace(' ', '0');
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        formatter.appendText(name);
        return formatter.getText();
    }

    @Override
    public ElementHandle getElementHandle() {
        return this;
    }

    @Override
    public long getPosition() {
        return startOffset;
    }

    @Override
    public long getEndPosition() {
        return stopOffset;
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public FileObject getFileObject() {
        return source;
    }

    @Override
    public String getMimeType() {
        return source.getMIMEType();
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
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return new OffsetRange(startOffset, stopOffset);
    }

    public static final class DirectiveBlockStructureItem extends BladeStructureItem {

        private String identifier;

        public final List<BladeStructureItem> nestedItems = new ArrayList<>();

        public DirectiveBlockStructureItem(String name, FileObject source, int startOffset, int stopOffset) {
            super(name, source, startOffset, stopOffset);
        }

        public DirectiveBlockStructureItem(String name, String identifier, FileObject source, int startOffset, int stopOffset) {
            super(name, source, startOffset, stopOffset);
            this.identifier = identifier;
        }

        @Override
        public boolean isLeaf() {
            return nestedItems.isEmpty();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return nestedItems;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(name);
            if (identifier != null) {
                formatter.appendText(" ");
                formatter.appendHtml("<em>");
                formatter.appendHtml("<font color='5b5b5b'>");
                formatter.appendText(identifier);
                formatter.appendHtml("</font>");
                formatter.appendHtml("</em>");
            }
            return formatter.getText();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return ResourceUtilities.loadLayoutIcon();
        }
    }

    public static class DirectiveInlineStructureItem extends BladeStructureItem {

        private String identifier;

        public DirectiveInlineStructureItem(String name, FileObject source, int startOffset, int stopOffset) {
            super(name, source, startOffset, stopOffset);
        }

        public DirectiveInlineStructureItem(String name, String identifier, FileObject source, int startOffset, int stopOffset) {
            super(name, source, startOffset, stopOffset);
            this.identifier = identifier;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(name);
            if (identifier != null) {
                formatter.appendText(" ");
                formatter.appendHtml("<em>");
                formatter.appendHtml("<font color='5b5b5b'>");
                formatter.appendText(identifier);
                formatter.appendHtml("</font>");
                formatter.appendHtml("</em>");
            }
            return formatter.getText();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public ImageIcon getCustomIcon() {
            return ResourceUtilities.loadLayoutIcon();
        }
    }

}
