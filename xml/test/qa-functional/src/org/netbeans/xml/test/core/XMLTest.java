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
package org.netbeans.xml.test.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author jindra
 */
public class XMLTest extends NbTestCase{
    public static final String ACTIONS_BUNDLE	    = "org.netbeans.modules.xml.core.actions.Bundle";
    public static final String OPTIONS_GENERAL_BUNDLE = "org.netbeans.modules.options.general.Bundle";
    public static final String TOOLS_ACTIONS_BUNDLE = "org.netbeans.modules.xml.tools.actions.Bundle";
    public static final String TOOLS_DOCLET_BUNDLE = "org.netbeans.modules.xml.tools.doclet.Bundle";
    public static final String TOOLS_GENERATOR_BUNDLE	= "org.netbeans.modules.xml.tools.generator.Bundle";
    public static final String CORE_BUNDLE	    = "org.netbeans.modules.xml.core.resources.Bundle";
    public static final String CSS_BUNDLE	    = "org.netbeans.modules.css.resources.Bundle";
    public static final String CSS_ACTIONS_BUNDLE   = "org.netbeans.modules.css.actions.Bundle";
    public static final String XSL_ACTIONS_BUNDLE   = "org.netbeans.modules.xsl.actions.Bundle";
    public static final String XSL_TRANSFORM_BUNDLE = "org.netbeans.modules.xsl.transform.Bundle";
    
    //    public static final String CATALOG_BUNDLE	    = "org.netbeans.modules.xml.catalog.resources.Bundle";
    //    public static final String TAX_BUNDLE	    = "org.netbeans.tax.resources.Bundle";
    //    public static final String TEXT_BUNDLE	    = "org.netbeans.modules.xml.text.resources.Bundle";
    //    public static final String TOOLS_BUNDLE	    = "org.netbeans.modules.xml.tools.resources.Bundle";
    //    public static final String TREE_BUNDLE	    = "org.netbeans.modules.xml.tree.resources.Bundle";
    public static final String WIZARD_BUNDLE	    = "org.netbeans.modules.xml.core.wizard.Bundle";
    public static final String XMLSchema_BUNDLE	    = "org.netbeans.modules.xml.schema.resources.Bundle";
    public static final String UI_BUNDLE	    = "org.netbeans.modules.project.ui.Bundle";
    
    private static boolean generateGoldenFiles = false;
    private static OutputTabOperator outputXML;
    /** Creates a new instance of XMLTest */
    public XMLTest(String testName) {
        super(testName);
    }
    
    protected StyledDocument openFile(String projectName, String fileName)throws Exception{
        File testedFile = new File(getDataDir(), projectName+"/web/"+fileName);
        FileObject fileToTest = FileUtil.toFileObject(testedFile);
        DataObject dataToTest = DataObject.find(fileToTest);
        EditorCookie editorCookie = (EditorCookie) dataToTest.getCookie(EditorCookie.class);
        StyledDocument doc = (StyledDocument)editorCookie.openDocument();
        editorCookie.open();
        return doc;
    }
    
    protected void ending() throws IOException{
        getRef().flush();
        if (!generateGoldenFiles()) compareReferenceFiles();
        else {
            File ref = new File(getWorkDir(),this.getName()+".ref");
            File f = getDataDir();
            ArrayList names = new ArrayList();
            names.add("goldenfiles");
            names.add("data");
            names.add("qa-functional");
            while (!f.getName().equals("test")) f = f.getParentFile();
            for (int i=names.size()-1;i > -1;i--) {
                f=new File(f,(String)(names.get(i)));
            }
            f=new File(f, getClass().getName().replace('.', File.separatorChar));
            f=new File(f, this.getName()+".pass");
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            ref.renameTo(f);
            assertTrue("Generating golden files to " + f.getAbsolutePath(), false);
        }
        
    }
    
    public boolean generateGoldenFiles(){
        return generateGoldenFiles;
    }
    
    protected OutputTabOperator getOutput(){
        if (outputXML == null) outputXML = OutputOperator.invoke().getOutputTab(Bundle.getString(ACTIONS_BUNDLE, "TITLE_XML_check_window"));
        return outputXML;
    }
    
    protected void writeIn() throws InterruptedException{
        Thread.sleep(1000);//wait output window update
        String text = getOutput().getText();
        int index, konec;
        while ((index=text.indexOf("file:"))!=-1){
            index +=4;
            konec = text.indexOf("\n", index);
            text = text.replace(text.substring(index, konec), "name");
        }
        ref(text);
    }
    
    
    public static void initialization(String projectName){
        File datadir = new XMLTest("testName").getDataDir();
        File projectDir = new File(datadir, projectName);
        Project project = (Project) ProjectSupport.openProject(projectDir);
        // not a project
        if (project == null) {
            fail("There is not a project in" + projectDir);
        }
        ProjectSupport.waitScanFinished();
    }
    
    public void tearDown(){
        EditorOperator.closeDiscardAll();
    }
    
}
