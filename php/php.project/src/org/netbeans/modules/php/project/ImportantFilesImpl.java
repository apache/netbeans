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
package org.netbeans.modules.php.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public final class ImportantFilesImpl implements org.netbeans.modules.web.common.spi.ImportantFilesImplementation, LookupListener, ChangeListener {

    private final PhpProject phpProject;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    @NullAllowed
    private Lookup.Result<ImportantFilesImplementation> lookupResult;
    // @GuardedBy("this")
    @NullAllowed
    private List<ImportantFilesImplementation> allInstances;


    public ImportantFilesImpl(PhpProject phpProject) {
        assert phpProject != null;
        this.phpProject = phpProject;
    }

    @Override
    public Collection<FileInfo> getFiles() {
        Collection<? extends ImportantFilesImplementation> allFiles = getAllInstances();
        if (allFiles.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<FileInfo> files = new ArrayList<>();
        for (ImportantFilesImplementation instance : allFiles) {
            files.addAll(map(instance.getFiles()));
        }
        return files;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        removeImportantFilesListener();
        resetAllInstances();
        getAllInstances();
        fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    private synchronized Lookup.Result<ImportantFilesImplementation> getLookupResult() {
        if (lookupResult == null) {
            lookupResult = phpProject.getLookup().lookupResult(ImportantFilesImplementation.class);
            lookupResult.addLookupListener(this);
        }
        return lookupResult;
    }

    private synchronized List<ImportantFilesImplementation> getAllInstances() {
        if (allInstances == null) {
            allInstances = new ArrayList<>(getLookupResult().allInstances());
            addImportantFilesListener();
        }
        return allInstances;
    }

    private synchronized void resetAllInstances() {
        allInstances = null;
    }

    private synchronized void addImportantFilesListener() {
        if (allInstances == null) {
            return;
        }
        for (ImportantFilesImplementation files : allInstances) {
            files.addChangeListener(this);
        }
    }

    private synchronized void removeImportantFilesListener() {
        if (allInstances == null) {
            return;
        }
        for (ImportantFilesImplementation files : allInstances) {
            files.removeChangeListener(this);
        }
    }

    private Collection<? extends FileInfo> map(Collection<ImportantFilesImplementation.FileInfo> originals) {
        List<FileInfo> converted = new ArrayList<>(originals.size());
        for (ImportantFilesImplementation.FileInfo original : originals) {
            converted.add(new FileInfo(original.getFile(), original.getDisplayName(), original.getDescription()));
        }
        return converted;
    }

}
