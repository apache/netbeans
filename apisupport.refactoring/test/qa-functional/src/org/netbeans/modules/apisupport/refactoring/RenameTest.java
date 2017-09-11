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

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
 
/**
 *
 * @author  Petr Zajac
 */
public class RenameTest extends NbTestCase {
        
 
    private static String[] resultFiles = {
        "src/testRename/RenamedAction.java",
        "src/testRename/layer.xml",
        "src/testRename/RenamedLoader.java",
        "manifest.mf",
    };
    private static String[] goldenFiles = {
        "RenamedAction.java.pass",
        "layer.xml.pass",
        "RenamedLoader.java.pass",
        "manifest.mf.pass",
    };
    
    private String PATH_PREFIX = "";
    
//    private static TypeClass typeProxy;
//    private static JavaClass jc;

    private PrintStream refPs;

    
    /** Creates a new instance of Signature1Test */
    public RenameTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(RenameTest.class);
        return suite;
    }
    
//    public void testRename() throws FileStateInvalidException, IOException {
//            jc = (JavaClass) TestUtility.findClass("testRename.MyAction");
//            typeProxy = ((JavaModelPackage) jc.refOutermostPackage()).getType();
//
//            jc = (JavaClass) typeProxy.resolve("testRename.MyAction");
//            RenameRefactoring refactoring = new RenameRefactoring(jc);
//            refactoring.setNewName("RenamedAction");
//            refactoring.checkParameters();
//            RefactoringSession result = RefactoringSession.create("rename class");
//            refactoring.prepare(result);
//            result.doRefactoring(true);
//
//            jc = (JavaClass) TestUtility.findClass("testRename.MyDataLoader");
//            typeProxy = ((JavaModelPackage) jc.refOutermostPackage()).getType();
//
//            jc = (JavaClass) typeProxy.resolve("testRename.MyDataLoader");
//             refactoring = new RenameRefactoring(jc);
//            refactoring.setNewName("RenamedLoader");
//            refactoring.checkParameters();
//            result = RefactoringSession.create("rename class"); 
//            refactoring.prepare(result);
//            result.doRefactoring(true);
//
//            // check modified files
//            for (int x = 0; x < resultFiles.length; x++) {
//                String fileName = PATH_PREFIX + resultFiles[x] ;
//                log("assertFile " + fileName);
//                File resF = TestUtility.getFile(getDataDir(),"testRename", fileName);
//                File goldenF = getGoldenFile(goldenFiles[x]);  
//                File f1 = writeToWorkDir(resF,resF.getName() + ".result");
//                File f2 = writeToWorkDir(goldenF,goldenF.getName() + ".pass");
//                assertFile(f1,f2);
//                f1.delete();
//                f2.delete();
//            }
//    } 
//    public void testWhereUsed() throws Exception {
//       
//        File f = new File(getWorkDir(),"whereUsed.ref" );
//        refPs = new PrintStream(new FileOutputStream(f)); 
//        jc = (JavaClass) TestUtility.findClass("testRename.WhereUsedDataLoader");
//        ref("testrename.MyDataLoader");
//        WhereUsedQuery wu= new WhereUsedQuery(jc);
//        wu.setSearchInComments(true);
//        findClass(wu);
//        wu = new WhereUsedQuery(TestUtility.findClass("testRename.WhereUsedAction"));
//        ref("testrename.WhereUsedAction");
//        findClass(wu);
//        refPs.close();
//        assertFile(f,  getGoldenFile("whereUsed.ref"));
//                
//    }
//        
//    
//    public static String getAsString(String file) {
//        String result;
//        try {
//            FileObject testFile = Repository.getDefault().findResource(file);
//            DataObject dob = DataObject.find(testFile);
//            
//            EditorCookie ec = (EditorCookie) dob.getCookie(EditorCookie.class);
//            StyledDocument doc = ec.openDocument();
//            result = doc.getText(0, doc.getLength());
//        } 
//        catch (Exception e) {
//            throw new AssertionFailedErrorException(e);
//        }
//        return result;
//    }
    
//    protected void findClass(WhereUsedQuery wu) {
//        RefactoringSession result = RefactoringSession.create(null);
//        refProblems(wu.prepare(result));
//        refUsages(result);
//        ref("");
//    }

    /**
     * Stores problems into ref file. Problems should be sorted.
     * @return true if problem is not null and one of them is fatal
     */
//    public boolean refProblems(Problem problem) {
//        Problem p=problem;
//        boolean ret=false;
//        if (p != null) {
//            ArrayList list=new ArrayList();
//            while (p != null) {
//                if (p.isFatal()) {
//                    ret=true;
//                    list.add("Problem fatal: "+p.getMessage());
//                } else {
//                    list.add("Problem: "+p.getMessage());
//                }
//                p=p.getNext();
//            }
//            Collections.sort(list);
//            for (int i=0;i < list.size();i++) {
//                ref(list.get(i));
//            }
//        }
//        return ret;
//    } 
//     protected void refUsages(RefactoringSession session) {
//        Collection result = session.getRefactoringElements();
//        ArrayList list=new ArrayList();
//        HashMap map=new HashMap();
//        for (Iterator it=result.iterator();it.hasNext();) {
//            Object o=it.next();
//            if (o instanceof RefactoringElement) {
//                RefactoringElement wue=(RefactoringElement) o;
//                Element el = wue.getJavaElement();
//                if (el != null && el.getResource() != null) {
//                    String s;
//                    s=el.getResource().getName().replace(File.separatorChar,'/');
//                    list=(ArrayList)(map.get(s));
//                    if (list == null) {
//                        list=new ArrayList();
//                        map.put(s, list);
//                    }
//                    list.add(getDisplayText(wue));
//                } else {
//                    log("refUsages without resource");
//                    log(getDisplayText(wue));
//                    map.put(getDisplayText(wue), "");
//                }
//            }
//        }
//        ref("Found "+String.valueOf(result.size())+" occurance(s).");
//        Object[] keys=map.keySet().toArray();
//        Arrays.sort(keys);
//        for (int i=0;i < keys.length;i++) {
//            ref("");
//            if (map.get(keys[i]) instanceof ArrayList) {
//                ref(keys[i]);
//                list=(ArrayList)(map.get(keys[i]));
//                Collections.sort(list);
//                for (int j=0;j < list.size();j++) {
//                    ref("      "+list.get(j));
//                }
//            } else {
//                ref(keys[i]);
//            }
//        }
//        ref("");
//    }
   public void ref(String s) {
       refPs.println(s);
    }
    
    public void ref(Object o) {
        ref(o.toString());
    }
    
    public void ref(File file) throws Exception {
        BufferedReader br=new BufferedReader(new FileReader(file));
        String line;
        while ((line=br.readLine()) != null) {
            ref(line);
        }
        br.close();
    } 
//     protected String getDisplayText(RefactoringElement elm) {
//        String app="";
//        if (elm.getStatus() == RefactoringElement.WARNING) {
//            app=" [ warning! ]";
//        } else if (elm.getStatus() == RefactoringElement.GUARDED) {
//            app=" [ error: code is in guarded block ]";
//        }
//        return elm.getDisplayText()+app;
//     }   

    private File writeToWorkDir(File resF, String name) throws IOException {
       byte buff[] =  new byte[(int)resF.length()]; 
       FileInputStream fis = new FileInputStream(resF);
       File retF = new File(getWorkDir(),name);
       FileOutputStream fos = new FileOutputStream(retF);
       try {
           fis.read(buff);
           fos.write(buff);
       } finally {
           fis.close();
           fos.close();
       }
       return retF; 
    }  
}
