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
package org.netbeans.modules.cnd.source.spi;

import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;

/**
 * If such a provider is defined, then for C/C++ data objects pane creation
 * is delegated to the provider. It can anyhow return null,
 * in which case standard pane will be created.
 */
public interface CndPaneProvider {

    /**
     * The method is called each time an editor for C/C++ file is opened.
     * If method returns non null Pane, this pane is used for editing.
     * If it returns null, then standard C/C++ pane  is created.
     * @param DataEditorSupport 
     * @return Pane or null
     */
    Pane createPane(DataEditorSupport support);
}
