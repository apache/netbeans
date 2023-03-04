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

package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.model.IncludeElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.IncludeInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class IncludeElementImpl extends ModelElementImpl implements IncludeElement {
    private String fileName;
    private OffsetRange referenceSpanRange;

    IncludeElementImpl(Scope inScope, IncludeInfo info) {
        super(
                inScope,
                "include", //NOI18N
                Union2.<String, FileObject>createSecond(inScope.getFileObject()),
                new OffsetRange(0, 0),
                info.getPhpElementKind(),
                PhpModifiers.noModifiers(),
                false);
        this.fileName = info.getFileName();
        this.referenceSpanRange = info.getRange();
    }

    @Override
    public OffsetRange getReferenceSpanRange() {
        return referenceSpanRange;
    }

    @Override
    public FileObject getFileObject() {
        FileObject fileObject = super.getFileObject();
        return VariousUtils.resolveInclude(fileObject, fileName);
    }

    @Override
    public String getNormalizedName() {
        return getName() + String.valueOf(getOffset());
    }
}
