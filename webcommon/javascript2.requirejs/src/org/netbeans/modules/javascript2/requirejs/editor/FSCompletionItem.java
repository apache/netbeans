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
package org.netbeans.modules.javascript2.requirejs.editor;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Petr Pisl
 */
public class FSCompletionItem implements CompletionProposal {

    private final FileObject file;
    private final ImageIcon icon;
    private final int anchor;
    private final String prefix;
    private final FSElementHandle element;
    private final boolean addExtension;
    
    public FSCompletionItem(final FileObject file, final String prefix, final boolean addExtension, final int anchor) throws IOException {
        this.file = file;
        this.element = new FSElementHandle(file);
        DataObject od = DataObject.find(file);

        icon = new ImageIcon(od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));

        this.anchor = anchor;
        this.addExtension = addExtension;
        this.prefix = prefix;
    }
    
    protected String getText() {
        return prefix + file.getNameExt() + (file.isFolder() ? "/" : "");
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FSCompletionItem)) {
            return false;
        }

        FSCompletionItem remote = (FSCompletionItem) o;

        return getFile().equals(remote.getFile());
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    @Override
    public int getAnchorOffset() {
        return this.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return this.element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return getName() + (file.isFolder() ? "/" : "");
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return file.getNameExt() + " "; //NOI18N
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return "";
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return file.isFolder() ? -1001 : -1000;
    }

    @Override
    public String getCustomInsertTemplate() {
        return prefix + ((addExtension || file.isFolder()) ? file.getNameExt() : file.getName()) + (file.isFolder() ? "/" : "");
    }

    public FileObject getFile() {
        return file;
    }
    
    public static class FSElementHandle implements ElementHandle {
        
        private final FileObject fo;
        private final Set<FileObject> representedFiles;

        public FSElementHandle(FileObject fo) {
            this.fo = fo;
            this.representedFiles = new HashSet<>(1);
            representedFiles.add(fo);
        }

        
        @Override
        public FileObject getFileObject() {
            return fo;
        }

        @Override
        public String getMimeType() {
            return fo.getMIMEType();
        }

        @Override
        public String getName() {
            return fo.isFolder() ? fo.getNameExt() : fo.getName();
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FILE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return fo.equals(handle.getFileObject());
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
        
        public void addRepresentedFile(FileObject fo) {
            if (!representedFiles.contains(fo)) {
                representedFiles.add(fo);
            }
        }

        public Set<FileObject> getRepresentedFiles() {
            return representedFiles;
        }
        
        
        
    }

}
