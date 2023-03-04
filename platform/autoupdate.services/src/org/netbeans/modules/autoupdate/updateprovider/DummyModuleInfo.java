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

package org.netbeans.modules.autoupdate.updateprovider;

import java.util.*;
import java.util.jar.Attributes;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/** A fake module info class initialized from a manifest but not backed by a real JAR.
 * Used for purposes of comparisons to real modules and updates and so on.
 * @author Jesse Glick
 */
public final class DummyModuleInfo extends ModuleInfo {
    
    public static final String TOKEN_MODULE_FORMAT1 = "org.openide.modules.ModuleFormat1"; // NOI18N
    public static final String TOKEN_MODULE_FORMAT2 = "org.openide.modules.ModuleFormat2"; // NOI18N
    
//    private static AutomaticDependencies autoDepsHandler = null;
//    
//    /**
//     * Roughly copied from NbInstaller.refineDependencies.
//     * @see "#29577"
//     */
//    private static synchronized AutomaticDependencies getAutoDepsHandler() {
//        if (autoDepsHandler == null) {
//            FileObject depsFolder = FileUtil.getConfigFile("ModuleAutoDeps"); // NOI18N
//            if (depsFolder != null) {
//                FileObject[] kids = depsFolder.getChildren();
//                List urls = new ArrayList(Math.max(kids.length, 1)); // List<URL>
//                for (int i = 0; i < kids.length; i++) {
//                    if (kids[i].hasExt("xml")) {
//                        urls.add(kids[i].toURL());
//                    }
//                }
//                try {
//                    autoDepsHandler = AutomaticDependencies.parse((URL[])urls.toArray(new URL[urls.size()]));
//                } catch (IOException e) {
//                    Exceptions.printStackTrace(e);
//                } catch (SAXException e) {
//                    Exceptions.printStackTrace(e);
//                }
//            }
//            if (autoDepsHandler == null) {
//                // Parsing failed, or no files.
//                autoDepsHandler = AutomaticDependencies.empty();
//            }
//        }
//        return autoDepsHandler;
//    }
//    
    private final Attributes attr;
    private final Set<Dependency> deps;
    private final String[] provides;
    
    private SpecificationVersion specVersion = null;
    private String codeName = null;
    private String codeNameBase = null;
    private Integer codeNameRelease = null;
    
    /** Create a new fake module based on manifest.
     * Only main attributes need be presented, so
     * only pass these.
     */
    public DummyModuleInfo(Attributes attr) throws IllegalArgumentException {
        this.attr = attr;
        if (attr == null) {
            throw new IllegalArgumentException ("The parameter attr cannot be null.");
        }
        if (getCodeName() == null) {
            throw new IllegalArgumentException ("No code name in module descriptor " + attr.entrySet ());
        }
        String cnb = getCodeNameBase();
        try {
            getSpecificationVersion();
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe.toString() + " from " + cnb); // NOI18N
        }
        deps = parseDeps (attr, cnb);
//        getAutoDepsHandler().refineDependencies(cnb, deps); // #29577
        String providesS = attr.getValue("OpenIDE-Module-Provides"); // NOI18N
        if (cnb.equals ("org.openide.modules")) { // NOI18N
            providesS = providesS == null ? TOKEN_MODULE_FORMAT1 : providesS + ", " + TOKEN_MODULE_FORMAT1; // NOI18N
            providesS = providesS == null ? TOKEN_MODULE_FORMAT2 : providesS + ", " + TOKEN_MODULE_FORMAT2; // NOI18N
        }
        if (providesS == null) {
            provides = new String[0];
        } else {
            StringTokenizer tok = new StringTokenizer(providesS, ", "); // NOI18N
            provides = new String[tok.countTokens()];
            for (int i = 0; i < provides.length; i++) {
                provides[i] = tok.nextToken();
            }
        }
        // XXX could do more error checking but this is probably plenty
    }
    
    public boolean isEnabled() {
        return false;
    }
    
    public SpecificationVersion getSpecificationVersion() {
        if (specVersion == null) {
            String sv = attr.getValue("OpenIDE-Module-Specification-Version"); // NOI18N
            specVersion = sv == null ? null : new SpecificationVersion(sv);
        }
        return specVersion;
    }
    
    public String getCodeName() {
        if (codeName == null) {
            codeName = attr.getValue("OpenIDE-Module"); // NOI18N
        }
        return codeName;
    }
    
    public int getCodeNameRelease() {
        if (codeNameRelease == null) {
            String s = getCodeName();
            int idx = s.lastIndexOf('/'); // NOI18N
            if (idx == -1) {
                codeNameRelease = -1;
            } else {
                codeNameRelease = Integer.parseInt(s.substring(idx + 1));
            }
        }
        return codeNameRelease;
    }
    
    public String getCodeNameBase() {
        if (codeNameBase == null) {
            String s = getCodeName();
            int idx = s.lastIndexOf('/'); // NOI18N
            if (idx == -1) {
                codeNameBase = s;
            } else {
                codeNameBase = s.substring(0, idx);
            }
        }
        return codeNameBase;
    }
    
    public Object getLocalizedAttribute(String a) {
        return attr.getValue(a);
    }
    
    public Object getAttribute(String a) {
        return attr.getValue(a);
    }
    
    /** Get a list of all dependencies this module has.  */
    public Set<Dependency> getDependencies() {
        return deps;
    }
    
    private static final Set<Dependency> parseDeps(Attributes attr, String cnb) throws IllegalArgumentException {
        Set<Dependency> s = new HashSet<Dependency> ();
        s.addAll(Dependency.create(Dependency.TYPE_MODULE, attr.getValue("OpenIDE-Module-Module-Dependencies"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_PACKAGE, attr.getValue("OpenIDE-Module-Package-Dependencies"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_JAVA, attr.getValue("OpenIDE-Module-Java-Dependencies"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_REQUIRES, attr.getValue("OpenIDE-Module-Requires"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_NEEDS, attr.getValue("OpenIDE-Module-Needs"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, attr.getValue("OpenIDE-Module-Recommends"))); // NOI18N
        SpecificationVersion api = null;
        String impl = null;
        String major = null;
        if (api != null) {
            s.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide" + major + " > " + api)); // NOI18N
        }
        if (impl != null) {
            s.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide" + major + " = " + impl)); // NOI18N
        }
        if (api == null && impl == null) {
            // All modules implicitly depend on openide.
            // Needed for #29577.
            //s.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/1 > 0")); // NOI18N
        }
        return s;
    }
    
    public boolean owns(Class clazz) {
        return false;
    }
    
    @Override
    public String[] getProvides() {
        return provides;
    }
}
