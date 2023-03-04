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


package org.netbeans.test.j2ee.multiview;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.netbeans.modules.j2ee.ddloaders.multiview.EnterpriseBeansNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EntityNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author blaha
 */
public class Utils {
    private NbTestCase nbTestCase;
    public static final String EJB_PROJECT_NAME = "TestCMP";
    
    
    /** Creates a new instance of Utils */
    public Utils(NbTestCase nbTestCase) {
        this.nbTestCase = nbTestCase;
    }
    
    public static void waitForAWTDispatchThread() {
        final boolean[] finished = new boolean[]{false};
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                finished[0] = true;
            }
        });
        while (!finished[0]) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
    }
    
    void save(DataObject dObj) throws IOException{
        SaveCookie saveCookie = (SaveCookie)dObj.getCookie(SaveCookie.class);
        NbTestCase.assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if(saveCookie != null)
            saveCookie.save();
    }
    
    
    
    void checkInXML(EjbJarMultiViewDataObject ddObj, String findText)throws Exception{
        Thread.sleep(3000);
        //check editor in text node
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)ddObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0,document.getLength());
            int index = text.indexOf(findText);
            NbTestCase.assertEquals("Cannot find correct element in XML view (editor document)",true,index>0);
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ",ex);
        }
    }
    
    
    public static EntityNode getEntityNode(EjbJarMultiViewDataObject ddObj){
        ToolBarMultiViewElement toolBar = ddObj.getActiveMVElement();
        SectionNodeView sectionView = (SectionNodeView)toolBar.getSectionView();
        
        Node[] n = getChildrenNodes(sectionView.getRootNode());
        for(int i =0; i < n.length; i++){
            if(n[i] instanceof EnterpriseBeansNode){
                Node[] nChild = getChildrenNodes((EnterpriseBeansNode)n[i]);
                for(int j = 0; j < nChild.length; j++){
                    if(nChild[j] instanceof EntityNode)
                        return (EntityNode)nChild[j];
                }
            }
        }
        return null;
    }
    
    public static Node[] getChildrenNodes(SectionNode parent){
        Children nodes = parent.getChildren();
        return nodes.getNodes();
    }
    
    public void checkFiles(String methodName, String[] ddNames, String[] classNames) throws IOException{
        org.netbeans.test.j2ee.lib.Utils utils = new org.netbeans.test.j2ee.lib.Utils(nbTestCase);
        if(ddNames != null){
            utils.assertFiles(new File(nbTestCase.getDataDir(), "projects/"+EJB_PROJECT_NAME+"/src/conf"), ddNames, methodName+"_");
        }
        if(classNames != null){
            utils.assertFiles(new File(nbTestCase.getDataDir(), "projects/"+EJB_PROJECT_NAME+"/src/java/cmp"), classNames, methodName+"_");
        }
    }
    
}
