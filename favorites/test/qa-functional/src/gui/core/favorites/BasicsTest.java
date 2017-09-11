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
package gui.core.favorites;

import java.io.File;
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
 * Basic favorites test.
 *
 * @author Tomas.Musil@sun.com
 */
public class BasicsTest extends JellyTestCase {

    private final String SAMPLE_PROJECT_NAME_F1="SampleF1";



    /** Need to be defined because of JUnit */
    public BasicsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(BasicsTest.class, ".*", ".*");
    }

    public @Override void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    public void testHomeFolder(){
        // checking if $HOME is by default present in favorites
        new FavoritesAction().performShortcut();
        FavoritesOperator fo = FavoritesOperator.invoke();
        File home = new File(System.getProperty("user.home"));
        Node nodeHome = new Node(fo.tree(),home.getName());
        nodeHome.expand();
        nodeHome.collapse();
    }

// #178009
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

    public void testAddFolderToFavorites(){
        //Opening a favorites tab (or focusing into it)
        new FavoritesAction().performShortcut();
        FavoritesOperator fo = FavoritesOperator.invoke();
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
        assertEquals("items in favorites", 2, fo.tree().getChildCount(fo.tree().getRoot()));
     }

    public void testAddNonexistingFolder(){
        //Opening a favorites tab (or focusing into it)
        new FavoritesAction().performShortcut();
        FavoritesOperator fo = FavoritesOperator.invoke();
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
        assertEquals("items in favorites", 2, fo.tree().getChildCount(fo.tree().getRoot()));
    }

    public void testRemoveFromFavorites(){
        //Opening a favorites tab (or focusing into it)
        FavoritesOperator fo = FavoritesOperator.invoke();
        // Selecting home node in favorites window.
        File f = new File(System.getProperty("user.home"));
        Node node = new Node(fo.tree(), f.getName());
        // selecting Remove from favorites from popup on homeNode
        String removeFromFav = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle","ACT_Remove");
        node.performPopupAction(removeFromFav);
    }
}
