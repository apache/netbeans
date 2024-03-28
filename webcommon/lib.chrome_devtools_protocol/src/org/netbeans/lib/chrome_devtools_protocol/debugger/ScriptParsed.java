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
package org.netbeans.lib.chrome_devtools_protocol.debugger;

import java.net.URI;
import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace;

/**
 * Fired when virtual machine fails to parse the script.
 */
public final class ScriptParsed {
    private String scriptId;
    private URI url;
    private int startLine;
    private int startColumn;
    private int endLine;
    private int endColumn;
    private int executionContextId;
    private String hash;
    private Object executionContextAuxData;
    private Boolean isLiveEdit;
    private URI sourceMapURL;
    private Boolean hasSourceURL;
    private Boolean isModule;
    private Integer length;
    private StackTrace stackTrace;
    private Integer codeOffset;
    private String scriptLanguage;
    private DebugSymbols debugSymbols;
    private String embedderName;

    public ScriptParsed() {
    }

    /**
     * Identifier of the script parsed.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Identifier of the script parsed.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    /**
     * URL or name of the script parsed (if any).
     */
    public URI getUrl() {
        return url;
    }

    /**
     * URL or name of the script parsed (if any).
     */
    public void setUrl(URI url) {
        this.url = url;
    }

    /**
     * Line offset of the script within the resource with given URL (for script
     * tags).
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Line offset of the script within the resource with given URL (for script
     * tags).
     */
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    /**
     * Column offset of the script within the resource with given URL.
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Column offset of the script within the resource with given URL.
     */
    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    /**
     * Last line of the script.
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Last line of the script.
     */
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    /**
     * Length of the last line of the script.
     */
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * Length of the last line of the script.
     */
    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    /**
     * Specifies script creation context.
     */
    public int getExecutionContextId() {
        return executionContextId;
    }

    /**
     * Specifies script creation context.
     */
    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }

    /**
     * Content hash of the script, SHA-256.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Content hash of the script, SHA-256.
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Embedder-specific auxiliary data.
     */
    public Object getExecutionContextAuxData() {
        return executionContextAuxData;
    }

    /**
     * Embedder-specific auxiliary data.
     */
    public void setExecutionContextAuxData(Object executionContextAuxData) {
        this.executionContextAuxData = executionContextAuxData;
    }

    /**
     * URL of source map associated with script (if any)
     */
    public URI getSourceMapURL() {
        return sourceMapURL;
    }

     /**
     * URL of source map associated with script (if any)
     */
    public void setSourceMapURL(URI sourceMapURL) {
        this.sourceMapURL = sourceMapURL;
    }

    /**
     * True, if this script has sourceURL.
     */
    public Boolean getHasSourceURL() {
        return hasSourceURL;
    }

    /**
     * True, if this script has sourceURL.
     */
    public void setHasSourceURL(Boolean hasSourceURL) {
        this.hasSourceURL = hasSourceURL;
    }

    /**
     * True, if this script is ES6 module.
     */
    public Boolean getIsModule() {
        return isModule;
    }

    /**
     * True, if this script is ES6 module.
     */
    public void setIsModule(Boolean isModule) {
        this.isModule = isModule;
    }

    /**
     * This script length.
     */
    public Integer getLength() {
        return length;
    }

    /**
     * This script length.
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * JavaScript top stack frame of where the script parsed event was triggered
     * if available.
     * <p><strong>Experimental</strong></p>
     */
    public StackTrace getStackTrace() {
        return stackTrace;
    }

    /**
     * JavaScript top stack frame of where the script parsed event was triggered
     * if available.
     * <p><strong>Experimental</strong></p>
     */
    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * If the scriptLanguage is WebAssembly, the code section offset in the
     * module.
     * <p><strong>Experimental</strong></p>
     */
    public Integer getCodeOffset() {
        return codeOffset;
    }

    /**
     * If the scriptLanguage is WebAssembly, the code section offset in the
     * module.
     * <p><strong>Experimental</strong></p>
     */
    public void setCodeOffset(Integer codeOffset) {
        this.codeOffset = codeOffset;
    }

    /**
     * The language of the script.
     * <p><strong>Experimental</strong></p>
     */
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    /**
     * The language of the script.
     * <p><strong>Experimental</strong></p>
     */
    public void setScriptLanguage(String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }

    /**
     * The name the embedder supplied for this script.
     * <p><strong>Experimental</strong></p>
     */
    public String getEmbedderName() {
        return embedderName;
    }

    /**
     * The name the embedder supplied for this script.
     * <p><strong>Experimental</strong></p>
     */
    public void setEmbedderName(String embedderName) {
        this.embedderName = embedderName;
    }

    /**
     * True, if this script is generated as a result of the live edit operation.
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getIsLiveEdit() {
        return isLiveEdit;
    }

    /**
     * True, if this script is generated as a result of the live edit operation.
     * <p><strong>Experimental</strong></p>
     */
    public void setIsLiveEdit(Boolean isLiveEdit) {
        this.isLiveEdit = isLiveEdit;
    }

    /**
     * If the scriptLanguage is WebASsembly, the source of debug symbols for the
     * module.
     * <p><strong>Experimental</strong></p>
     */
    public DebugSymbols getDebugSymbols() {
        return debugSymbols;
    }

    /**
     * If the scriptLanguage is WebASsembly, the source of debug symbols for the
     * module.
     * <p><strong>Experimental</strong></p>
     */
    public void setDebugSymbols(DebugSymbols debugSymbols) {
        this.debugSymbols = debugSymbols;
    }

    @Override
    public String toString() {
        return "ScriptParsed{" + "scriptId=" + scriptId + ", url=" + url + ", startLine=" + startLine + ", startColumn=" + startColumn + ", endLine=" + endLine + ", endColumn=" + endColumn + ", executionContextId=" + executionContextId + ", hash=" + hash + ", executionContextAuxData=" + executionContextAuxData + ", isLiveEdit=" + isLiveEdit + ", sourceMapURL=" + sourceMapURL + ", hasSourceURL=" + hasSourceURL + ", isModule=" + isModule + ", length=" + length + ", stackTrace=" + stackTrace + ", codeOffset=" + codeOffset + ", scriptLanguage=" + scriptLanguage + ", debugSymbols=" + debugSymbols + ", embedderName=" + embedderName + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.scriptId);
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
        final ScriptParsed other = (ScriptParsed) obj;
        return Objects.equals(this.scriptId, other.scriptId);
    }

}
