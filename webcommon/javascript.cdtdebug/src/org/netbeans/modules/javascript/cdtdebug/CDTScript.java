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
package org.netbeans.modules.javascript.cdtdebug;

import java.net.URI;
import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptFailedToParse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptParsed;

public class CDTScript {
    private final String scriptId;
    private final URI url;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;
    private final String hash;
    private final long length;

    public CDTScript(ScriptFailedToParse dto) {
        this.scriptId = dto.getScriptId();
        this.url = dto.getUrl();
        this.startLine = dto.getStartLine();
        this.startColumn = dto.getStartColumn();
        this.endLine = dto.getEndLine();
        this.endColumn = dto.getEndColumn();
        this.hash = dto.getHash();
        this.length = dto.getLength()== null ? -1 : dto.getLength().longValue();
    }

    public CDTScript(ScriptParsed dto) {
        this.scriptId = dto.getScriptId();
        this.url = dto.getUrl();
        this.startLine = dto.getStartLine();
        this.startColumn = dto.getStartColumn();
        this.endLine = dto.getEndLine();
        this.endColumn = dto.getEndColumn();
        this.hash = dto.getHash();
        this.length = dto.getLength()== null ? -1 : dto.getLength().longValue();
    }

    public String getScriptId() {
        return scriptId;
    }

    public URI getUrl() {
        return url;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public String getHash() {
        return hash;
    }

    public long getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.scriptId);
        hash = 83 * hash + Objects.hashCode(this.hash);
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
        final CDTScript other = (CDTScript) obj;
        if (!Objects.equals(this.scriptId, other.scriptId)) {
            return false;
        }
        return Objects.equals(this.hash, other.hash);
    }

}
