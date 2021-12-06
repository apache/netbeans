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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.versionvault.ui.diff;

import org.openide.util.NbBundle;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.VersionsCache;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.modules.versioning.diff.AbstractDiffSetup;

/**
 * Represents on DIFF setup.
 *
 * @author Maros Sandor
 */
public final class Setup extends AbstractDiffSetup {

    /**
     * What was locally changed? The right pane contains local file.
     *
     * <p>Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_LOCAL     = 0;

    /**
     * What was remotely changed? The right pane contains remote file.
     *
     * <p>Remote addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-HEAD
     * </pre>
     */
    public static final int DIFFTYPE_REMOTE    = 1;

    /**
     * What was locally changed comparing to recent head?
     * The Right pane contains local file.
     *
     * <p> Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-HEAD to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_ALL       = 2;
    
    private final File baseFile;
    
    private String    firstRevision;
    private final String    secondRevision;
    private FileInformation info;

    private DiffStreamSource    firstSource;
    private DiffStreamSource    secondSource;

    private DiffController      view;
    private DiffNode            node;

    private String    title;

    public Setup(File baseFile, int type) {
        this.baseFile = baseFile;
        info = Clearcase.getInstance().getFileStatusCache().getInfo(baseFile);
        int status = info.getStatus();
        
        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;

        // the first source
        switch (type) {
            case Setup.DIFFTYPE_LOCAL:           
            case Setup.DIFFTYPE_REMOTE:

                // from-BASE
                if (Setup.match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                    firstRevision = VersionsCache.REVISION_BASE;
                    firstTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else {
                    firstRevision = VersionsCache.REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), firstRevision);
                }

                break;

            case Setup.DIFFTYPE_ALL:
                if (Setup.match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                    firstRevision = null;
                    firstTitle = loc.getString("MSG_DiffPanel_NoBaseRevision");
                } 
                else {
                    firstRevision = VersionsCache.REVISION_HEAD;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), firstRevision);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
        }

        // the second source
        switch (type) {
            case Setup.DIFFTYPE_LOCAL:
            case Setup.DIFFTYPE_ALL:

                // to-LOCAL
                if (Setup.match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                    secondRevision = VersionsCache.REVISION_CURRENT;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else {
                    secondRevision = VersionsCache.REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalModified"), secondRevision);
                }
                break;

            case Setup.DIFFTYPE_REMOTE:

                // to-HEAD
                if (Setup.match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } 
                else {
                    secondRevision = VersionsCache.REVISION_HEAD;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), secondRevision);
                }            
                break;

            default:
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
        }
        
        firstSource = new DiffStreamSource(baseFile, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, secondRevision, secondTitle);
        title = "<html>" + Clearcase.getInstance().getAnnotator().annotateNameHtml(baseFile, info); // NOI18N
    }

    /**
     * Text file setup for arbitrary revisions.
     * @param firstRevision first revision or <code>null</code> for inital.
     * @param secondRevision second revision
     */
    public Setup(File baseFile, String firstRevision, String secondRevision) {
        this.baseFile = baseFile;
        this.firstRevision = firstRevision;
        this.secondRevision = secondRevision;
        firstSource = new DiffStreamSource(baseFile, firstRevision, firstRevision);
        secondSource = new DiffStreamSource(baseFile, secondRevision, secondRevision);
    }

    public File getBaseFile() {
        return baseFile;
    }

    public FileInformation getInfo() {
        return info;
    }

    public void setView(DiffController view) {
        this.view = view;
    }

    public DiffController getView() {
        return view;
    }

    public StreamSource getFirstSource() {
        return firstSource;
    }

    public StreamSource getSecondSource() {
        return secondSource;
    }

    public void setNode(DiffNode node) {
        this.node = node;
    }

    public DiffNode getNode() {
        return node;
    }
    
    @Override
    public String toString() {
        return title;
    }

    /**
     * Loads data over network
     */
    void initSources() throws IOException {
        if (firstSource != null) firstSource.init();
        if (secondSource != null) secondSource.init();
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }
}
