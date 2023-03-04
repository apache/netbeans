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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.model.CodeMarker;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
final class FileScopeImpl extends ScopeImpl implements FileScope  {

    private PHPParseResult info;
    private final List<CodeMarkerImpl> codeMarkers = Collections.synchronizedList(new ArrayList<CodeMarkerImpl>());

    FileScopeImpl(PHPParseResult info) {
        this(info, "program"); //NOI18N
    }

    private FileScopeImpl(PHPParseResult info, String name) {
        super(
                null,
                name,
                Union2.<String, FileObject>createSecond(info != null ? info.getSnapshot().getSource().getFileObject() : null),
                new OffsetRange(0, 0),
                PhpElementKind.PROGRAM,
                false);
        this.info = info;
    }

    void addCodeMarker(CodeMarkerImpl codeMarkerImpl) {
        codeMarkers.add(codeMarkerImpl);
    }

    List<? extends CodeMarker> getMarkers() {
        return codeMarkers;
    }

    void clearMarkers() {
        codeMarkers.clear();
    }

    /**
     * @return the indexScope
     */
    @Override
    public IndexScope getIndexScope() {
        return info.getModel().getIndexScope();
    }

    @Override
    public Collection<? extends NamespaceScope> getDeclaredNamespaces() {
        return filter(getElements(), new ElementFilter<NamespaceScope>() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.NAMESPACE_DECLARATION);
            }
        });
    }


    @Override
    public NamespaceScope getDefaultDeclaredNamespace() {
        return ModelUtils.getFirst(ModelUtils.filter(getDeclaredNamespaces(), new ModelUtils.ElementFilter<NamespaceScope>() {
            @Override
            public boolean isAccepted(NamespaceScope ns) {
                return ns.isDefaultNamespace();
            }
        }));
    }
}
