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
/*
 * Contributor(s): Thomas Ball
 */

import org.netbeans.modules.classfile.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * ScanJar:  load all of the classes of a specified jar file,
 * useful for performance and regression testing.
 *
 * @author Thomas Ball
 */
public class ScanJar {
    String jarName;
    boolean includeCode;
    boolean toString;

    public static void main(String[] args) {
	boolean includeCode = false;
	boolean toString = false;
        if (args.length == 0)
            usage();
        for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-includeCode"))
		includeCode = true;
	    else if (args[i].equals("-toString"))
		toString = true;
            else if (args[i].charAt(0) == '-')
                usage();
            else {
                try {
                    ScanJar sj = new ScanJar(args[i], includeCode, toString);
		    System.out.print("scanning " + args[i]);
		    if (includeCode || toString) {
			System.out.print(": ");
			if (includeCode)
			    System.out.print("includeCode ");
			if (toString)
			    System.out.print("toString");
		    }
		    System.out.println();
		    ElapsedTimer timer = new ElapsedTimer();
		    int n = sj.scan();
		    System.out.println("scanned " + n + " files in " + 
				       timer.toString());
                } catch (IOException e) {
                    System.err.println("error accessing \"" + args[i] + 
                                       "\": " + e.toString());
                }
            }
        }
    }

    ScanJar(String name, boolean incl, boolean tos) {
	jarName = name;
	includeCode = incl;
	toString = tos;
    }

    /**
     * Reads  class entries from the jar file into ClassFile instances.
     * Returns the number of classes scanned.
     */
    public int scan() throws IOException {
	int n = 0;
	ZipFile zf = new ZipFile(jarName);
	Enumeration files = zf.entries();
	while (files.hasMoreElements()) {
	    ZipEntry entry = (ZipEntry)files.nextElement();
	    String name = entry.getName();
	    if (name.endsWith(".class")) {
                InputStream in = zf.getInputStream(entry);
                ClassFile cf = new ClassFile(in, includeCode);
                if (toString)
                    try {
                        cf.toString(); // forces loading of attributes.
                    } catch (InvalidClassFileAttributeException ex) {
                        System.out.println("error accessing " + name);
                        throw ex;
                    }
                in.close();
		n++;
	    }
	}
	zf.close();
	return n;
    }

    public static void usage() {
        System.err.println("usage:  java ScanJar [-includeCode] " + 
			   "[-toString] <jar file> [ <jar file> ...]");
        System.exit(1);
    }
}

