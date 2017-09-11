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
package org.netbeans.performance.j2se.actions;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Tests Add to Favorites.
 *
 * @author mmirilovic@netbeans.org
 */
public class AddToFavoritesTest extends PerformanceTestCase {

    protected static String ADD_TO_FAVORITES, REMOVE_FROM_FAVORITES;
    private String fileProject, filePackage, fileName;
    private Node addToFavoritesNode;
    private FavoritesOperator favoritesWindow;

    /**
     * Creates a new instance of AddToFavorites
     *
     * @param testName the name of the test
     */
    public AddToFavoritesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of AddToFavorites
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddToFavoritesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(AddToFavoritesTest.class)
                .suite();
    }

    public void testAddJavaFile() {
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        doMeasurement();
    }

    @Override
    public void initialize() {
        ADD_TO_FAVORITES = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_Add"); // Tools|Add to Favorites
        REMOVE_FROM_FAVORITES = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_Remove"); // Remove from Favorites
    }

    @Override
    public ComponentOperator open() {
        addToFavoritesNode.performMenuAction(ADD_TO_FAVORITES);
        favoritesWindow = new FavoritesOperator();
        return favoritesWindow;
    }

    @Override
    public void close() {
        if (favoritesWindow != null) {
            new MaximizeWindowAction().performAPI(favoritesWindow);
            Node n = new Node(favoritesWindow.tree(), fileName);
            n.performPopupAction(REMOVE_FROM_FAVORITES);
            favoritesWindow.close();
        }
    }

    @Override
    public void prepare() {
        addToFavoritesNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
    }
}
