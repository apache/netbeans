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

package org.netbeans.modules.versioning.util.common;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JComponent;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSCommitDiffProvider<T extends VCSFileNode> {
    
    private final HashMap<File, JComponent> displayedDiffs = new HashMap<File, JComponent>();
        
    JComponent getDiffComponent(File file) {
        JComponent component = displayedDiffs.get(file);
        if (component == null) {
            component = createDiffComponent(file); //new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false); // switch the last parameter to true if editable diff works poorly
            displayedDiffs.put(file, component);                
        }   
        return component;
    }

    protected abstract JComponent createDiffComponent(File file);
    
    protected JComponent getDiffComponent (T[] files) {
        return null;
    }

    protected Set<File> getModifiedFiles() {
        return Collections.emptySet();
    }

    protected SaveCookie[] getSaveCookies() {
        return new SaveCookie[0];
    }

    protected EditorCookie[] getEditorCookies() {
        return new EditorCookie[0];
    }

    /**
     * Selects the file in the opened diff view. Makes sense only if the diff
     * view is capable of showing more files.
     * @param file file to select
     */
    protected void selectFile (File file) {
        
    }
}
