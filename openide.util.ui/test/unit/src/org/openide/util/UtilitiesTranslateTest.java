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

package org.openide.util;

import java.lang.ref.*;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;
import java.io.*;
import java.util.Enumeration;
import java.io.Serializable;
import org.openide.util.io.NbObjectOutputStream;
import org.openide.util.io.NbObjectInputStream;

public class UtilitiesTranslateTest extends NbTestCase {

    public UtilitiesTranslateTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        if (args.length == 1) {
            junit.textui.TestRunner.run(new UtilitiesTranslateTest (args[0]));
        } else {
            junit.textui.TestRunner.run(new NbTestSuite(UtilitiesTranslateTest.class));
        }
    }

    /** Setups the info.
     */
    protected void setUp () {
        System.setProperty ("org.openide.util.Lookup", "-");
        BaseUtilities.initForLoader (new CL ("UtilitiesTranslate.txt"), null);
    }
    
    /** Checks whether the . is treated is normal character.
     */
    public void testDotIsNotEscaped () {
        String dot = "combo";
        
        assertEquals ("Error! Dot is not escaped", Utilities.translate (dot), dot);
    }
    
    /** Tests fail result.
     */
    public void testNoTransformation () {
        String unknown = "unknown.pkg.UnknownName";
        
        String res = Utilities.translate(unknown);
        
        assertTrue ("Should be the same instance", unknown == res);
    }
    
    /** Checks class transformation.
     */
    public void testClassTransformation () {
        String c = "org.netbeans.api.MyClass";
        String res = Utilities.translate (c);

        assertTrue ("Not equal", !res.equals (c));
        assertTrue ("Not the same", res != c);
        assertEquals ("Result ok", "org.nb.api.MyClass", res);
    }
        
    /** Checks package transformation.
     */ 
    public void testPackageTransformation () {
        String c = "SomeClass";
        String p = "org.openide.util";
        
        String res = Utilities.translate (p + '.' + c);
        
        assertTrue ("Ends with the same name", res.endsWith (c));
        assertTrue ("Begins with package", res.startsWith ("org.nb.util"));
        assertEquals ("Length is good", res.length (), 1 + c.length () + "org.nb.util".length ());
    }
    
    /** Test longer transform takes preceedence over shorter one.
     */
    public void testMoreExplicitTransform () {
        String c = A.class.getName ();
        
        String res = Utilities.translate (c);
        
        assertEquals ("A converts to Ahoj", Ahoj.class.getName (), res);
    }
        
    /** Checks transformation to the class with the same name.
     */
    public void testSameNameTransform () {
        String n = "org.openide.TheSame";
        
        String res = Utilities.translate (n);
        
        assertTrue ("Equal strings", res.equals (n));
        assertTrue ("But not the same", res != n);
    }
    
    /** Test (de)serialization of classes with different name
     */
    public void testDeserialization () throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream ();
        
        NbObjectOutputStream oos = new NbObjectOutputStream (os);
        
        A a = new A ();
        a.value = "Ahoj";
        
        oos.writeObject (a);
        oos.close ();

        ByteArrayInputStream is = new ByteArrayInputStream (os.toByteArray());
        NbObjectInputStream ois = new NbObjectInputStream (is);
        
        Object res = ois.readObject ();
        
        assertEquals ("Must be class Ahoj", res.getClass (), Ahoj.class);
        
        Ahoj ahoj = (Ahoj)res;
        
        assertEquals ("Must contain right values", ahoj.value, a.value);
    }

    /** Test that we are able to read the original format.
     */
    public void testReadOldFormat () {
        BaseUtilities.initForLoader (new CL ("UtilitiesTranslateOrig.txt"), null);
    }
    
    /** Translate whole original format.
     */
    public void testTranslateOldFormat () throws Exception {
        BaseUtilities.initForLoader (new CL ("UtilitiesTranslateOrig.txt"), null);
        
        InputStream is = getClass ().getResourceAsStream ("UtilitiesTranslateOrig.txt");
        BufferedReader r = new BufferedReader (new InputStreamReader (is));
        
        for (;;) {
            String line = r.readLine ();
            if (line == null) {
                break;
            }
            
            int space = line.indexOf (' ');
            assertTrue ("Line does not have spaces" + line, space >= 0);
            
            String o = line.substring (0, space).trim () + ".Ahoj";
            String n = line.substring (space).trim () + ".Ahoj";
            
            String trans = Utilities.translate (o);
            
            assertEquals ("Translatated as expected", n, trans);
        }
    }
    
    /** Test with empty classloader.
     */
    public void testNoConvesions () {
        BaseUtilities.initForLoader (new CL (null), null);
        
        Utilities.translate ("something.strange");
        Utilities.translate ("anything");
    }
    
    public void testEmptyFile () {
        BaseUtilities.initForLoader (new CL ("UtilitiesTranslateEmpty.txt"), null);
        
        Utilities.translate ("something.strange");
        Utilities.translate ("anything");
    }
        
    /** Test to fix bug 29878
     */
    public void testBug29878 () {
        BaseUtilities.initForLoader (new CL ("UtilitiesTranslate29878.txt"), null);
        Utilities.translate ("org.netbeans.modules.apisupport.APIModule");
    }
    
    /** Fake classloader that returns provides file from getResources method.
     */
    private static final class CL extends ClassLoader {
        private String file;
        
        public CL (String file) {
            super (java.lang.Object.class.getClassLoader());
            this.file = file;
        }
     
        
        protected Enumeration findResources (String res) throws IOException {
            if (file != null) {
                return Enumerations.singleton (getClass ().getResource (file));
            } else {
                return Enumerations.empty ();
            }
        }
        
    } // end of CL
    
    
    /** A test to serialize and deserialize different class with same fields.
     */
    private static final class A implements Serializable {
        final static long serialVersionUID = 1;
        public String value;
    }
    
    private static final class Ahoj implements Serializable {
        final static long serialVersionUID = 1;
        public String value;
    }
    
    /** Useful code to write down current content of a registry
     *
     * /
    public static void main(String[] args) throws Exception {
//        String  r = org.openide.util.Utilities.translate ("org.netbeans.modules.apisupport.APIModule");
//        System.out.println("result: " + r);
        
        
        Enumeration en;
        ClassLoader c = org.openide.TopManager.getDefault().currentClassLoader();
        en = c.getResources("META-INF/netbeans/translate.names");
        
        
        PrintStream out = new PrintStream (new FileOutputStream ("/tmp/allnames.txt"));
        while (en.hasMoreElements()) {
            URL url = (URL)en.nextElement ();
            
            out.print ("# ");
            out.println (url.toString());
            out.println ();
            
            org.openide.filesystems.FileUtil.copy(url.openStream(), out);
            
            out.println ();
            out.println ();
        }
        
        out.close ();
    }
    */
    
}
