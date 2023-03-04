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

package org.netbeans.modules.apisupport.project;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;

/**
 * Represents one module dependency. I.e. <em>&lt;dependency&gt;</em> element
 * in module's <em>project.xml</em> or one token in the
 * OpenIDE-Module-Module-Dependencies attribute of module manifest.
 * <p>
 * Natural ordering is based sequentially on the code name base of {@link
 * ModuleEntry} this instance represents, release version, specification
 * version, implementation dependency and compilation dependency. Two instances
 * are equals only if all the mentioned are <code>equals</code>.
 * </p>
 *
 * @author Martin Krauskopf
 */
public final class ModuleDependency implements Comparable<ModuleDependency> {
    
    // XXX refactor and use SpecificationVersion instead
    private String releaseVersion;
    private String specVersion;
    /**
     * Defer loading spec version until really needed since it can be expensive.
     */
    private static final String SPEC_VERSION_LAZY = "<lazy>"; // NOI18N
    private boolean implDep;
    private boolean compileDep;
    boolean buildPrerequisite;
    boolean runDependency;
    
    private ModuleEntry me;
    
    private Set<String> filterTokensNotFriend;
    private Set<String> filterTokensFriend;
    
    public static final Comparator<ModuleDependency> LOCALIZED_NAME_COMPARATOR;
    public static final Comparator<ModuleDependency> CNB_COMPARATOR;
    
    static {
        LOCALIZED_NAME_COMPARATOR = new Comparator<ModuleDependency>() {
            public int compare(ModuleDependency d1, ModuleDependency d2) {
                ModuleEntry me1 = d1.getModuleEntry();
                ModuleEntry me2 = d2.getModuleEntry();
                int result = Collator.getInstance().compare(
                        me1.getLocalizedName(), me2.getLocalizedName());
                return result != 0 ? result :
                    me1.getCodeNameBase().compareTo(me2.getCodeNameBase());
            }
        };
        CNB_COMPARATOR = new Comparator<ModuleDependency>() {
            public int compare(ModuleDependency d1, ModuleDependency d2) {
                return d1.getCodeNameBase().compareTo(d2.getCodeNameBase());
            }
        };
    }
    
    /**
     * Creates a new instance based on the given entry. The instance will be
     * initialized with given entry's release and specification versions.
     * Compile dependency is set to true by default, implementation version to
     * false.
     */
    public ModuleDependency(ModuleEntry me) {
        this(me, me.getReleaseVersion(), SPEC_VERSION_LAZY, me.getPublicPackages().length > 0, false);
    }
    
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public ModuleDependency(ModuleEntry me, String releaseVersion,
            String specVersion, boolean compileDep, boolean implDep) throws NumberFormatException {
        this.me = me;
        
        // set versions to null if contain the same value as the given entry
        this.compileDep = compileDep;
        this.implDep = implDep;
        this.releaseVersion = releaseVersion;
        if (specVersion != null && !specVersion.equals(SPEC_VERSION_LAZY)) {
            new SpecificationVersion(specVersion);
        }
        this.specVersion = specVersion;
        // defaults, ProjectXMLManager can override with specific info:
        buildPrerequisite = compileDep;
        runDependency = true;
    }
    
    /**
     * Get the <b>major release version</b>.
     * @return <code>null</code> for none or the version.
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    public String getSpecificationVersion() {
        if (SPEC_VERSION_LAZY.equals(specVersion)) {
            specVersion = me.getSpecificationVersion();
        }
        return specVersion;
    }
    
    public ModuleEntry getModuleEntry() {
        return me;
    }
    
    private String getCodeNameBase() {
        return getModuleEntry().getCodeNameBase();
    }
    
    public int compareTo(ModuleDependency other) {
        int result = getCodeNameBase().compareTo(other.getCodeNameBase());
        if (result != 0) { return result; }
        
        // XXX this is not exact since we should use SpecificationVersion
        // instead of String. In this way are not using Dewey-decimal comparison.
        String relVersion = other.getReleaseVersion();
        result = getReleaseVersion() == null // release versions may be null
                ? (relVersion == null ? 0 : -1)
                : (relVersion == null ? 1 : getReleaseVersion().compareTo(relVersion));
        if (result != 0) { return result; }
        
        // do not force resolution of SPEC_VERSION_LAZY for comparisons, unnecessary
        if (specVersion != SPEC_VERSION_LAZY || other.specVersion != SPEC_VERSION_LAZY) {
            String otherSpec = other.getSpecificationVersion();
            String spec = getSpecificationVersion();
            result = spec == null // spec versions may be null
                    ? (otherSpec == null ? 0 : -1)
                    : (otherSpec == null ? 1 : spec.compareTo(otherSpec));
            if (result != 0) { return result; }
        }
        
        result = hasImplementationDependency() == other.hasImplementationDependency() ? 0 : (implDep ? 1 : -1);
        if (result != 0) { return result; }
        
        result = hasCompileDependency() == other.hasCompileDependency() ? 0 : (compileDep ? 1 : -1);
        return result;
    }
    
    public boolean equals(Object o) {
        if (o instanceof ModuleDependency) {
            ModuleDependency other = (ModuleDependency) o;
            return getCodeNameBase().equals(other.getCodeNameBase()) &&
                    Utilities.compareObjects(getReleaseVersion(), other.getReleaseVersion()) &&
                    ((specVersion == SPEC_VERSION_LAZY && other.specVersion == SPEC_VERSION_LAZY) ||
                    Utilities.compareObjects(getSpecificationVersion(), other.getSpecificationVersion())) &&
                    (hasImplementationDependency() == other.hasImplementationDependency()) &&
                    (hasCompileDependency() == other.hasCompileDependency());
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        // specifically do not hash spec version
        return getCodeNameBase().hashCode();
    }
    
    public boolean hasCompileDependency() {
        return compileDep;
    }
    
    public boolean hasImplementationDependency() {
        return implDep;
    }
    
    /**
     * Return a set of tokens that can be used to search for this dependency.
     * Per UI spec, includes lower-case versions of:
     * <ol>
     * <li>the code name base
     * <li>the localized display name
     * <li> the full path to the module JAR or any Class-Path extension
     * <li> the fully-qualified class name (use . for inner classes) of any class
     * contained in the module JAR or any Class-Path extension which is in an package
     * which would be made available to the depending module when using a specification version dependency
     * </ol>
     * Note that the last item means that this can behave differently according to the depending
     * module (according to whether or not it would be listed as a friend).
     * @param dependingModuleCNB the CNB of the module depending on this one
     */
    public Set<String> getFilterTokens(String dependingModuleCNB) {
        boolean friend = me.isDeclaredAsFriend(dependingModuleCNB);
        Set<String> filterTokens = friend ? filterTokensFriend : filterTokensNotFriend;
        if (filterTokens == null) {
            filterTokens = new HashSet<String>();
            filterTokens.add(me.getCodeNameBase());
            filterTokens.add(me.getLocalizedName());
            filterTokens.add(me.getJarLocation().getAbsolutePath());
            String[] cpext = PropertyUtils.tokenizePath(me.getClassPathExtensions());
            filterTokens.addAll(Arrays.asList(cpext));
            if (friend) {
                for (String clazz : me.getPublicClassNames()) {
                    filterTokens.add(clazz.replace('$', '.'));
                }
            }
            if (friend) {
                filterTokensFriend = filterTokens;
            } else {
                filterTokensNotFriend = filterTokens;
            }
        }
        return filterTokens;
    }
    
    public String toString() {
        return "ModuleDependency[me: " + getModuleEntry() + // NOI18N
                ", relVer: " + getReleaseVersion() + // NOI18N
                ", specVer: " + getSpecificationVersion() + // NOI18N
                ", implDep: " + hasImplementationDependency() + // NOI18N
                ", compDep: " + hasCompileDependency() + // NOI18N
                "]"; // NOI18N
    }
}
