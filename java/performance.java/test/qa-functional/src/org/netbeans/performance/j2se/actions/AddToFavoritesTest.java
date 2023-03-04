/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
