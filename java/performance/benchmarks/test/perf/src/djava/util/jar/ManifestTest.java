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
