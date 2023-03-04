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
package org.netbeans.modules.javascript2.nodejs.editor;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class NodeJsElement implements ElementHandle {

    private final String name;
    private final ElementKind kind;
    private final String documentation;
    private final String template;
    private final FileObject fo;

    public NodeJsElement(FileObject fo, String name, String documentation, ElementKind kind) {
        this(fo, name, documentation, null, kind);
    }
    
    public NodeJsElement(FileObject fo, String name, String documentation, String template, ElementKind kind) {
        this.name = name;
        this.kind = kind;
        this.documentation = documentation;
        this.template = template;
        this.fo = fo;
    }

    @Override
    public FileObject getFileObject() {
        return fo;
    }

    @Override
    public String getMimeType() {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return "";
    }

    @Override
    public ElementKind getKind() {
        return kind;
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
    
    public String getDocumentation() {
        return documentation;
    }

    public String getTemplate() {
        return template;
    }

    public static class NodeJsFileElement extends NodeJsElement {
        public NodeJsFileElement(FileObject file) {
            super(file, file.getNameExt(), null, ElementKind.FILE);
        }

        @Override
        public String getDocumentation() {
            return super.getDocumentation(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FileObject getFileObject() {
            return super.getFileObject(); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public String getMimeType() {
            return getFileObject().getMIMEType();
        }

        @Override
        public String getName() {
            FileObject fo = getFileObject();
            return fo.isFolder() ? fo.getNameExt() : fo.getName();
        }
        
        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return getFileObject().equals(handle.getFileObject());
        }
    }
    
    public static class NodeJsModuleElement extends NodeJsElement {

        public NodeJsModuleElement(final FileObject fo, final String name) {
            super(fo, name, null, ElementKind.MODULE);
        }

        @Override
        public String getDocumentation() {
            FileObject fo = getFileObject();    
            return fo == null ? null : NodeJsDataProvider.getDefault(fo).getDocForModule(getName());
        }
    }
    
    public static class NodeJsLocalModuleElement extends NodeJsElement {

        public NodeJsLocalModuleElement(final FileObject fo, final String name) {
            super(fo, name, null, ElementKind.MODULE);
        }

        @NbBundle.Messages("NodeJsLocalModuleElement.lbl.location=Module located at {0}") //NOI18N
        @Override
        public String getDocumentation() {
            StringBuilder sb = new StringBuilder();
            if (getFileObject() != null) {
                sb.append(NodeJsDataProvider.getDefault(getFileObject()).getDocForLocalModule(getFileObject()));
                sb.append("<br/><br/>");
            }
            sb.append(Bundle.NodeJsLocalModuleElement_lbl_location(NodeJsUtils.writeFilePathForDocWindow(getFileObject())));
            return sb.toString();
        }
    }

}
