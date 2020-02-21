/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Level;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FileObjectFilter;
import org.openide.filesystems.FileObject;

/**
 * FileFilter implementation that is based on file sharability
 */
public final class SharabilityFilter implements FileFilter, FileObjectFilter {

    private static final boolean TRACE_SHARABILITY = Boolean.getBoolean("cnd.remote.trace.sharability"); //NOI18N

    @Override
    @SuppressWarnings("deprecation")
    public final boolean accept(File file) {
        // the ProjectSharabilityQuery filters out nbproject/private,
        // but we need it for remote build
        if (file.getName().equals("private")) { // NOI18N
            File parent = file.getParentFile();
            if (parent != null && parent.getName().equals("nbproject")) { // NOI18N
                return true;
            }
        }
        final int sharability = SharabilityQuery.getSharability(file);
        if(TRACE_SHARABILITY) {
            RemoteUtil.LOGGER.log(Level.INFO, "{0} sharability is {1}", new Object[]{file.getAbsolutePath(), sharabilityToString(sharability)});
        }
        switch (sharability) {
            case SharabilityQuery.NOT_SHARABLE:
                return false;
            case SharabilityQuery.MIXED:
            case SharabilityQuery.SHARABLE:
            case SharabilityQuery.UNKNOWN:
                return true;
            default:
                CndUtils.assertTrueInConsole(false, "Unexpected sharability value: " + sharability); //NOI18N
                return true;
        }
    }

    @Override
    public final boolean accept(FileObject file) {
        // the ProjectSharabilityQuery filters out nbproject/private,
        // but we need it for remote build
        if (file.getNameExt().equals("private")) { // NOI18N
            FileObject parent = file.getParent();
            if (parent != null && parent.getNameExt().equals("nbproject")) { // NOI18N
                return true;
            }
        }
        Sharability sharability = SharabilityQuery.getSharability(file);
        if(TRACE_SHARABILITY) {
            RemoteUtil.LOGGER.log(Level.INFO, "{0} sharability is {1}", new Object[]{file.getPath(), sharabilityToString(sharability)});
        }
        switch (sharability) {
            case NOT_SHARABLE:
                return false;
            case MIXED:
            case SHARABLE:
            case UNKNOWN:
                return true;
            default:
                CndUtils.assertTrueInConsole(false, "Unexpected sharability value: " + sharability); //NOI18N
                return true;
        }
    }

    @SuppressWarnings("deprecation")
    private static String sharabilityToString(int sharability) {
        switch (sharability) {
            case SharabilityQuery.NOT_SHARABLE: return "NOT_SHARABLE"; //NOI18N
            case SharabilityQuery.MIXED:        return "MIXED"; //NOI18N
            case SharabilityQuery.SHARABLE:     return "SHARABLE"; //NOI18N
            case SharabilityQuery.UNKNOWN:      return "UNKNOWN"; //NOI18N
            default:                            return "UNEXPECTED: " + sharability; //NOI18N
        }
    }

    private static String sharabilityToString(Sharability sharability) {
        switch (sharability) {
            case NOT_SHARABLE: return "NOT_SHARABLE"; //NOI18N
            case MIXED:        return "MIXED"; //NOI18N
            case SHARABLE:     return "SHARABLE"; //NOI18N
            case UNKNOWN:      return "UNKNOWN"; //NOI18N
            default:                            return "UNEXPECTED: " + sharability; //NOI18N
        }
    }
}
