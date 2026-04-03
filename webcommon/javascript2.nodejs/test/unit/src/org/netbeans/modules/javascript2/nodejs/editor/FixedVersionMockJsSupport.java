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
package org.netbeans.modules.javascript2.nodejs.editor;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript2.nodejs.spi.NodeJsSupport;
import org.netbeans.modules.web.common.api.Version;
import org.openide.filesystems.FileObject;

/**
 * Mock NodeJsSupport to enable providing a fixed node js version and not use
 * fallback.
 */
public class FixedVersionMockJsSupport implements NodeJsSupport {

    private final String version;

    public FixedVersionMockJsSupport(String version) {
        this.version = version;
    }

    @Override
    public boolean isSupportEnabled() {
        return true;
    }

    @Override
    public Version getVersion() {
        return Version.fromDottedNotationWithFallback(version);
    }

    @Override
    public String getDocumentationUrl() {
        return "https://nodejs.org/docs/v" + version + "/api/";
    }

    @Override
    public FileObject getDocumentationFolder() {
        return null;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        // Ignore
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        // Ignore
    }
}
