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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 */
public final class TestModelHelper implements TraceModel.ParsingTimeResultListener {
    private static final Object LOCK = new Object();
    private final TraceModel traceModel;
    private CharSequence projectName;
    private NativeProject platformProject;
    private TraceModel.ParsingTimeResultListener delegate;
    /**
     * Creates a new instance of TestModelHelper
     */
    public TestModelHelper(boolean clearCache) {
        this(clearCache, null);
    }

    public TestModelHelper(boolean clearCache, TraceModelFileFilter filter) {
        synchronized (LOCK) {
            traceModel = new TraceModel(clearCache, filter);
        }
        traceModel.addParsingTimeResultListener(this);
    }
    
    public TraceModel getTraceModel() {
        return traceModel;
    }
    
    public void addParsingTimeResultListener(TraceModel.ParsingTimeResultListener delegate) {
        this.delegate = delegate;
    }

    public void initParsedProject(String projectRoot, 
            List<String> sysIncludes, List<String> usrIncludes, List<String> libProjectsPaths) throws Exception {
        synchronized (LOCK) {
            traceModel.setIncludePaths(sysIncludes, usrIncludes, libProjectsPaths);
            traceModel.test(new String[]{projectRoot}, System.out, System.err);
            getProject();
        }
    } 
    
    public void initParsedProject(String projectRoot) throws Exception {
        synchronized (LOCK) {
            traceModel.test(new String[]{projectRoot}, System.out, System.err);
            getProject();
        }
    }     
    
    public ProjectBase getProject(){
        synchronized (LOCK) {
            ProjectBase project = traceModel.getProject();
            if (projectName == null) {
                projectName = project.getName();
                platformProject = (NativeProject) project.getPlatformProject();
            }
            return project;
        }
    }

    public ProjectBase reopenProject() {
        synchronized (LOCK) {
            assert platformProject != null;
            ProjectBase project = traceModel.reopenProject(platformProject);
            return project;
        }
    }

    public CharSequence getProjectName() {
        assert projectName != null;
        return projectName;
    }

    public void resetProject() {
        synchronized (LOCK) {
            traceModel.resetProject();
        }
    }

    public CsmModel getModel(){
        synchronized (LOCK) {
            return traceModel.getModel();
        }
    }
    
    public void shutdown(boolean clearCache) {
        synchronized (LOCK) {
            traceModel.shutdown(clearCache);
            TextIndexStorageManager.shutdown();
        }
    }

    @Override
    public String toString() {
        return "TestModelHelper{" + "projectName=" + projectName + '}';
    }

    @Override
    public void notifyParsingTime(TraceModel.TestResult parsingTime) {
        if (delegate != null) {
            delegate.notifyParsingTime(parsingTime);
        }
    }
}
