/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
