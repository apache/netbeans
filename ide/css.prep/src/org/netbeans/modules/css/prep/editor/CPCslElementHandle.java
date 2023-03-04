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
package org.netbeans.modules.css.prep.editor;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CPCslElementHandle implements ElementHandle {
      
    private final CharSequence name;
    private final FileObject file;
    private final OffsetRange range;
    private final CPElementType type;

    public CPCslElementHandle(CharSequence name) {
        this(null, name);
    }

    public CPCslElementHandle(FileObject file, CharSequence name) {
        this(file, name, null, null);
    }

    public CPCslElementHandle(FileObject file, CharSequence name, OffsetRange range, CPElementType type) {
        this.file = file;
        this.name = name;
        this.range = range;
        this.type = type;
    }
     
    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public FileObject getFileObject() {
        return file;
    }

    @Override
    public String getMimeType() {
        return "text/css";
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        switch(type) {
            case MIXIN_DECLARATION:
                return ElementKind.METHOD;
            case VARIABLE_GLOBAL_DECLARATION:
            case VARIABLE_LOCAL_DECLARATION:
            case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
                return ElementKind.VARIABLE;
            default:
                return null;
        }
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
        return range;
    }
}
