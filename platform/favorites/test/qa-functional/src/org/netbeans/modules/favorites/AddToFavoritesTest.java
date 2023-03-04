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
package org.netbeans.modules.favorites;

import java.io.File;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.FavoritesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Tomas.Musil@sun.com
 */
public class AddToFavoritesTest extends JellyTestCase {

    public AddToFavoritesTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(AddToFavoritesTest.class, ".*", ".*");
    }

    @Override
    public void setUp() throws Exception {
        JellyTestCase.closeAllModal();
    }

    public void testAddFolderToFavorites() {
        //Opening a favorites tab (or focusing into it)
        new FavoritesAction().performShortcut();
        FavoritesOperator fo = FavoritesOperator.invoke();
        int initialTreeChildCount = fo.tree().getChildCount(fo.tree().getRoot());
        //Invoking popup, choosing Add to favorites.
        String add2fav = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_AddOnFavoritesNode");
        new ActionNoBlock(null, add2fav).perform(fo);
        //Selecting current working dir, pushing Add
        JFileChooserOperator fco = new JFileChooserOperator();
        File f = new File(System.getProperty("netbeans.user"));
        fco.setSelectedFile(f);
        fco.approve();
        // Test if successful
        Node node = new Node(fo.tree(), f.getName());
        node.expand();
        node.collapse();
        int treeChildCountExpected = initialTreeChildCount + 1;
        assertEquals("items in favorites", treeChildCountExpected, fo.tree().getChildCount(fo.tree().getRoot()));
    }

    public void testAddNonexistingFolder() {
        //Opening a favorites tab (or focusing into it)
        new FavoritesAction().performShortcut();
        FavoritesOperator fo = FavoritesOperator.invoke();
        int initialTreeChildCount = fo.tree().getChildCount(fo.tree().getRoot());
        //Invoking popup, choosing Add to favorites.
        String add2fav = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_AddOnFavoritesNode");
        new ActionNoBlock(null, add2fav).perform(fo);
        // In filechooser, type some non-existing name
        JFileChooserOperator jfco = new JFileChooserOperator();
        jfco.chooseFile("SomeNotExistingFooFile");
        //Expected error dialog arises, close it.
        String dialogName = Bundle.getString("org.netbeans.modules.favorites.Bundle", "ERR_FileDoesNotExistDlgTitle");
        NbDialogOperator dialog = new NbDialogOperator(dialogName);
        dialog.closeByButton();
        assertEquals("items in favorites", initialTreeChildCount, fo.tree().getChildCount(fo.tree().getRoot()));
    }

    // #178009
    // https://netbeans.org/bugzilla/show_bug.cgi?id=178009
//    public void testAddJavaPackageToFavorites(){
//        //Creating sample project General/Java application
//        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
//        String standard = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
//        String javaApp = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SE.xml");
//        npwo.selectCategory(standard);
//        npwo.selectProject(javaApp);
//        npwo.next();
//        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
//        npnlso.txtProjectName().setText(SAMPLE_PROJECT_NAME_F1);
//        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
//        npnlso.finish();
//        ProjectSupport.waitScanFinished();
//        //invoking Projects tab
//        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
//        //Selecting Source package (the one named as project)
//        String srcPack = Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
//        Node node = new Node(pto.tree(), SAMPLE_PROJECT_NAME_F1+"|"+srcPack+"|"+SAMPLE_PROJECT_NAME_F1);
//        //invoking popup, Tools|Add to Favorites
//        String tools = Bundle.getString("org.openide.actions.Bundle", "CTL_Tools");
//        String add = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_Add");
//        node.performPopupAction(tools+"|"+add);
//        FavoritesOperator fo = FavoritesOperator.invoke();
//        assertEquals("items in favorites", 2, fo.tree().getChildCount(fo.tree().getRoot()));
//    }
}
