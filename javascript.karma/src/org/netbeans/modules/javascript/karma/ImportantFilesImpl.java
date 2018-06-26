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
package org.netbeans.modules.javascript.karma;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public final class ImportantFilesImpl implements ImportantFilesImplementation, PreferenceChangeListener {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private ImportantFilesImpl(Project project) {
        assert project != null;
        this.project = project;
    }

    @ProjectServiceProvider(service = ImportantFilesImplementation.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static ImportantFilesImplementation createImportantFiles(Project project) {
        ImportantFilesImpl importantFiles = new ImportantFilesImpl(project);
        KarmaPreferences.addPreferenceChangeListener(project, WeakListeners.create(PreferenceChangeListener.class, importantFiles, KarmaPreferences.class));
        return importantFiles;
    }

    @Override
    public Collection<FileInfo> getFiles() {
        String config = KarmaPreferences.getConfig(project);
        ValidationResult result = new KarmaPreferencesValidator()
                .validateConfig(config)
                .getResult();
        if (!result.isFaultless()) {
            return Collections.emptyList();
        }
        FileObject file = FileUtil.toFileObject(new File(config));
        assert file != null : config;
        return Collections.singleton(new FileInfo(file));
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
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (KarmaPreferences.CONFIG.equals(evt.getKey())) {
            changeSupport.fireChange();
        }
    }

}
