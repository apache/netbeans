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
package org.netbeans;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module.PackageExport;
import org.openide.modules.Dependency;
import org.openide.modules.PatchFor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Information about essential properties of a module.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class ModuleData {
    private static final PackageExport[] ZERO_PACKAGE_ARRAY = new PackageExport[0];
    private static final String[] ZERO_STRING_ARRAY = new String[0];

    private final String codeName;
    private final String codeNameBase;
    private final int codeNameRelease;
    private final String implVersion;
    private final String buildVersion;
    private final Set<String> friendNames;
    private final SpecificationVersion specVers;
    private final PackageExport[] publicPackages;
    private final String[] provides;
    private final Dependency[] dependencies;
    private final Set<String> coveredPackages;
    private final String agentClass;
    private final String fragmentHostCodeName;
    
    ModuleData(Manifest mf, Module forModule) throws InvalidException {
        Attributes attr = mf.getMainAttributes();
        // Code name
        codeName = attr.getValue("OpenIDE-Module"); // NOI18N
        if (codeName == null) {
            InvalidException e = new InvalidException("Not a module: no OpenIDE-Module tag in manifest of " + /* #17629: important! */ this, mf); // NOI18N
            // #29393: plausible user mistake, deal with it politely.
            Exceptions.attachLocalizedMessage(e,
                NbBundle.getMessage(Module.class,
                "EXC_not_a_module",
                this.toString()));
            throw e;
        }
        forModule.assignData(this);
        try {
            // This has the side effect of checking syntax:
            if (codeName.indexOf(',') != -1) {
                throw new InvalidException("Illegal code name syntax parsing OpenIDE-Module: " + codeName); // NOI18N
            }
            Object[] cnParse = Util.parseCodeName(codeName);
            codeNameBase = (String) cnParse[0];
            Set<?> deps = forModule.getManager().loadDependencies(codeNameBase);
            boolean verifyCNBs = deps == null;
            if (verifyCNBs) {
                Dependency.create(Dependency.TYPE_MODULE, codeName);
            }
            codeNameRelease = (cnParse[1] != null) ? ((Integer) cnParse[1]).intValue() : -1;
            if (cnParse[2] != null) {
                throw new NumberFormatException(codeName);
            }
            // Spec vers
            String specVersS = attr.getValue("OpenIDE-Module-Specification-Version"); // NOI18N
            if (specVersS != null) {
                try {
                    specVers = new SpecificationVersion(specVersS);
                } catch (NumberFormatException nfe) {
                    throw (InvalidException) new InvalidException("While parsing OpenIDE-Module-Specification-Version: " + nfe.toString()).initCause(nfe); // NOI18N
                }
            } else {
                specVers = null;
            }
            String iv = attr.getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            implVersion = iv == null ? "" : iv;
            String bld = attr.getValue("OpenIDE-Module-Build-Version"); // NOI18N
            buildVersion = bld == null ? implVersion : bld;
            
            this.provides = computeProvides(forModule, attr, verifyCNBs, false);

            // Exports
            String exportsS = attr.getValue("OpenIDE-Module-Public-Packages"); // NOI18N
            if (exportsS != null) {
                if (exportsS.trim().equals("-")) { // NOI18N
                    publicPackages = ZERO_PACKAGE_ARRAY;
                } else {
                    StringTokenizer tok = new StringTokenizer(exportsS, ", "); // NOI18N
                    List<Module.PackageExport> exports = new ArrayList<Module.PackageExport>(Math.max(tok.countTokens(), 1));
                    while (tok.hasMoreTokens()) {
                        String piece = tok.nextToken();
                        if (piece.endsWith(".*")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 2);
                            if (verifyCNBs) {
                                Dependency.create(Dependency.TYPE_MODULE, pkg);
                            }
                            if (pkg.lastIndexOf('/') != -1) {
                                throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                            }
                            exports.add(new Module.PackageExport(pkg.replace('.', '/') + '/', false));
                        } else if (piece.endsWith(".**")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 3);
                            if (verifyCNBs) {
                                Dependency.create(Dependency.TYPE_MODULE, pkg);
                            }
                            if (pkg.lastIndexOf('/') != -1) {
                                throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                            }
                            exports.add(new Module.PackageExport(pkg.replace('.', '/') + '/', true));
                        } else {
                            throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                        }
                    }
                    if (exports.isEmpty()) {
                        throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                    }
                    publicPackages = exports.toArray(new Module.PackageExport[0]);
                }
            } else {
                // XXX new link?
                Util.err.log(Level.WARNING, "module {0} does not declare OpenIDE-Module-Public-Packages "
                    + "in its manifest, so all packages are considered public by default: "
                    + "http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/api.html#how-vers", 
                    codeNameBase
                );
                publicPackages = null;
            }

            {
                HashSet<String> set = null;
                // friends 
                String friends = attr.getValue("OpenIDE-Module-Friends"); // NOI18N
                if (friends != null) {
                    StringTokenizer tok = new StringTokenizer(friends, ", "); // NOI18N
                    set = new HashSet<String>();
                    while (tok.hasMoreTokens()) {
                        String piece = tok.nextToken();
                        if (piece.indexOf('/') != -1) {
                            throw new IllegalArgumentException("May specify only module code name bases in OpenIDE-Module-Friends, not major release versions: " + piece); // NOI18N
                        }
                        if (verifyCNBs) {
                            // Indirect way of checking syntax:
                            Dependency.create(Dependency.TYPE_MODULE, piece);
                        }
                        // OK, add it.
                        set.add(piece);
                    }
                    if (set.isEmpty()) {
                        throw new IllegalArgumentException("Empty OpenIDE-Module-Friends: " + friends); // NOI18N
                    }
                    if (publicPackages == null || publicPackages.length == 0) {
                        throw new IllegalArgumentException("No use specifying OpenIDE-Module-Friends without any public packages: " + friends); // NOI18N
                    }
                }
                this.friendNames = set;
            }
            this.dependencies = initDeps(forModule, deps, attr);
            String classLoader = attr.getValue(PatchFor.MANIFEST_FRAGMENT_HOST); // NOI18N
            if (classLoader != null) {
                Object[] clParse = Util.parseCodeName(classLoader);
                String frag = (String)clParse[0];
                if (frag != null) {
                    if ((frag = frag.trim()).isEmpty()) {
                        frag = null;
                    }
                }
                this.fragmentHostCodeName = frag;
                if (verifyCNBs && frag != null) {
                    // Indirect way of checking syntax:
                    Dependency.create(Dependency.TYPE_MODULE, fragmentHostCodeName);
                }
            } else {
                fragmentHostCodeName = null;
            }
        } catch (IllegalArgumentException iae) {
            throw (InvalidException) new InvalidException("While parsing " + codeName + " a dependency attribute: " + iae.toString()).initCause(iae); // NOI18N
        }
        this.coveredPackages = new HashSet<String>();
        this.agentClass = attr.getValue("Agent-Class");
    }
    
    ModuleData(Manifest mf, NetigsoModule m) throws InvalidException {
        final String symbName = getMainAttribute(mf, "Bundle-SymbolicName"); // NOI18N
        if (symbName == null) {
            throw new InvalidException("Not an OSGi bundle: " + m);
        }
        m.assignData(this);
        this.codeName = symbName.replace('-', '_');
        int slash = codeName.lastIndexOf('/');
        if (slash != -1) {
            this.codeNameRelease = Integer.parseInt(symbName.substring(slash + 1));
        } else {
            this.codeNameRelease = -1;
        }
        String v = getMainAttribute(mf, "Bundle-Version"); // NOI18N
        if (v == null) {
            Logger.getLogger(ModuleData.class.getName()).log(Level.WARNING, "No Bundle-Version for {0}", m);
            this.specVers = new SpecificationVersion(v = "0.0");
        } else {
            this.specVers = computeVersion(v);
        }
        this.codeNameBase = codeName;
        String iv = getMainAttribute(mf, "OpenIDE-Module-Implementation-Version"); // NOI18N
        this.implVersion = iv == null ? v : iv;
        String bld = getMainAttribute(mf, "OpenIDE-Module-Build-Version"); // NOI18N
        this.buildVersion = bld == null ? implVersion : bld;
        this.friendNames = Collections.emptySet();
        this.publicPackages = null;
        this.provides = computeProvides(m, mf.getMainAttributes(), false, true);
        this.dependencies = computeImported(mf.getMainAttributes());
        this.coveredPackages = new HashSet<String>();
        this.agentClass = getMainAttribute(mf, "Agent-Class"); // NOI18N
        this.fragmentHostCodeName = null;
    }
    
    ModuleData(ObjectInput dis) throws IOException {
        try {
            this.codeName = dis.readUTF();
            this.codeNameBase = dis.readUTF();
            this.codeNameRelease = dis.readInt();
            this.coveredPackages = readStrings(dis, new HashSet<String>(), true);
            this.dependencies = (Dependency[]) dis.readObject();
            this.implVersion = dis.readUTF();
            this.buildVersion = dis.readUTF();
            this.provides = readStrings(dis);
            this.friendNames = readStrings(dis, new HashSet<String>(), false);
            this.specVers = new SpecificationVersion(dis.readUTF());
            this.publicPackages = Module.PackageExport.read(dis);
            this.agentClass = dis.readUTF();
            String s = dis.readUTF();
            if (s != null) {
                s = s.trim();
            }
            this.fragmentHostCodeName = s == null || s.isEmpty() ? null : s;
        } catch (ClassNotFoundException cnfe) {
            throw new IOException(cnfe);
        }
    }
    
    void write(ObjectOutput dos) throws IOException {
        dos.writeUTF(codeName);
        dos.writeUTF(codeNameBase);
        dos.writeInt(codeNameRelease);
        writeStrings(dos, coveredPackages);
        dos.writeObject(dependencies);
        dos.writeUTF(implVersion);
        dos.writeUTF(buildVersion);
        writeStrings(dos, provides);
        writeStrings(dos, friendNames);
        dos.writeUTF(specVers != null ? specVers.toString() : "0");
        Module.PackageExport.write(dos, publicPackages);
        dos.writeUTF(agentClass == null ? "" : agentClass);
        dos.writeUTF(fragmentHostCodeName == null ? "" : fragmentHostCodeName);
    }

    private Dependency[] computeImported(Attributes attr) {
        String pkgs = attr.getValue("Import-Package"); // NOI18N
        List<Dependency> arr = null;
        if (pkgs != null) {
            arr = new ArrayList<Dependency>();
            StringTokenizer tok = createTokenizer(pkgs); // NOI18N
            while (tok.hasMoreElements()) {
                String dep = beforeSemicolon(tok);
                arr.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, dep));
            }
        }
        String recomm = attr.getValue("Require-Bundle"); // NOI18N
        if (recomm != null) {
            if (arr == null) {
                arr = new ArrayList<Dependency>();
            }
            StringTokenizer tok = createTokenizer(recomm); // NOI18N
            while (tok.hasMoreElements()) {
                String dep = beforeSemicolon(tok);
                arr.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, "cnb." + dep)); // NOI18N
            }
        }
        return arr == null ? null : arr.toArray(new Dependency[0]);
    }

    private static StringTokenizer createTokenizer(String osgiDep) {
        for (;;) {
            int first = osgiDep.indexOf('"');
            if (first == -1) {
                break;
            }
            int second = osgiDep.indexOf('"', first + 1);
            if (second == -1) {
                break;
            }
            osgiDep = osgiDep.substring(0, first - 1) + osgiDep.substring(second + 1);
        }
        
        return new StringTokenizer(osgiDep, ",");
    }

    private static String beforeSemicolon(StringTokenizer tok) {
        String dep = tok.nextToken().trim();
        int semicolon = dep.indexOf(';');
        if (semicolon >= 0) {
            dep = dep.substring(0, semicolon);
        }
        return dep.replace('-', '_');
    }
    
    private String[] computeExported(boolean useOSGi, Collection<String> arr, Attributes attr) {
        if (!useOSGi) {
            return arr.toArray(ZERO_STRING_ARRAY);
        }
        String pkgs = attr.getValue("Export-Package"); // NOI18N
        if (pkgs == null) {
            return arr.toArray(ZERO_STRING_ARRAY);
        }
        StringTokenizer tok = createTokenizer(pkgs); // NOI18N
        while (tok.hasMoreElements()) {
            arr.add(beforeSemicolon(tok));
        }
        return arr.toArray(ZERO_STRING_ARRAY);
    }
    
    private String[] computeProvides(
        Module forModule, Attributes attr, boolean verifyCNBs, boolean useOSGi
    ) throws InvalidException, IllegalArgumentException {
        Set<String> arr = new LinkedHashSet<String>();
        // Token provides
        String providesS = attr.getValue("OpenIDE-Module-Provides"); // NOI18N
        if (providesS != null) {
            StringTokenizer tok = new StringTokenizer(providesS, ", "); // NOI18N
            int expCount = tok.countTokens();
            while (tok.hasMoreTokens()) {
                String provide = tok.nextToken();
                if (provide.indexOf(',') != -1) {
                    throw new InvalidException("Illegal code name syntax parsing OpenIDE-Module-Provides: " + provide); // NOI18N
                }
                if (verifyCNBs) {
                    Dependency.create(Dependency.TYPE_MODULE, provide);
                }
                if (provide.lastIndexOf('/') != -1) throw new IllegalArgumentException("Illegal OpenIDE-Module-Provides: " + provide); // NOI18N
                arr.add(provide);
            }
            if (arr.size() != expCount) {
                throw new IllegalArgumentException("Duplicate entries in OpenIDE-Module-Provides: " + providesS); // NOI18N
            }
        }
        String[] additionalProvides = forModule.getManager().refineProvides (forModule);
        if (additionalProvides != null) {
            arr.addAll (Arrays.asList (additionalProvides));
        }
        arr.add("cnb." + getCodeNameBase()); // NOI18N
        return computeExported(useOSGi, arr, attr);
    }
    
    /**
     * Initializes dependencies of this module
     *
     * @param knownDeps Set<Dependency> of this module known from different
     * source, can be null
     * @param attr attributes in manifest to parse if knownDeps is null
     */
    private Dependency[] initDeps(Module forModule, Set<?> knownDeps, Attributes attr)
        throws IllegalStateException, IllegalArgumentException {
        if (knownDeps != null) {
            return knownDeps.toArray(new Dependency[0]);
        }

        // deps
        Set<Dependency> deps = new HashSet<Dependency>(20);
        // First convert IDE/1 -> org.openide/1, so we never have to deal with
        // "IDE deps" internally:
        @SuppressWarnings(value = "deprecation")
        Set<Dependency> openideDeps = Dependency.create(Dependency.TYPE_IDE, attr.getValue("OpenIDE-Module-IDE-Dependencies")); // NOI18N
        if (!openideDeps.isEmpty()) {
            // If empty, leave it that way; NbInstaller will add it anyway.
            Dependency d = openideDeps.iterator().next();
            String name = d.getName();
            if (!name.startsWith("IDE/")) {
                throw new IllegalStateException("Weird IDE dep: " + name); // NOI18N
            }
            deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/" + name.substring(4) + " > " + d.getVersion())); // NOI18N
            if (deps.size() != 1) {
                throw new IllegalStateException("Should be singleton: " + deps); // NOI18N
            }
            Util.err.log(Level.WARNING, "the module {0} uses OpenIDE-Module-IDE-Dependencies which is deprecated. See http://openide.netbeans.org/proposals/arch/modularize.html", codeNameBase); // NOI18N
        }
        deps.addAll(Dependency.create(Dependency.TYPE_JAVA, attr.getValue("OpenIDE-Module-Java-Dependencies"))); // NOI18N
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, attr.getValue("OpenIDE-Module-Module-Dependencies"))); // NOI18N
        String pkgdeps = attr.getValue("OpenIDE-Module-Package-Dependencies"); // NOI18N
        if (pkgdeps != null) {
            // XXX: Util.err.log(ErrorManager.WARNING, "Warning: module " + codeNameBase + " uses the OpenIDE-Module-Package-Dependencies 
            // manifest attribute, which is now deprecated: XXX URL TBD");
            deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, pkgdeps)); // NOI18N
        }
        deps.addAll(Dependency.create(Dependency.TYPE_REQUIRES, attr.getValue("OpenIDE-Module-Requires"))); // NOI18N
        deps.addAll(Dependency.create(Dependency.TYPE_NEEDS, attr.getValue("OpenIDE-Module-Needs"))); // NOI18N
        deps.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, attr.getValue("OpenIDE-Module-Recommends"))); // NOI18N
        forModule.refineDependencies(deps);
        return deps.toArray(new Dependency[0]);
    }
    
    final String getFragmentHostCodeName() {
        return fragmentHostCodeName;
    }

    final String getCodeName() {
        return codeName;
    }
    
    final String getCodeNameBase() {
        return codeNameBase;
    }

    final int getCodeNameRelease() {
        return codeNameRelease;
    }

    final String[] getProvides() {
        return provides;
    }

    final SpecificationVersion getSpecificationVersion() {
        return specVers;
    }

    final PackageExport[] getPublicPackages() {
        return publicPackages;
    }

    final Set<String> getFriendNames() {
        return friendNames;
    }

    final Dependency[] getDependencies() {
        return dependencies;
    }

    final String getBuildVersion() {
        return buildVersion.isEmpty() ? null : buildVersion;
    }

    final String getImplementationVersion() {
        return implVersion.isEmpty() ? null : implVersion;
    }
    
    void registerCoveredPackages(Set<String> known) {
        assert coveredPackages.isEmpty();
        coveredPackages.addAll(known);
    }

    Set<String> getCoveredPackages() {
        return coveredPackages.isEmpty() ? null : coveredPackages;
    }

    private <T extends Collection<String>> T readStrings(
        DataInput dis, T set, boolean returnEmpty
    ) throws IOException {
        int cnt = dis.readInt();
        if (!returnEmpty && cnt == 0) {
            return null;
        }
        while (cnt-- > 0) {
            set.add(dis.readUTF());
        }
        return set;
    }
    private String[] readStrings(ObjectInput dis) throws IOException {
        List<String> arr = new ArrayList<String>();
        readStrings(dis, arr, false);
        return arr.toArray(new String[0]);
    }
    private void writeStrings(DataOutput dos, Collection<String> set) 
    throws IOException {
        if (set == null) {
            dos.writeInt(0);
            return;
        }
        dos.writeInt(set.size());
        for (String s : set) {
            dos.writeUTF(s);
        }
    }
    private void writeStrings(ObjectOutput dos, String[] provides) throws IOException {
        writeStrings(dos, Arrays.asList(provides));
    }
    
    private static String getMainAttribute(Manifest manifest, String attr) {
        String s = manifest.getMainAttributes().getValue(attr);
        if (s == null) {
            return null;
        }
        int semicolon = s.indexOf(';');
        if (semicolon == -1) {
            return s;
        } else {
            return s.substring(0, semicolon);
        }
    }
    private static SpecificationVersion computeVersion(String v) {
        int pos = -1;
        for (int i = 0; i < 3; i++) {
            pos = v.indexOf('.', pos + 1);
            if (pos == -1) {
                return new SpecificationVersion(v);
            }
        }
        return new SpecificationVersion(v.substring(0, pos));
    }

    final String getAgentClass() {
        return agentClass == null || agentClass.isEmpty() ? null : agentClass;
    }
}
