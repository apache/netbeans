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

package org.netbeans.modules.groovy.editor.api.elements;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class ElementHandleSupport {

    public static ElementHandle createHandle(ParserResult info, final GroovyElement object) {
        if (object instanceof KeywordElement || object instanceof CommentElement) {
            // Not tied to an AST - just pass it around
            return new GroovyElementHandle(null, object, info.getSnapshot().getSource().getFileObject());
        }

        if (object instanceof IndexedElement) {
            // Probably a function in a "foreign" file (not parsed from AST),
            // such as a signature returned from the index of the Groovy libraries.
            // TODO - make sure this is infrequent! getFileObject is expensive!            
            // Alternatively, do this in a delayed fashion - e.g. pass in null and in getFileObject
            // look up from index            
            return new GroovyElementHandle(null, object, ((IndexedElement)object).getFileObject());
        }

        if (!(object instanceof ASTElement)) {
            return null;
        }

        // XXX Gotta fix this
        if (info == null) {
            return null;
        }
        
        ParserResult result = ASTUtils.getParseResult(info);

        if (result == null) {
            return null;
        }

        ASTNode root = ASTUtils.getRoot(info);

        return new GroovyElementHandle(root, object, info.getSnapshot().getSource().getFileObject());
    }

    @SuppressWarnings("unchecked")
    public static ElementHandle createHandle(ParserResult result, final ASTElement object) {
        ASTNode root = ASTUtils.getRoot(result);

        return new GroovyElementHandle(root, object, result.getSnapshot().getSource().getFileObject());
    }
    
    public static GroovyElement resolveHandle(ParserResult info, ElementHandle handle) {
        GroovyElementHandle h = (GroovyElementHandle) handle;
        ASTNode oldRoot = h.root;
        ASTNode oldNode;

        if (h.object instanceof KeywordElement || h.object instanceof IndexedElement || h.object instanceof CommentElement) {
            // Not tied to a tree
            return h.object;
        }

        if (h.object instanceof ASTElement) {
            oldNode = ((ASTElement)h.object).getNode(); // XXX Make it work for DefaultComObjects...
        } else {
            return null;
        }

        ASTNode newRoot = ASTUtils.getRoot(info);
        if (newRoot == null) {
            return null;
        }

        // Find newNode
        ASTNode newNode = find(oldRoot, oldNode, newRoot);

        if (newNode != null) {
            GroovyElement co = ASTElement.create(newNode);

            return co;
        }

        return null;
    }

    public static ElementHandle createHandle(String className, String elementName, ElementKind kind,
                Set<Modifier> modifiers) {
        return new SimpleElementHandle(className, elementName, kind, modifiers);
    }

    private static ASTNode find(ASTNode oldRoot, ASTNode oldObject, ASTNode newRoot) {
        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
        @SuppressWarnings("unchecked")
        List<?extends ASTNode> oldChildren = ASTUtils.children(oldRoot);
        @SuppressWarnings("unchecked")
        List<?extends ASTNode> newChildren = ASTUtils.children(newRoot);
        Iterator<?extends ASTNode> itOld = oldChildren.iterator();
        Iterator<?extends ASTNode> itNew = newChildren.iterator();

        while (itOld.hasNext()) {
            if (!itNew.hasNext()) {
                return null; // No match - the trees have changed structure
            }

            ASTNode o = itOld.next();
            ASTNode n = itNew.next();

            if (o == oldObject) {
                // Found it!
                return n;
            }

            // Recurse
            ASTNode match = find(o, oldObject, n);

            if (match != null) {
                return match;
            }
        }

        if (itNew.hasNext()) {
            return null; // No match - the trees have changed structure
        }

        return null;
    }

    private static class GroovyElementHandle implements ElementHandle {
        private final ASTNode root;
        private final GroovyElement object;
        private final FileObject fileObject;

        private GroovyElementHandle(ASTNode root, GroovyElement object, FileObject fileObject) {
            this.root = root;
            this.object = object;
            this.fileObject = fileObject;
        }

        public boolean signatureEquals(ElementHandle handle) {
            // XXX TODO
            return false;
        }

        public FileObject getFileObject() {
            if (object instanceof IndexedElement) {
                return ((IndexedElement) object).getFileObject();
            }

            return fileObject;
        }

        public String getMimeType() {
            return GroovyTokenId.GROOVY_MIME_TYPE;
        }

        public String getName() {
            return object.getName();
        }

        public String getIn() {
            return object.getIn();
        }

        public ElementKind getKind() {
            return object.getKind();
        }

        public Set<Modifier> getModifiers() {
            return object.getModifiers();
        }

        // FIXME parsing API
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

    }

    // FIXME could it be ElementKind.OTHER or can we use url?
    private static class SimpleElementHandle implements ElementHandle {

        private final String className;

        private final String elementName;

        private final ElementKind kind;

        private final Set<Modifier> modifiers;

        public SimpleElementHandle(String className, String elementName, ElementKind kind,
                Set<Modifier> modifiers) {
            this.className = className;
            this.elementName = elementName;
            this.kind = kind;
            this.modifiers = modifiers;
        }

        public FileObject getFileObject() {
            return null;
        }

        public String getIn() {
            return className;
        }

        public ElementKind getKind() {
            return kind;
        }

        public String getMimeType() {
            return GroovyTokenId.GROOVY_MIME_TYPE;
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        public String getName() {
            return elementName;
        }

        public boolean signatureEquals(ElementHandle handle) {
            // FIXME
            return false;
        }

        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.className);
            hash = 79 * hash + Objects.hashCode(this.elementName);
            hash = 79 * hash + Objects.hashCode(this.kind);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimpleElementHandle other = (SimpleElementHandle) obj;
            if (!Objects.equals(this.className, other.className)) {
                return false;
            }
            if (!Objects.equals(this.elementName, other.elementName)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            if (!Objects.equals(this.modifiers, other.modifiers)) {
                return false;
            }
            return true;
        }

    }

}
