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

package org.netbeans.modules.ide.ergonomics;

import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.newproject.EnableStep;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectTemplatesCheck extends NbTestCase {
    public ProjectTemplatesCheck(String n) {
        super(n);
    }
    
    public void testCanGetWizardIteratorsForAllProjects() {
        FileObject root = FileUtil.getConfigFile("Templates/Project");
        assertNotNull("project is available (need to run in NbModuleTest mode)", root);
        Enumeration<? extends FileObject> en = root.getChildren(true);
        StringBuilder sb = new StringBuilder();
        int error = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (Boolean.TRUE.equals(fo.getAttribute("template"))) {
                sb.append(fo);
                Object value = EnableStep.readWizard(fo);

                if (value == null) {
                    error++;
                    sb.append(" - failure\n");
                    Enumeration<String> names = fo.getAttributes();
                    while (names.hasMoreElements()) {
                        String n = names.nextElement();
                        sb.append("  name: " + n + " value: " + fo.getAttribute(n) + "\n");
                    }
                } else {
                    sb.append(" - OK\n");
                }
            }
        }
        if (error > 0) {
            fail(sb.toString());
        }
    }
}
