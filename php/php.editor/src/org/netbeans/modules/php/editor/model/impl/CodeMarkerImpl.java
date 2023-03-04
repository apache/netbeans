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

import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.CodeMarker;
import org.netbeans.modules.php.editor.model.OccurrenceHighlighter;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;

/**
 *
 * @author Radek Matous
 */
class CodeMarkerImpl implements CodeMarker {
    private final OffsetRange range;
    private final FileScopeImpl fileScope;

    public CodeMarkerImpl(ASTNodeInfo nodeInfo, FileScopeImpl fileScope) {
        this(nodeInfo.getRange(), fileScope);
    }

    public CodeMarkerImpl(OffsetRange  range, FileScopeImpl fileScope) {
         this.range = range;
         this.fileScope = fileScope;
    }

    @Override
    public List<? extends CodeMarker> getAllMarkers() {
        return fileScope.getMarkers();
    }

    @Override
    public boolean containsInclusive(int offset) {
        return range.containsInclusive(offset);
    }

    @Override
    public void highlight(OccurrenceHighlighter highlighter) {
        highlighter.add(range);
    }

    public static final class InvisibleCodeMarker extends CodeMarkerImpl {

        public InvisibleCodeMarker(ASTNodeInfo nodeInfo, FileScopeImpl fileScope) {
            super(nodeInfo, fileScope);
        }

        public InvisibleCodeMarker(OffsetRange range, FileScopeImpl fileScope) {
            super(range, fileScope);
        }

        @Override
        public void highlight(OccurrenceHighlighter highlighter) {
        }

    }

}
