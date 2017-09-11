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

package org.netbeans.modules.gsf.testrunner.ui;

import java.util.List;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
public final class TestsuiteNodeChildren extends ChildFactory<Testcase> {

    private Report report;
    private int filterMask;
    
    /**
     * Creates a new instance of TestsuiteNodeChildren
     */
    public TestsuiteNodeChildren(final Report report, final int filterMask) {
        this.report = report;
        this.filterMask = filterMask;
    }
    
    @Override
    protected boolean createKeys(List<Testcase> toPopulate) {
        if(report != null) {
            for (Testcase testcase : report.getTests()) {
                if (!testcase.getStatus().isMaskApplied(filterMask)){
                    toPopulate.add(testcase);
                }
            }
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Testcase testcase) {
        if (testcase.getStatus().isMaskApplied(filterMask)){
            return null;
        }
        return Manager.getInstance().getNodeFactory().createTestMethodNode(testcase, report.getProject());
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void notifyTestSuiteFinished() {
        refresh(false);
    }
    
    /**
     */
    public void setFilterMask(final int filterMask) {
        int diff = this.filterMask ^ filterMask;
        if (filterMask == this.filterMask) {
            return;
        }
        this.filterMask = filterMask;
                
        if (report != null) {
            for (Testcase testcase : report.getTests()) {
                if (testcase.getStatus().isMaskApplied(diff)){
                   refresh(false);
                   break;
                }
            }
        }
    }

}
