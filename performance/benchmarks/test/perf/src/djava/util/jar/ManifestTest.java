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

package djava.util.jar;

import java.io.*;
import java.util.jar.*;

import org.netbeans.performance.Benchmark;

// Rough results (P6/1.2GHz):
// Without sections:
//   5 -   75us
//  10 -  107us
//  25 -  216us
// 100 -  754us
// I.e. approx. 8us per main attr asymptotic
// With sections:
//   5 -  126us
//  25 -  450us
// 100 - 1650us
// I.e. approx. 17us per main attr + sect. (w/ one attr)
// or a cost of approx. 9us per sect (w/ one attr)

/**
 * Benchmark measuring manifest parsing speed.
 *
 * @author Jesse Glick
 */
public class ManifestTest extends Benchmark {
    
    public static void main(String[] args) {
        simpleRun(ManifestTest.class);
    }
    
    public ManifestTest(String name) {
        super(name, new Integer[] {new Integer(5), new Integer(10), new Integer(25), new Integer(100), new Integer(-5), new Integer(-25), new Integer(-100)});
    }
    
    protected byte[] mani;
    protected void setUp() throws Exception {
        int magnitude = ((Integer)getArgument()).intValue();
        boolean doSects = (magnitude < 0);
        magnitude = Math.abs(magnitude);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        try {
            PrintWriter pw = new PrintWriter(baos);
            pw.println("Manifest-Version: 1.0");
            for (int i = 0; i < magnitude; i++) {
                pw.println("Attribute-Number-" + i + ": Somewhat-lengthy-value-number-" + i);
            }
            pw.println();
            if (doSects) {
                for (int i = 0; i < magnitude; i++) {
                    pw.println("Name: some/jar/file/section/number" + i);
                    pw.println("Some-Attribute: Some-value");
                    pw.println();
                }
            }
            pw.close();
        } finally {
            baos.close();
        }
        mani = baos.toByteArray();
        /*
        System.out.println("Manifest:");
        System.out.print(new String(mani, "UTF-8"));
         */
    }
    
    public void testManifestParsing() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
        boolean doSects = (magnitude < 0);
        magnitude = Math.abs(magnitude);
        for (int i = 0; i < count; i++) {
            InputStream is = new ByteArrayInputStream(mani);
            try {
                Manifest m = new Manifest(is);
                assertEquals("Somewhat-lengthy-value-number-" + (magnitude - 1),
                             m.getMainAttributes().getValue("attribute-number-" + (magnitude - 1)));
                if (doSects) {
                    Attributes a = m.getAttributes("some/jar/file/section/number" + (magnitude - 1));
                    assertNotNull(a);
                    assertEquals("Some-value", a.getValue("some-attribute"));
                }
            } finally {
                is.close();
            }
        }
    }
    
}
