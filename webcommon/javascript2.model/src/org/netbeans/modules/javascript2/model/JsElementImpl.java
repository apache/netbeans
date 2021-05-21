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
package org.netbeans.modules.javascript2.model;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsElement.Kind;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.ANONYMOUS_OBJECT;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.CLASS;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.CONSTRUCTOR;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.FIELD;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.FUNCTION;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.METHOD;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.OBJECT;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.OBJECT_LITERAL;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.PROPERTY;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.PROPERTY_GETTER;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.PROPERTY_SETTER;
import static org.netbeans.modules.javascript2.model.api.JsElement.Kind.VARIABLE;
import org.netbeans.modules.javascript2.model.spi.PlatformProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Pisl
 */
public abstract class JsElementImpl implements JsElement {

    private static final Logger LOG = Logger.getLogger(JsElementImpl.class.getSimpleName());
    
    private FileObject fileObject;

    private final String name;

    private boolean isDeclared;

    private final OffsetRange offsetRange;

    private final Set<Modifier> modifiers;

    private final String mimeType;

    private final String sourceLabel;

    public JsElementImpl(FileObject fileObject, String name, boolean isDeclared,
            OffsetRange offsetRange, Set<Modifier> modifiers,
            @NullAllowed String mimeType, @NullAllowed String sourceLabel) {
        this.fileObject = fileObject;
        this.name = name;
        if (offsetRange.getStart() > offsetRange.getEnd()) {
            LOG.log(Level.WARNING, "Suspicious offset range of element at \n", new Throwable());
        }
        this.offsetRange = offsetRange;
        this.modifiers = modifiers == null ? Collections.emptySet(): modifiers;
        this.isDeclared = isDeclared;
        assert mimeType == null || 
               isCorrectMimeType(mimeType) : mimeType;
        this.mimeType = mimeType;
        this.sourceLabel = sourceLabel;
    }
    
    private static boolean isCorrectMimeType(String mt) {
        if (JsTokenId.JAVASCRIPT_MIME_TYPE.equals(mt) || 
               JsTokenId.JSON_MIME_TYPE.equals(mt)) {
            return true;
        }
        MimePath mp = MimePath.get(mt);
        String inhType = mp.getInheritedType();
        return JsTokenId.JAVASCRIPT_MIME_TYPE.equals(inhType) 
                || JsTokenId.JSON_MIME_TYPE.equals(inhType);
    }
           
    @Override
    public ElementKind getKind() {
        return convertJsKindToElementKind(getJSKind());
    }
    
    @Override
    public FileObject getFileObject() {
        return fileObject;
    }
    
    protected void setFileObject(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public String getMimeType() {
        if (mimeType != null) {
            return mimeType;
        }
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
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
    public boolean isDeclared() {
        return isDeclared;
    }

    public void setDeclared(boolean isDeclared) {
        this.isDeclared = isDeclared;
    }
   
    @Override
    public final OffsetRange getOffsetRange(ParserResult result) {
        int start = result.getSnapshot().getOriginalOffset(offsetRange.getStart());
        if (start < 0) {
            return OffsetRange.NONE;
        }
        int end = result.getSnapshot().getOriginalOffset(offsetRange.getEnd());
        return new OffsetRange(start, end);
    }

    @Override
    public final OffsetRange getOffsetRange() {
        return offsetRange;
    }

    @Override
    public int getOffset() {
        return offsetRange.getStart();
    }
    
    @Override
    public Set<Modifier> getModifiers() {
         return modifiers;
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }
    
    public void addModifier(Modifier modifier) {
        modifiers.add(modifier);
    }

    @CheckForNull
    @Override
    public String getSourceLabel() {
        return sourceLabel;
    }

    @Override
    public boolean isPlatform() {
        FileObject fo = getFileObject();
        if (fo != null) {
            return isInternalFile(fo);
        }
        return false;
    }

    private static boolean isInternalFile(FileObject file) {
        PlatformProvider p = Lookup.getDefault().lookup(PlatformProvider.class);
        if (p == null) {
            return false;
        }
        for (FileObject dir : p.getPlatformStubs()) {
            if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                return true;
            }
        }
        return false;
    }
    
    public static ElementKind convertJsKindToElementKind(Kind jsKind) {
        ElementKind result = ElementKind.OTHER;
        switch (jsKind) {
            case CONSTRUCTOR: 
                result = ElementKind.CONSTRUCTOR;
                break;
            case METHOD:
            case FUNCTION:
            case PROPERTY_GETTER:
            case PROPERTY_SETTER:
            case GENERATOR:
                result = ElementKind.METHOD;
                break;
            case OBJECT:
            case ANONYMOUS_OBJECT:
            case OBJECT_LITERAL:
            case CLASS:
                result = ElementKind.CLASS;
                break;
            case CONSTANT:
                result = ElementKind.CONSTANT;
                break;
            case PROPERTY:
                result = ElementKind.FIELD;
                break;
            case FILE:
                result = ElementKind.FILE;
                break;
            case PARAMETER:
                result = ElementKind.PARAMETER;
                break;
            case VARIABLE:
                result = ElementKind.VARIABLE;
                break;
            case FIELD:
                result = ElementKind.FIELD;
                break;
            default:
                break;
        }
        return result;
    }
}
