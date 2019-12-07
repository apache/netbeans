/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        ArrayList<File> dirs = new ArrayList<>();
        File sub[];
        dirs.add(new File(args[0]));
        TreeMap<String, TreeSet<String>> bundles = new TreeMap<>();
        while (dirs.size() > 0) {
            sub = dirs.remove(0).listFiles();
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
                        String bundle = m.group(2);
                        String key = m.group(3);
                        if (bundles.containsKey(bundle)) {
                            bundles.get(bundle).add(key);
                        } else {
                            TreeSet<String> keys = new TreeSet<>();
                            keys.add(key);
                            bundles.put(bundle, keys);
                        }
                    }
                }
            }
        }
        int bs = 0, ks = 0;
        PrintStream out = new PrintStream(new FileOutputStream(args[1]));
        for (String bundle : bundles.keySet()) {
            bs++;
            Iterator<String> ki = bundles.get(bundle).iterator();
            out.print(bundle + "=");
            out.print(ki.next());
            ks++;
            while (ki.hasNext()) {
                out.print("," + ki.next());
                ks++;
            }
            out.println();
        }
        out.close();
        System.out.println("Finished " + String.valueOf(ks) + " keys from " + String.valueOf(bs) + " bundles.");
    }
}
