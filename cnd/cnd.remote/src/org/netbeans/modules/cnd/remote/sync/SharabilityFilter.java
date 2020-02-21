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
