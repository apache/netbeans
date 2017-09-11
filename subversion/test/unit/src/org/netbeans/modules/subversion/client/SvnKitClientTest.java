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

package org.netbeans.modules.subversion.client;

import java.lang.reflect.Method;
import org.netbeans.modules.subversion.client.commands.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.FileStatusCache;

/**
 * intended to be run with 1.8 client
 * @author tomas
 */
public class SvnKitClientTest extends NbTestCase {
    // XXX test cancel

    public SvnKitClientTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static Test suite() throws Exception {
        
        System.setProperty("svnClientAdapterFactory", "svnkit");
        // svnkit uses its own version of javahl types,
        // test needs to run with NB module system to load classes properly
        return NbModuleSuite.emptyConfiguration()
                .addTest(AddTestHidden.class)
                .addTest(BlameTestHidden.class)
                .addTest(CatTestHidden.class)
                .addTest(CheckoutTestHidden.class)
                .addTest(CommitTestHidden.class)
                .addTest(CopyTestHidden.class)
                .addTest(DifferentWorkingDirsTestHidden.class)
                .addTest(ImportTestHidden.class)
                .addTest(InfoTestHidden.class)
                .addTest(ListTestHidden.class)
                .addTest(LogTestHidden.class)
                .addTest(MergeTestHidden.class)
                .addTest(MkdirTestHidden.class)
                .addTest(MoveTestHidden.class)
                .addTest(ParsedStatusTestHidden.class)
                .addTest(PropertyTestHidden.class)
                .addTest(RelocateTestHidden.class)
                .addTest(RemoveTestHidden.class)
                .addTest(ResolvedTestHidden.class)
                .addTest(RevertTestHidden.class)
                .addTest(StatusTestHidden.class)
                .addTest(TreeConflictsTestHidden.class)
                .addTest(SwitchToTestHidden.class)
                .addTest(UpdateTestHidden.class)
                .gui(false)
                .suite();
    }
}
