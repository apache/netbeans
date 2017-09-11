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

package org.netbeans.modules.bugzilla;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.query.QueryNotifyListener;

/**
 *
 * @author tomas
 */
public class TestQueryNotifyListener implements QueryNotifyListener {
    public boolean started = false;
    public boolean finished = false;
    public List<BugzillaIssue> issues = new ArrayList<BugzillaIssue>();
    private BugzillaQuery q;
    public TestQueryNotifyListener(BugzillaQuery q) {
        this.q = q;
        q.addNotifyListener(this);
    }
    public void started() {
        started = true;
    }
    public void notifyDataAdded (BugzillaIssue issue) {
        issues.add(issue);
    }
    public void notifyDataRemoved (BugzillaIssue issue) {
        issues.remove(issue);
    }
    public void finished() {
        finished = true;
    }
    public void reset() {
        started = false;
        finished = false;
        issues = new ArrayList<BugzillaIssue>();
    }
    public List<BugzillaIssue> getIssues(EnumSet<IssueStatusProvider.Status> includeStatus) {
        List<BugzillaIssue> ret = new ArrayList<BugzillaIssue>();
        for (BugzillaIssue issue : issues) {
            if (q == null || includeStatus.contains(q.getIssueStatus(issue.getID()))) {
                ret.add(issue);
            }
        }
        return ret;
    }
}
