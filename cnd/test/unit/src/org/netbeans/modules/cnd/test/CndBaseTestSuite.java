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

package org.netbeans.modules.cnd.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * NbTestSuite class => NB JUnit module is absent in target platform 
 * 
 * To solve this problem NB JUnit must be installed 
 * For instance from Netbeans Update Center Beta:
 * - start target(!) platform as IDE from command line (/opt/NBDEV/bin/netbeans)
 * - in opened IDE go into Tools->Update Center
 * - select "Netbeans Update Center Beta" 
 * -- if absent => configure it using the following url as example
 *    http://www.netbeans.org/updates/beta/55_{$netbeans.autoupdate.version}_{$netbeans.autoupdate.regnum}.xml?{$netbeans.hash.code}
 * - press Next
 * - in Libraries subfoler found NB JUnit module
 * - Add it and install
 * - close target IDE and reload development IDE to update the information of 
 *         available modules in target's platform
 */

/**
 * base class to isolate using of NbJUnit library
 */
public class CndBaseTestSuite extends NativeExecutionBaseTestSuite {
    static {
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
        Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager").setLevel(Level.SEVERE);
        Logger.getLogger("org.openide.filesystems.FileUtil").setLevel(Level.OFF);

//        System.setProperty("cnd.pp.condition.comparision.trace", "true");
//        System.setProperty("cnd.modelimpl.trace.file", "gmodule-dl.c");
    }
    /**
     * Constructs an empty TestSuite.
     */
    public CndBaseTestSuite() {
        super();
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     *
     */
    public CndBaseTestSuite(Class<? extends CndBaseTestCase> theClass) {
        super(theClass);
    }

    /**
     * Constructs an empty TestSuite.
     */
    public CndBaseTestSuite(String name) {
        super(name);
    }

    /**
     * Constructs TestSuite that takes platforms (mspecs) from the given section,
     * and performs tests specified by classes parameters for each of them
     * @param name suite name
     * @param mspecSection section of the .cndtestrc that contains platforms as keys
     * @param testClasses test classes
     */
    public CndBaseTestSuite(String name, String mspecSection, Class... testClasses) {
        super(name, mspecSection, testClasses);
    }

}
