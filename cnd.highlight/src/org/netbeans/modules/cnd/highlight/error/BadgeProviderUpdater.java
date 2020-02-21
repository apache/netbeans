/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.error;

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 */
public class BadgeProviderUpdater implements CsmModelListener, CsmProgressListener {

    private static final BadgeProviderUpdater instance = new BadgeProviderUpdater();

    public static BadgeProviderUpdater getInstance() {
        return instance;
    }

    /** Creates a new instance of HighlightProvider */
    private BadgeProviderUpdater() {
        CsmListeners.getDefault().addModelListener(this);
        CsmListeners.getDefault().addProgressListener(this);
    }

    public void startup() {
    }

    public void shutdown() {
        CsmListeners.getDefault().removeModelListener(this);
        BadgeProvider.getInstance().removeAllProjects();
    }

    public void close() {
    }

    @Override
    public void projectOpened(CsmProject project) {
    }

    @Override
    public void projectClosed(CsmProject project) {
        BadgeProvider.getInstance().removeProject(project);
    }

    @Override
    public void modelChanged(CsmChangeEvent e) {
        for (Iterator<CsmFile> it = e.getRemovedFiles().iterator(); it.hasNext();) {
            CsmFile file = it.next();
            BadgeProvider.getInstance().onFileRemoved(file);
        }
    }

    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
        BadgeProvider.getInstance().invalidateFile(file);
    }

    @Override
    public void projectLoaded(CsmProject project) {
        BadgeProvider.getInstance().invalidateProject(project);
    }

    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }
}

