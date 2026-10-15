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

import java.util.Objects;

public final class SetScriptSourceRequest {
    private String scriptId;
    private String scriptSource;
    private Boolean dryRun;

    public SetScriptSourceRequest() {
    }

    /**
     * Id of the script to edit.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Id of the script to edit.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    /**
     * New content of the script.
     */
    public String getScriptSource() {
        return scriptSource;
    }

    /**
     * New content of the script.
     */
    public void setScriptSource(String scriptSource) {
        this.scriptSource = scriptSource;
    }

    /**
     * If true the change will not actually be applied. Dry run may be used to
     * get result description without actually modifying the code.
     */
    public Boolean getDryRun() {
        return dryRun;
    }

    /**
     * If true the change will not actually be applied. Dry run may be used to
     * get result description without actually modifying the code.
     */
    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public String toString() {
        return "SetScriptSourceRequest{" + "scriptId=" + scriptId + ", scriptSource=" + scriptSource + ", dryRun=" + dryRun + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.scriptId);
        hash = 47 * hash + Objects.hashCode(this.scriptSource);
        hash = 47 * hash + Objects.hashCode(this.dryRun);
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
        final SetScriptSourceRequest other = (SetScriptSourceRequest) obj;
        if (!Objects.equals(this.scriptId, other.scriptId)) {
            return false;
        }
        if (!Objects.equals(this.scriptSource, other.scriptSource)) {
            return false;
        }
        return Objects.equals(this.dryRun, other.dryRun);
    }
}
