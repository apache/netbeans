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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;

import javax.swing.text.JTextComponent;

/**
 * Errorstripe mark provider for Diff pane.
 * 
 * @author Maros Sandor
 */
public class DiffMarkProviderCreator implements MarkProviderCreator {
    
    static final String MARK_PROVIDER_KEY = "org.netbeans.modules.diff.builtin.visualizer.editable.MarkProvider";

    public MarkProvider createMarkProvider(JTextComponent component) {
        return (MarkProvider) component.getClientProperty(MARK_PROVIDER_KEY);
    }
}
