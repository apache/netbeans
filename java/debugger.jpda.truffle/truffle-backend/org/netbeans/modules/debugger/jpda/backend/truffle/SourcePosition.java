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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Martin
 */
final class SourcePosition {

    private static final Map<Source, Long> sourceId = new WeakHashMap<>();
    private static long nextId = 0;

    final long id;
    final String name;
    final String hostClassName;
    final String hostMethodName;
    final String path;
    final String sourceSection;
    final String code;
    final URI uri;
    final String mimeType;

    public SourcePosition(SourceSection sourceSection, LanguageInfo languageInfo) {
        if (sourceSection != null) {
            Source source = sourceSection.getSource();
            this.id = getId(source);
            this.name = source.getName();
            this.hostClassName = null;
            this.hostMethodName = null;
            String sourcePath = source.getPath();
            if (sourcePath == null) {
                sourcePath = name;
            }
            this.path = sourcePath;
            this.sourceSection = sourceSection.getStartLine() + "," + sourceSection.getStartColumn() + "," + sourceSection.getEndLine() + "," + sourceSection.getEndColumn();
            this.code = source.getCharacters().toString();
            this.uri = source.getURI();
            this.mimeType = findMIMEType(source, languageInfo);
        } else {
            this.id = -1;
            this.name = this.hostClassName = this.hostMethodName = this.path = this.sourceSection = this.code = this.mimeType = null;
            this.uri = null;
        }
    }

    public SourcePosition(StackTraceElement ste) {
        this.id = -1;
        this.name = ste.getFileName() != null ? ste.getFileName() : ste.getClassName();
        this.hostClassName = ste.getClassName();
        this.hostMethodName = ste.getMethodName();
        this.path = ste.getFileName();
        this.sourceSection = ste.getLineNumber() + "," + 0 + "," + ste.getLineNumber() + "," + 0;
        this.code = null;
        this.uri = URI.create("");
        this.mimeType = null;
    }

    private String findMIMEType(Source source, LanguageInfo languageInfo) {
        String mimeType = source.getMimeType();
        if (mimeType == null && languageInfo != null) {
            mimeType = languageInfo.getDefaultMimeType();
        }
        return mimeType;
    }

    private static synchronized long getId(Source s) {
        Long id = sourceId.get(s);
        if (id == null) {
            id = nextId++;
            sourceId.put(s, id);
        }
        return id;
    }

}
