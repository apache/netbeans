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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
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
 *
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

