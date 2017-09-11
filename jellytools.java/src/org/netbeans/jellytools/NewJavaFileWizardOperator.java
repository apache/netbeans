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

package org.netbeans.jellytools;

/**
 * Java-specific extension to NewFileWizardOperator.
 *
 * @author Vojtech.Sigler@sun.com
 */
public class NewJavaFileWizardOperator extends NewFileWizardOperator
{

    /** Creates a new object from template. It invokes new file wizard,
     * sets given project, category and file type. On the next panel it
     * sets package and object name. If package name is null or empty, it lets
     * the default one.
     * @param projectName name of project in which new object should be created
     * @param category category to be selected
     * @param fileType file type to be selected
     * @param packageName package name of new object
     * @param name name of created object
     */
    public static void create(String projectName, String category, String fileType, String packageName, String name) {
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.selectCategory(category);
        nfwo.selectFileType(fileType);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(name);
        if(packageName != null && !"".equals(packageName)) {
            nfnlso.setPackage(packageName);
        }
        nfnlso.finish();
    }

}
