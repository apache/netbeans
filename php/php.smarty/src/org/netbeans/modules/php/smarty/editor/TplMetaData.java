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
package org.netbeans.modules.php.smarty.editor;

import org.netbeans.modules.php.smarty.SmartyFramework;

/** Holds data relevant to the TPL coloring for one TPL page.
 *
 * @author Martin Fousek
 */
public final class TplMetaData {

    private String openDelimiter, closeDelimiter;
    private SmartyFramework.Version version;
    private boolean initialized;

    public TplMetaData(String openDelimiter, String closeDelimiter, SmartyFramework.Version version) {
        this.openDelimiter = openDelimiter;
        this.closeDelimiter = closeDelimiter;
        this.version = version;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean initialized() {
        boolean oldVal = initialized;
        this.initialized = true;
        return oldVal;
    }

    /** Updates coloring data. The update is initiated by saving project properties. */
    public void updateMetaData(String openDelimiter, String closeDelimiter) {
        this.openDelimiter = openDelimiter;
        this.closeDelimiter = closeDelimiter;
    }

    public String getOpenDelimiter() {
        return openDelimiter;
    }

    public String getCloseDelimiter() {
        return closeDelimiter;
    }

    public SmartyFramework.Version getSmartyVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TplMetaData:");
        buf.append(" openDelim=");
        buf.append(getOpenDelimiter());
        buf.append("; closeDelim=");
        buf.append(getCloseDelimiter());
        buf.append(')');

        return buf.toString();
    }
}
