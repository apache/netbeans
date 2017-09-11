/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class StandardModuleData extends ModuleData {
    /**
     * Map from extension JARs to sets of JAR that load them via Class-Path.
     * Used only for debugging purposes, so that a warning is printed if two
     * different modules try to load the same extension (which would cause them
     * to both load their own private copy, which may not be intended).
     */
    private static final Map<File, Set<File>> extensionOwners = new HashMap<File, Set<File>>();
    /** Set of locale-variants JARs for this module (or null).
     * Added explicitly to classloader, and can be used by execution engine.
     */
    private final Set<File> localeVariants;
    /** Set of extension JARs that this module loads via Class-Path (or null).
     * Can be used e.g. by execution engine. (#9617)
     */
    private final Set<File> plainExtensions;
    /** Set of localized extension JARs derived from plainExtensions (or null).
     * Used to add these to the classloader. (#9348)
     * Can be used e.g. by execution engine.
     */
    private final Set<File> localeExtensions;
    
    
    public StandardModuleData(Manifest mf, StandardModule forModule) throws InvalidException {
        super(mf, forModule);
        assert forModule instanceof StandardModule;
        final File jar = forModule.getJarFile();
        assert jar != null : "Cannot load extensions from classpath module " + getCodeNameBase();
        List<File> l = LocaleVariants.findLocaleVariantsOf(jar, getCodeNameBase());
        if (!l.isEmpty()) {
            localeVariants = new HashSet<File>(l);
        } else {
            localeVariants = null;
        }
        Set<File> pe = null;
        Set<File> le = null;
        String classPath = mf.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        if (classPath != null) {
            StringTokenizer tok = new StringTokenizer(classPath);
            while (tok.hasMoreTokens()) {
                String ext = tok.nextToken().replace("%20", " "); // NOI18N
                File extfile;
                if (ext.equals("${java.home}/lib/ext/jfxrt.jar")) { // NOI18N
                    // special handling on JDK7
                    File jre = new File(System.getProperty("java.home")); // NOI18N
                    File jdk8 = new File(new File(new File(jre, "lib"), "ext"), "jfxrt.jar"); // NOI18N
                    if (jdk8.exists()) {
                        // jdk8 has the classes on bootclasspath
                        continue;
                    }
                    extfile = new File(new File(jre, "lib"), "jfxrt.jar"); // NOI18N
                } else {
                    if (new File(ext).isAbsolute()) { // NOI18N
                        Util.err.log(Level.WARNING, "Class-Path value {0} from {1} is illegal according to the Java Extension Mechanism: must be relative", new Object[]{ext, jar});
                    }
                    File base = jar.getParentFile();
                    while (ext.startsWith("../")) {
                        // cannot access FileUtil.normalizeFile from here, and URI.normalize might be unsafe for UNC paths
                        ext = ext.substring(3);
                        base = base.getParentFile();
                    }
                    extfile = new File(base, ext.replace('/', File.separatorChar));
                }
                //No need to sync on extensionOwners - we are in write mutex
                Set<File> owners = extensionOwners.get(extfile);
                if (owners == null) {
                    owners = new HashSet<File>(2);
                    owners.add(jar);
                    extensionOwners.put(extfile, owners);
                } else if (!owners.contains(jar)) {
                    owners.add(jar);
                    forModule.getManager().getEvents().log(Events.EXTENSION_MULTIPLY_LOADED, extfile, owners);
                } // else already know about it (OK or warned)
                // Also check to make sure it is not a module JAR! See constructor for the reverse case.
                if (StandardModule.isModuleJar(extfile)) {
                    Util.err.log(Level.WARNING, 
                        "Class-Path value {0} from {1} illegally refers to another module; use OpenIDE-Module-Module-Dependencies instead", 
                        new Object[]{ext, jar}
                    );
                }
                if (pe == null) {
                    pe = new HashSet<File>();
                }
                pe.add(extfile);
                l = LocaleVariants.findLocaleVariantsOf(extfile, getCodeNameBase());
                if (!l.isEmpty()) {
                    if (le == null) {
                        le = new HashSet<File>();
                    }
                    le.addAll(l);
                }
            }
        }
        localeExtensions = le;
        plainExtensions = pe;
        
        
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.log(Level.FINE, "localeVariants of {0}: {1}", new Object[]{jar, localeVariants});
            Util.err.log(Level.FINE, "plainExtensions of {0}: {1}", new Object[]{jar, plainExtensions});
            Util.err.log(Level.FINE, "localeExtensions of {0}: {1}", new Object[]{jar, localeExtensions});
        }
        // For the chronologically reverse case, see findExtensionsAndVariants().
        Set<File> bogoOwners = extensionOwners.get(jar);
        if (bogoOwners != null) {
            Util.err.log(Level.WARNING, 
                "module {0} was incorrectly placed in the Class-Path of other JARs {1}; please use OpenIDE-Module-Module-Dependencies instead", 
                new Object[]{jar, bogoOwners}
            );
        }
    }

    public StandardModuleData(ObjectInput dis) throws IOException {
        super(dis);
        localeVariants = readFiles(dis);
        localeExtensions = readFiles(dis);
        plainExtensions = readFiles(dis);
    }

    @Override
    void write(ObjectOutput dos) throws IOException {
        super.write(dos);
        writeFiles(dos, localeVariants);
        writeFiles(dos, localeExtensions);
        writeFiles(dos, plainExtensions);
    }
    
    private static Set<File> readFiles(DataInput is) throws IOException {
        int size = is.readInt();
        Set<File> set = new HashSet<File>();
        while (size-- > 0) {
            set.add(new File(Stamps.readRelativePath(is)));
        }
        return set;
    }
    
    private static void writeFiles(DataOutput os, Set<File> files) throws IOException {
        if (files == null) {
            os.writeInt(0);
            return;
        }
        os.writeInt(files.size());
        for (File f : files) {
            Stamps.writeRelativePath(f.getPath(), os);
        }
    }
    
    final void addCp(List<File> classp) {
        // URLClassLoader would not otherwise find these, so:
        if (localeVariants != null) {
            classp.addAll(localeVariants);
        }

        if (localeExtensions != null) {
            classp.addAll(localeExtensions);
        }

        if (plainExtensions != null) {
            classp.addAll(plainExtensions);
        }
        
    }
    
}
