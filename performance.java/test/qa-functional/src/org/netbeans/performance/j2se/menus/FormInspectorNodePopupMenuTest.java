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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.ValidatePopupMenuOnNodes;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on node in Component Inspector.
 *
 * @author juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class FormInspectorNodePopupMenuTest extends ValidatePopupMenuOnNodes {

    /**
     * Creates a new instance of FormInspectorNodePopupMenu
     *
     * @param testName test name
     */
    public FormInspectorNodePopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of FormInspectorNodePopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public FormInspectorNodePopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(FormInspectorNodePopupMenuTest.class)
                .suite();
    }

    public void testFormNodePopupMenuInspector() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        CommonUtilities.openSmallFormFile();
        NavigatorOperator.invokeNavigator();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void prepare() {
        String path = "[JFrame]";
        dataObjectNode = new Node(new ComponentInspectorOperator().treeComponents(), path);
        super.prepare();
    }

    @Override
    public void close() {
        if (dataObjectNode != null) {
            super.close();
        }
    }
}
