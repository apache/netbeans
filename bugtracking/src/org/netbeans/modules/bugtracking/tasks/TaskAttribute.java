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
package org.netbeans.modules.bugtracking.tasks;

import java.util.Comparator;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;

/**
 *
 * @author jpeska
 */
public class TaskAttribute implements Comparator<TaskNode>, Comparable<TaskAttribute> {

    private final String id;
    private final String displayName;
    private final Comparator<TaskNode> comparator;
    private int rank;
    private boolean asceding;

    public static final int NO_RANK = Integer.MAX_VALUE;

    public TaskAttribute(String id, String displayName, Comparator<TaskNode> comparator) {
        this.id = id;
        this.displayName = displayName;
        this.comparator = comparator;
        this.rank = NO_RANK;
        this.asceding = true;
    }

    private TaskAttribute(String id, String displayName, Comparator<TaskNode> comparator, int rank, boolean asceding) {
        this.id = id;
        this.displayName = displayName;
        this.comparator = comparator;
        this.rank = rank;
        this.asceding = asceding;
    }

    @Override
    public int compare(TaskNode tn1, TaskNode tn2) {
        int compare = comparator.compare(tn1, tn2);
        return asceding ? compare : -compare;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isAsceding() {
        return asceding;
    }

    public void setAsceding(boolean asceding) {
        this.asceding = asceding;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(TaskAttribute o) {
        return Integer.compare(rank, o.rank);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    TaskAttribute getClone() {
        return new TaskAttribute(id, displayName, comparator, rank, asceding);
    }
}
