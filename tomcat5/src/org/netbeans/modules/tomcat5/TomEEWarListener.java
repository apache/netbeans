/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tomcat5;

import java.io.File;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Petr Hejl
 */
public class TomEEWarListener implements FileChangeListener {

    private final TomcatProperties tp;

    private final RefreshHook refresh;

    private File currentTomEEJar;

    public TomEEWarListener(TomcatProperties tp, RefreshHook refresh) {
        this.tp = tp;
        this.refresh = refresh;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public void checkAndRefresh() {
        File jar = TomcatFactory.getTomEEWebAppJar(tp.getCatalinaHome(), tp.getCatalinaBase());
        if (this.currentTomEEJar != jar && (this.currentTomEEJar == null || !this.currentTomEEJar.equals(jar))) {
            currentTomEEJar = jar;
            TomcatManager.TomEEVersion version = TomcatFactory.getTomEEVersion(jar);
            TomcatManager.TomEEType type = version == null ? null : TomcatFactory.getTomEEType(jar.getParentFile());
            refresh.refresh(version, type);
        }
    }

    public static interface RefreshHook {
        void refresh(TomcatManager.TomEEVersion version, TomcatManager.TomEEType type);
    }

}
