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
package org.netbeans.tax;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;

public class FactoryTestGenerator {
    public static String[] CLASS_NAMES = new String [] {
        "TreeAttlistDecl",
        "TreeAttribute",
        "TreeCDATASection",
        "TreeCharacterReference",
        "TreeComment",
        "TreeConditionalSection",
        "TreeDTD",
        "TreeDocument",
        "TreeDocumentFragment",
        "TreeDocumentType",
        "TreeElement",
        "TreeElementDecl",
        "TreeEntityDecl",
        "TreeGeneralEntityReference",
        "TreeNotationDecl",
        "TreeParameterEntityReference",
        "TreeProcessingInstruction",
        "TreeText",
        // Abstract classes
/*            "AbstractTreeDTD",
            "AbstractTreeDocument",
            "TreeChild",
            "TreeCharacterData",
            "TreeData",
            "TreeNode",
            "TreeObject",
            "TreeParentNode",
            "TreeNodeDecl",
            "TreeEntityReference",*/
    };
    
    /** Creates a new instance of ElementTestGenerator */
    public FactoryTestGenerator() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        final Class thisCls = FactoryTestGenerator.class;
        //URL url = thisCls.getProtectionDomain().getCodeSource().getLocation();
        URL url = thisCls.getResource(".");
        PrintWriter out = new PrintWriter(new FileWriter(url.getPath() + "/AbstractFactoryTest.java"));
        //System.out.println(STATIC_CODE);
        
        //if (true != false) { System.exit(0); }
        
        
        out.println(CLASS_HEADER);
        
        for (int i = 0; i < CLASS_NAMES.length; i++) {
            String clsName = CLASS_NAMES[i];
            Class cls = Class.forName("org.netbeans.tax." + clsName);
            Constructor[] constructs = cls.getDeclaredConstructors();
            
            for (int j = 0 ; j < constructs.length; j++) {
                Constructor con = constructs[j];
                if (Modifier.isPublic(con.getModifiers()) && con.getParameterTypes().length > 0) {
                    String methods = createMethods(clsName, con);
                    out.println("\n    //--------------------------------------------------------------------------\n");
                    out.println(methods);
                }
            }
        }
        out.println(STATIC_CODE);
        out.println("}");
        out.close();
        
        for (int i = 0; i < CLASS_NAMES.length; i++) {
            String clsName = CLASS_NAMES[i];
            String sname = clsName.substring(4); // Cut "Tree"
            
            System.out.println("\n"
            + "    public void test" + sname + "() throws Exception {\n"
            + "        create" + sname + "(\"\", \"\");\n"
            + "        create" + sname + "(\"\", \"\");\n"
            + "        create" + sname + "(\"\", \"\");\n"
            + "        create" + sname + "Invalid(null);\n"
            + "        create" + sname + "Invalid(null);\n"
            + "    }\n");
        }
    }
    
    private static String createMethods(String clsName, Constructor constructor) {
        String sname = clsName.substring(4); // Cut "Tree"
        Class[] params = constructor.getParameterTypes();
        HashSet set = new HashSet();
        String header = "";
        String atrs = "";
        String res = "";
        
        // create header declaration an attr. list
        for (int i = 0;  i < params.length; i++) {
            String pType = params[i].getName();
            String pName;
            
            if (params[i].isPrimitive()) {
                pName = pType + "_val";
            } else {
                pName = pType.substring(pType.lastIndexOf('.') + 1).toLowerCase();
            }
            
            if (set.contains(pName)) {
                pName += i;
            }
            set.add(pName);
            
            header += pType + " " + pName;
            atrs += pName;
            if (i < params.length - 1) {
                header += ", ";
                atrs += ", ";
            }
        }
        
        // create() method
        res += "\n"
        + "    static Tree" + sname + " create" + sname + "(" + header + ", String view) throws Exception {\n"
        + "        Tree" + sname + " node = new Tree" + sname + "(" + atrs + ");\n"
        + "        \n"
        + "        assertEquals(node, view);\n"
        + "        cloneNodeTest(node, view);\n"
        + "        return node;\n"
        + "    }\n";
        
        Class[] exs = constructor.getExceptionTypes();
        
        // if constructor throw InvalidArgumentException create method createInvalid()
        for (int i = 0; i < exs.length; i++) {
            if (InvalidArgumentException.class.isAssignableFrom(exs[i])) {
                res += "\n"
                + "    static void create" + sname + "Invalid(" + header + ") throws Exception {\n"
                + "        try {\n"
                + "            new Tree" + sname + "(" + atrs + ");\n"
                + "            // Fail if previous line doesn't trhow exception.\n"
                + "            fail(NOT_EXCEPTION + \"from: new Tree" + sname +"(" + atrs + ")\");\n"
                + "        } catch (InvalidArgumentException e) {\n"
                + "            // OK\n"
                + "        }\n"
                + "    }\n";
                break;
            }
        }
        return res;
    }
    
    private static String CLASS_HEADER = ""
    + "package org.netbeans.modules.xml.tax.test.api.element;\n\n"
    
    + "import org.netbeans.tax.*;\n"
    + "import org.netbeans.junit.NbTestCase;\n"
    + "import org.openide.util.Utilities;\n"
    
    + "import org.netbeans.modules.xml.core.test.api.TestUtil;\n\n"
    
    + "abstract class AbstractFactoryTest extends NbTestCase {\n"
    + "    final private static String NOT_EXCEPTION = \"The InvalidArgumetException wasn't throwed \";\n\n"
    
    + "    public AbstractFactoryTest(String testName) {\n"
    + "        super(testName);\n"
    + "    }\n";
    
    
    
    private static String STATIC_CODE = "\n"
    + "    private static void cloneNodeTest(TreeParentNode node, String view) throws Exception {\n"
    + "        TreeParentNode clone = (TreeParentNode) node.clone(true);\n"
    + "        assertNotEquals(clone, node);\n"
    + "        assertEquals(clone, view);\n"
    + "        \n"
    + "        clone = (TreeParentNode) node.clone(false);\n"
    + "        assertNotEquals(clone, node);\n"
    + "        assertEquals(clone, view);\n"
    + "    }\n"
    + "    \n"
    + "    private static void cloneNodeTest(TreeNode node, String view) throws Exception {\n"
    + "        TreeNode clone = (TreeNode) node.clone();\n"
    + "        assertNotEquals(clone, node);\n"
    + "        assertEquals(clone, view);\n"
    + "    }\n"
    + "    \n"
    + "    private static void assertNotEquals(Object orig, Object clone) {\n"
    + "        if (orig == clone) {\n"
    + "            fail(\"Invalid clone.\");\n"
    + "        }\n"
    + "    }\n"
    + "    \n"
    + "    private static void assertEquals(TreeNode node, String view) throws TreeException{\n"
    + "        String str = TestUtil.nodeToString(node).replace(\"\\n\", \"\");\n"
    + "        if (!!! str.equals(view)) {\n"
    + "            fail(\"Invalid node view \\n is       : \\\"\" + str + \"\\\"\\n should be: \\\"\" + view + \"\\\"\");\n"
    + "        }\n"
    + "    }\n";
    
}
