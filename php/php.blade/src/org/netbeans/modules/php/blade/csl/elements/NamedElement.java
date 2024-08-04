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
package org.netbeans.modules.php.blade.csl.elements;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import static org.netbeans.modules.php.blade.csl.elements.ElementType.CUSTOM_DIRECTIVE;
import static org.netbeans.modules.php.blade.csl.elements.ElementType.STACK_ID;
import static org.netbeans.modules.php.blade.csl.elements.ElementType.YIELD_ID;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bhaidu
 */
public class NamedElement implements ElementHandle {

    protected final String name;
    protected final FileObject file;
    protected final ElementType type;

    public NamedElement(String name, FileObject file) {
        this.name = name;
        this.file = file;
        this.type = ElementType.NA;
    }

    public NamedElement(String name, FileObject file, ElementType type) {
        this.name = name;
        this.file = file;
        this.type = type;
    }

    @Override
    public FileObject getFileObject() {
        return file;
    }

    @Override
    public String getMimeType() {
        return BladeLanguage.MIME_TYPE;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getIn() {
        return "";
    }

    @Override
    public ElementKind getKind() {
        switch (type) {
            case YIELD_ID:
                return ElementKind.PACKAGE;
            case STACK_ID:
                return ElementKind.PACKAGE;
            case CUSTOM_DIRECTIVE:
                return ElementKind.METHOD;
            case PHP_CLASS:
                return ElementKind.CLASS;
        }
        return ElementKind.FILE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return new HashSet<>();
    }

    @Override
    public boolean signatureEquals(ElementHandle eh) {
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult pr) {
        return OffsetRange.NONE;
    }

    public ElementType getType() {
        return type;
    }
    
}
