/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.testrunner.api;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class CommonUtils {

    private static Reference<CommonUtils> instanceRef;
    public static final String JUNIT_TF = "junit"; // NOI18N
    public static final String TESTNG_TF = "testng"; // NOI18N
    public static final String ANT_PROJECT_TYPE = "ant"; // NOI18N
    public static final String MAVEN_PROJECT_TYPE = "maven"; // NOI18N
    private String testingFramework = ""; // NOI18N

    /**
     * Returns a singleton instance of this class. If no instance exists at the
     * moment, a new instance is created.
     *
     * @return singleton of this class
     */
    public static CommonUtils getInstance() {
        if (instanceRef != null) {
            CommonUtils inst = instanceRef.get();
            if (inst != null) {
                return inst;
            }
        }
        final CommonUtils instance = new CommonUtils();
        instanceRef = new WeakReference<CommonUtils>(instance);
        return instance;
    }

    /**
     *
     * @return the testing framework, e.g. {@link #JUNIT_TF} or {@link #TESTNG_TF}
     */
    public String getTestingFramework() {
        return testingFramework;
    }

    /**
     *
     * @param testingFramework the selected testing framework, e.g. {@link #JUNIT_TF} or {@link #TESTNG_TF}
     */
    public void setTestingFramework(String testingFramework) {
        this.testingFramework = testingFramework;
    }

    /**Get the ActionProvider associated with the project, if any, which "owns" the given file.
     *
     * @param fileObject the selected file
     * @return the ActionProvider associated with the given file, or {@code null}
     */
    public ActionProvider getActionProvider(FileObject fileObject) {
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner == null) { // #183586
            return null;
        }
        return owner.getLookup().lookup(ActionProvider.class);
    }

}
