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
