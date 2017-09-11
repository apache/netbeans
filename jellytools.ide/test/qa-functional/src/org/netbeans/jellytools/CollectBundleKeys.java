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
package org.netbeans.jellytools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.*;

/**
 *
 * @author Adam Sotona
 */
public class CollectBundleKeys {

    static final Pattern pat = Pattern.compile("Bundle.getString(Trimmed)?\\s*\\(\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"", Pattern.MULTILINE);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Missing command-line arguments: <source directory> <output property file> !");
            System.exit(1);
        }
        StringBuffer sb = new StringBuffer();
        byte b[] = new byte[1000];
        int i;
        ArrayList dirs = new ArrayList();
        File sub[];
        dirs.add(new File(args[0]));
        TreeMap bundles = new TreeMap();
        String bundle, key;
        TreeSet keys;
        while (dirs.size() > 0) {
            sub = ((File) dirs.remove(0)).listFiles();
            for (int j = 0; sub != null && j < sub.length; j++) {
                if (sub[j].isDirectory()) {
                    dirs.add(sub[j]);
                } else if (sub[j].getName().toLowerCase().endsWith(".java")) {
                    FileInputStream in = new FileInputStream(sub[j]);
                    while ((i = in.read(b)) >= 0) {
                        sb.append(new String(b, 0, i));
                    }
                    in.close();
                    Matcher m = pat.matcher(sb);
                    while (m.find()) {
                        bundle = m.group(2);
                        key = m.group(3);
                        if (bundles.containsKey(bundle)) {
                            ((TreeSet) bundles.get(bundle)).add(key);
                        } else {
                            keys = new TreeSet();
                            keys.add(key);
                            bundles.put(bundle, keys);
                        }
                    }
                }
            }
        }
        Iterator bi = bundles.keySet().iterator();
        Iterator ki;
        int bs = 0, ks = 0;
        PrintStream out = new PrintStream(new FileOutputStream(args[1]));
        while (bi.hasNext()) {
            bs++;
            bundle = (String) bi.next();
            ki = ((TreeSet) bundles.get(bundle)).iterator();
            out.print(bundle + "=");
            out.print((String) ki.next());
            ks++;
            while (ki.hasNext()) {
                out.print("," + (String) ki.next());
                ks++;
            }
            out.println();
        }
        out.close();
        System.out.println("Finished " + String.valueOf(ks) + " keys from " + String.valueOf(bs) + " bundles.");
    }
}
