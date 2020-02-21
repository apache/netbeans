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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 *
 *
 */
public class UnnamedEnumTestCase extends HyperlinkBaseTestCase {
    
    public UnnamedEnumTestCase(String testName) {
        super(testName);
        //System.setProperty("cnd.modelimpl.trace.registration", "true");
    }

    public void testFOUR() throws Exception {
        performTest("newfile.cc", 3, 15, "newfile.cc", 3, 14);
        performTest("newfile.cc", 7, 14, "newfile.cc", 3, 14);
    }
    
    public void testONE() throws Exception {
        performTest("newfile.cc", 6, 14, "newfile.h", 1, 7);
    }

    public void testExecutionContextT() throws Exception {
        performTest("unnamedTypedefEnum.cc", 6, 20, "unnamedTypedefEnum.cc", 6, 17); // k_eExecutionContextSystemTask
        performTest("unnamedTypedefEnum.cc", 7, 20, "unnamedTypedefEnum.cc", 7, 17); // k_eExecutionContextMPTask
        performTest("unnamedTypedefEnum.cc", 13, 60, "unnamedTypedefEnum.cc", 6, 17); // k_eExecutionContextSystemTask
        performTest("unnamedTypedefEnum.cc", 16, 60, "unnamedTypedefEnum.cc", 7, 17); // k_eExecutionContextMPTask
    }
    
    public void testA() throws Exception {
        performTest("unnamedTypedefEnum.cc", 25, 6, "unnamedTypedefEnum.cc", 25, 5); // A1
    }    
}
