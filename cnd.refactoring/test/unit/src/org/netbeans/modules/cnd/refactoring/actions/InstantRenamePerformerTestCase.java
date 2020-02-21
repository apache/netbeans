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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.refactoring.actions;

import java.io.File;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.test.RefactoringBaseTestCase;

/**
 *
 */
public class InstantRenamePerformerTestCase extends RefactoringBaseTestCase {

    public InstantRenamePerformerTestCase(String testName) {
        super(testName);
    }

    @Override 
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

    public void testAllow() throws Exception {
        performInstantRenameAvailable("quote.cc", 53, 25, true); // customers in list<Customer> customers;
        performInstantRenameAvailable("quote.cc", 55, 20, true); // void outCustomersList() {
        performInstantRenameAvailable("quote.cc", 56, 40, true); // customers in customers.size()
        performInstantRenameAvailable("quote.cc", 59, 39, true); // it in for
        performInstantRenameAvailable("quote.cc", 59, 63, true); // it in for
        performInstantRenameAvailable("quote.cc", 59, 88, true); // it in for
        performInstantRenameAvailable("quote.cc", 60, 24, true); // it in for body
    }
    
    public void testNotAllow() throws Exception {
        performInstantRenameAvailable("quote.cc", 60, 15, false); // cout
        performInstantRenameAvailable("quote.cc", 60, 32, false); // endl
        performInstantRenameAvailable("quote.cc", 70, 32, false); // Customer
    }
    
    public void performInstantRenameAvailable(String source, int line, int column, boolean goldenResult) throws Exception {
        CsmReference ref = super.getReference(source, line, column);
        assertNotNull(ref);

        boolean result = InstantRenamePerformer.allowInstantRename(ref);
        assertEquals(goldenResult, result);
    }
}
