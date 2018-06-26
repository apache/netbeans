/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
