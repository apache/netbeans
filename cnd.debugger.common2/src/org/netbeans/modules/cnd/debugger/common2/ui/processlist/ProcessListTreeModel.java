/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessList;

/**
 *
 */
public final class ProcessListTreeModel {

    private final Set<Integer> trackedList;
    private final Entry rootEntry;
    private final ProcessList processList;

    public ProcessListTreeModel(ProcessList processList, String filter) {
        this.processList = processList;
        Collection<Integer> filteredLeafPIDs = processList.getPIDs(filter);
        trackedList = new HashSet<Integer>(filteredLeafPIDs);
        Set<Integer> trackedRoots = new HashSet<Integer>();

        Integer ppid;
        Integer apid;
        for (Integer pid : filteredLeafPIDs) {
            apid = pid;
            while ((ppid = processList.getInfo(apid).getPPID()) > 1) {
                trackedList.add(ppid);
                apid = ppid;
            }

            if (apid > 5) {
                trackedRoots.add(apid);
            }
        }

        rootEntry = new Entry(1);
    }

    public Entry getRootEntry() {
        return rootEntry;
    }

    public class Entry {

        public final Integer pid;
        public final List<Entry> children;

        public Entry(Integer pid) {
            ArrayList<Entry> clist = new ArrayList<Entry>();
            this.pid = pid;
            for (Integer cpid : trackedList) {
                if (processList.getInfo(cpid).getPPID().equals(pid)) {
                    clist.add(new Entry(cpid));
                }
            }
            children = Collections.unmodifiableList(clist);
        }

        public List<Integer> getChildrenPIDs() {
            List<Integer> result = new ArrayList<Integer>();
            for (Entry e : children) {
                result.add(e.pid);
            }
            return result;
        }

        Entry getChild(Integer pid) {
            for (Entry e : children) {
                if (e.pid.equals(pid)) {
                    return e;
                }
            }

            return null;
        }
    }
}
