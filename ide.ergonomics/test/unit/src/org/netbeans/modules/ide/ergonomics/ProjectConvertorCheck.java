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

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectConvertorCheck extends NbTestCase {

    public ProjectConvertorCheck(String name) {
        super(name);
    }

    public void testGetConvertors() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services/ProjectConvertors");
        assertNotNull("Convertor folder found found", fo);
        assertTrue("Convertor folder is not empty found", fo.getChildren().length > 0);
        StringBuilder sb = new StringBuilder();
        for (FileObject f : fo.getChildren()) {
            Object d = f.getAttribute("raw:delegate");
            if (d instanceof Class) {
                System.setProperty("project.convertor." + f.getNameExt(), ((Class)d).getName());
            } else {
                sb.append("delegate property is not set ").append(f).append("\n");
            }
            Object n = f.getAttribute("requiredPattern");
            if (n instanceof String) {
                System.setProperty("requiredPattern.project.convertor." + f.getNameExt(), (String)n);
            } else {
                sb.append("Missing requiredPattern attribute: ").append(f).append("\n");
            }
        }
    }
    public void testCheckConvertorsPretest() throws Exception {
        testCheckConvertorsReal();
    }
    public void testCheckConvertorsReal() throws Exception {
        StringBuilder errors = new StringBuilder();
        FileObject fo = FileUtil.getConfigFile("Services/ProjectConvertors");
        for (Object o : System.getProperties().keySet()) {
            String f = (String)o;
            if (f.startsWith("project.convertor.")) {
                f = f.substring("project.convertor.".length());
                FileObject l = fo.getFileObject(f);
                if (l == null) {
                    errors.append("Missing convertor " + f + "\n");
                    continue;
                }
                Object n = l.getAttribute("requiredPattern");
                String origN = System.getProperty("requiredPattern.project.convertor." + l.getNameExt());
                if (origN == null || !origN.equals(n)) {
                    errors.append("Wrong name of " + l.getNameExt() + " old: " + origN + " new: " + n + "\n");
                }
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }
}
