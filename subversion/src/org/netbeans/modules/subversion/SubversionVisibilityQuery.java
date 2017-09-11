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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;

import java.io.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSupport;

/**
 * Hides folders that have 'Localy removed' status.
 * 
 * @author Maros Sandor
 */
public class SubversionVisibilityQuery extends VCSVisibilityQuery implements VersioningListener {

    private FileStatusCache       cache;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.SubversionVisibilityQuery"); //NOI18N

    public SubversionVisibilityQuery() {
    }

    @Override
    public boolean isVisible(File file) {
        long t = System.currentTimeMillis();
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "isVisible {0}", new Object[] { file });
        }
        boolean ret = true;
        try {
    
        if(file == null) return true;
        if (file.isFile()) {
            return true;
        }
        if(!(VersioningSupport.getOwner(file) instanceof SubversionVCS)) {
            return true;
        }
        try {
            // get cached status so you won't block or trigger synchronous shareability calls
            FileInformation info = getCache().getCachedStatus(file);
            return info == null || info.getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY || !containsMetadata(file);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return true;
        }        
        } finally {
            if(Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "isVisible returns {0} in {1} millis", new Object[] { ret, System.currentTimeMillis() - t });
    }
        }
    }

    private synchronized FileStatusCache getCache() {
        if (cache == null) {
            cache = Subversion.getInstance().getStatusCache();
            cache.addVersioningListener(this);
        }
        return cache;
    }
    
    @Override
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            if (file != null && file.isDirectory() && containsMetadata(file)) {
                FileInformation old = (FileInformation) event.getParams()[1];
                FileInformation cur = (FileInformation) event.getParams()[2];
                if (old != null && old.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY || cur.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                    fireVisibilityChanged(file);
                }
            }
        }
    }

    static boolean isHiddenFolder(FileInformation info, File file) {
        return file.isDirectory() && info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;
    }

    /**
     * It makes sense to implement versioning query only for folders with metadata inside.
     * Otherwise they are either new or come from 1.7+ working copies where svn folders
     * are removed from disk immediately.
     */
    private boolean containsMetadata (File folder) {
        String[] children = folder.list(new FilenameFilter() {
            @Override
            public boolean accept (File dir, String name) {
                return SvnUtils.isAdministrative(name);
            }
        });
        return children != null && children.length > 0;
    }

}          
