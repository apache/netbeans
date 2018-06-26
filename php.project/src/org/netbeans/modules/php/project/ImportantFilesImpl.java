/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
