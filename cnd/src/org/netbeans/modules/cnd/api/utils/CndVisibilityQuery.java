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

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

public final class CndVisibilityQuery {
    private final ChangeSupport cs = new ChangeSupport(this);

    private Pattern ignorePattern = null;

    public CndVisibilityQuery(String ignoredRegex){
        ignorePattern = Pattern.compile(ignoredRegex);
    }

    public void setIgnoredPattern(String regex) {
        if (ignorePattern != null && regex != null && !ignorePattern.pattern().equals(regex)) {
            ignorePattern = Pattern.compile(regex);
            cs.fireChange();
        }
        else if (ignorePattern == null && regex != null) {
            ignorePattern = Pattern.compile(regex);
            cs.fireChange();
        }
    }

    public String getRegEx() {
        return ignorePattern.pattern();
    }

    public boolean isIgnored(FileObject file) {
        return isIgnored(file.getNameExt());
    }

    public boolean isIgnored(File file) {
        return isIgnored(file.getName());
    }

    private boolean isIgnored(final String fileName) {
        return ignorePattern.matcher(fileName).find();
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
}
