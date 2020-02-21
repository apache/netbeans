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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 * Gathers parse statistics.
 * The idea is not to place statistics data into FilImpl,
 * since in this case we have to persist it.
 */
public class ParseStatistics {

    private static final ParseStatistics instance = new ParseStatistics();

    private static class Entry {
        public int cnt = 0;
    }

    private Map<CsmUID<CsmProject>, Map<CsmUID<CsmFile>, Entry> > projectMaps = null;

    private boolean enabled = false;

    public static ParseStatistics getInstance() {
        return instance;
    }

    private ParseStatistics() {
        if (TraceFlags.PARSE_STATISTICS) {
            setEnabled(true);
        }
    }

    public final void setEnabled(boolean on) {
        if (on != this.enabled) {
            synchronized(this) {
                this.enabled = on;
                if (on) {
                    projectMaps = new HashMap< >();
                } else {
                    projectMaps = null;
                }
            }
        }
    }

    public void fileParsed(FileImpl file) {
        if (enabled) {
            synchronized(this) {
                getEntry(file).cnt++;
            }
        }
    }

    public int getParseCount(FileImpl file) {
        if (enabled) {
            synchronized(this) {
                return getEntry(file).cnt;
            }
        } else {
            return 0;
        }
    }

    private Entry getEntry(FileImpl file) {
        Map<CsmUID<CsmFile>, Entry> map = getProjectMap(file.getProjectUID());
        Entry entry = map.get(file.getUID());
        if (entry == null) {
            entry = new Entry();
            map.put(file.getUID(), entry);
        }
        return entry;
    }

    private Map<CsmUID<CsmFile>, Entry> getProjectMap(CsmUID<CsmProject> projectUID) {
        Map<CsmUID<CsmFile>, Entry> map = projectMaps.get(projectUID);
        if (map == null) {
            map = new HashMap<>();
            projectMaps.put(projectUID, map);
        }
        return map;
    }

    public void clear() {
        if (enabled) {
            synchronized(this) {
                projectMaps.clear();
            }
        }
    }

    public void clear(CsmProject project) {
        if (enabled) {
            synchronized(this) {
                projectMaps.remove(UIDs.get(project));
            }
        }
    }

    public void printResults(CsmProject project) {
        printResults(project, new PrintWriter(System.out));
    }

    public void printResults(CsmProject project, PrintWriter out) {
        if (enabled) {
            synchronized(this) {
                printResults(UIDs.get(project), out);
            }
        } else {
            out.printf("Statistics is disabled"); //NOI18N
        }
        out.flush();
    }

    public void printResults() {
        printResults(new PrintWriter(System.out));
    }

    public void printResults(PrintWriter out) {
        out.printf("%nPARSING STATISTICS%n"); //NOI18N
        if (enabled) {
            synchronized(this) {
                for (CsmUID<CsmProject> projectUID : projectMaps.keySet()) {
                    printResults(projectUID, out);
                }
            }
        } else {
            out.printf("Statistics is disabled"); //NOI18N
        }
        out.flush();
    }

    private void printResults(CsmUID<CsmProject> projectUID, PrintWriter out) {
        List<Map.Entry<CsmUID<CsmFile>, Entry>> entries = new ArrayList<>(getProjectMap(projectUID).entrySet());
        if (entries.isEmpty()) {
            return;
        }
        out.printf("%nPARSING STATISTICS FOR %s%n", UIDUtilities.getProjectName(projectUID)); //NOI18N
        Collections.sort(entries, new Comparator<Map.Entry<CsmUID<CsmFile>, Entry>>() {
            @Override
            public int compare(Map.Entry<CsmUID<CsmFile>, Entry> e1, Map.Entry<CsmUID<CsmFile>, Entry> e2) {
                return e1.getValue().cnt - e2.getValue().cnt;
            }
        });
        int sum = 0;
        for (Map.Entry<CsmUID<CsmFile>, Entry> entry: entries) {
            int cnt = entry.getValue().cnt;
            out.printf("\t%6d %s%n", cnt, UIDUtilities.getFileName(entry.getKey())); //NOI18N
            sum += cnt;
        }
        float avg = entries.isEmpty() ? 0f : ((float)sum / (float)entries.size());
        out.printf("\t%6.1f avg", avg); //NOI18N
        out.printf("%nEND OF PARSING STATISTICS FOR %s%n", UIDUtilities.getProjectName(projectUID)); //NOI18N
    }
}
