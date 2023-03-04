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

package org.netbeans.modules.javascript2.requirejs.editor;

import java.util.Collections;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class MappingCompletionItem implements CompletionProposal {
    
    private static ImageIcon REQUIREJS_ICON = null;
   
    private final int anchor;
    private final MappingHandle element;
    private final FileObject mapToFile;
    
    public MappingCompletionItem(final String mapping, FileObject toFile, final int anchor){
        this.element = new MappingHandle(mapping, toFile);
        this.anchor = anchor;
        this.mapToFile = toFile;
    }
    
    protected String getText() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MappingCompletionItem)) {
            return false;
        }

        MappingCompletionItem remote = (MappingCompletionItem) o;

        return getText().equals(remote.getText());
    }

    @Override
    public ImageIcon getIcon() {
        
        if (REQUIREJS_ICON == null) {
            REQUIREJS_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/requirejs/resources/requirejs.png")); //NOI18N
        }
        return REQUIREJS_ICON;
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
        return getName() + (mapToFile != null && mapToFile.isFolder() ? "/" : "");
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return getText();
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
        return element.getModifiers();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return -1000;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    
    private static class MappingHandle extends SimpleHandle.DocumentationHandle {
        private final FileObject toFile;

        public MappingHandle(String name, final FileObject toFile) {
            super(name, ElementKind.FILE);
            this.toFile = toFile;
        }
        
        
        @Override
        @NbBundle.Messages({"mappingTo=Mapped to", 
            "mappedFile=file", "mappedFolder=folder", "mappedToVirtual=Mapped to non existing folder / file."})
        public String getDocumentation() {
            StringBuilder sb = new StringBuilder();
            sb.append(Bundle.mappingTo()).append(" "); //NOI18N
            if (toFile != null) {
                if (toFile.isFolder()) {
                    sb.append(Bundle.mappedFolder());
                } else {
                    sb.append(Bundle.mappedFile());
                }
                sb.append(FSCompletionUtils.writeFilePathForDocWindow(toFile));
            } else {
                return Bundle.mappedToVirtual();
            }
            return sb.toString();
        }
    }
}
