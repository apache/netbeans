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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;

/**
 * Task just like <jar> but predefines various module attributes.
 * Cf. projectized.xml#jar
 * @author Jesse Glick
 */
public class JarWithModuleAttributes extends Jar {

    public JarWithModuleAttributes() {}

    private static final Pattern COMMA_SPACE = Pattern.compile(", *");
    private static final Pattern IMPL_DEP = Pattern.compile(" *([a-zA-Z0-9_.]+)(/[0-9]+)? *= *(.+) *");
    
    private File stamp;
    /** Location of a stamp file to create and/or make newer than the JAR file.
     * 
     * @param stamp the file to create and update
     */
    public void setStamp(File stamp) {
        this.stamp = stamp;
    }


    static String extractCodeName(Attributes attr) {
        return extractCodeName(attr, null);
    }
    static String extractCodeName(Attributes attr, boolean[] osgi) {
        String codename = attr.getValue("OpenIDE-Module");
        if (codename != null) {
            return codename;
        }
        codename = attr.getValue("Bundle-SymbolicName");
        if (codename == null) {
            return null;
        }
        codename = codename.replace('-', '_');
        if (osgi != null) {
            osgi[0] = true;
        }
        int params = codename.indexOf(';');
        if (params >= 0) {
            return codename.substring(0, params);
        } else {
            return codename;
        }
    }

    @Override
    public void setManifest(File manifestFile) throws BuildException {
        Manifest added = new Manifest();
        try {
            // Check to see if OpenIDE-Module-Implementation-Version is already defined.
            String implVers;
            String specVer;
            String ownCnb;
            Manifest staticManifest;
            InputStream is = new FileInputStream(manifestFile);
            boolean isOSGiMode = false;
            try {
                staticManifest = new Manifest(new InputStreamReader(is, "UTF-8"));
                Manifest.Section mainSection = staticManifest.getMainSection();
                implVers = mainSection.getAttributeValue("OpenIDE-Module-Implementation-Version");
                specVer = mainSection.getAttributeValue("OpenIDE-Module-Specification-Version");
                String myself = mainSection.getAttributeValue("OpenIDE-Module");
                if (myself == null) {
                    myself = mainSection.getAttributeValue("Bundle-SymbolicName");
                    isOSGiMode = myself != null;
                }
                if (myself == null) {
                    throw new BuildException("No OpenIDE-Module in " + manifestFile);
                }
                int slash = myself.indexOf('/');
                if (slash == -1) {
                    ownCnb = myself;
                } else {
                    ownCnb = myself.substring(0, slash);
                }
                String cnbs = getProject().getProperty("code.name.base.slashes");
                String cnbDots = (cnbs != null) ? cnbs.replace('/', '.') : null;
                if (!ownCnb.equals(cnbDots)) {
                    // #58248: make sure these stay in synch.
                    throw new BuildException("Mismatch in module code name base: manifest says " + ownCnb +
                            " but project.xml says " + cnbDots, getLocation());
                }
            } finally {
                is.close();
            }

            if (isOSGiMode) {
                added.addConfiguredAttribute(new Manifest.Attribute("Bundle-ManifestVersion", "2")); // NOI18N
            }

            String pubPkgs = getProject().getProperty("public.packages");
            if (pubPkgs == null) {
                throw new BuildException("Must have defined 'public.packages'", getLocation());
            }
            if (isOSGiMode) {
                if (pubPkgs != null && !pubPkgs.equals("-")) {
                    added.addConfiguredAttribute(new Manifest.Attribute("Export-Package", pubPkgs.replaceAll("\\.\\*", ""))); // NOI18N
                }
            } else {
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Public-Packages", pubPkgs));
            }
            String friends = getProject().getProperty("friends");
            if (friends != null) {
                if (isOSGiMode) {
                    throw new BuildException("friends defined, yet OSGi does not support that " + friends);
                }
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Friends", friends));
            }
            // #52354: define Class-Path in the manifest automatically.
            String javahelpClassPathExtension = getProject().getProperty("javahelp.class.path.extension");
            String classPathExtensions = getProject().getProperty("class.path.extensions");
            String cp;
            if (javahelpClassPathExtension != null) {
                if (classPathExtensions != null) {
                    cp = classPathExtensions + " " + javahelpClassPathExtension;
                } else {
                    cp = javahelpClassPathExtension;
                }
            } else {
                cp = classPathExtensions;
            }
            if (cp != null) {
                added.addConfiguredAttribute(new Manifest.Attribute("Class-Path", cp));
            }
            String moduleDeps = getProject().getProperty("module.dependencies");
            if (moduleDeps != null) {
                if (isOSGiMode) {
                    StringBuilder sb = new StringBuilder();
                    String sep = "";
                    for (String one : moduleDeps.split(",")) {
                        if (one.indexOf('=') != -1) {
                           throw new BuildException("Implementation dependencies not supported in Netigso mode: " + one);
                        }
                        String versionS, codename;
                        int greaterThan = one.indexOf('>');
                        if (greaterThan == -1) {
                            versionS = "0";
                            codename = one.trim();
                        } else {
                            versionS = one.substring(greaterThan + 1).trim();
                            codename = one.substring(0, greaterThan).trim();
                        }
                        if (codename.equals("org.netbeans.libs.osgi")) {
                            // #184434: do not include a dep on a NB pseudomodule.
                            // apisupport's BundleActivator wizard anyway adds to manifest:
                            // Import-Package: org.osgi.framework
                            continue;
                        }
                        int[] version = parseDecimal(versionS, 3);
                        int slash = codename.indexOf('/');
                        int hiMajor;
                        String cnb;
                        if (slash >= 0) {
                            cnb = codename.substring(0, slash).trim();
                            String range = codename.substring(slash + 1).trim();
                            int dash = range.indexOf('-');
                            if (dash == -1) {
                                hiMajor = Integer.parseInt(range);
                                version[0] += 100 * hiMajor;
                            } else {
                                hiMajor = Integer.parseInt(range.substring(dash + 1));
                                version[0] += 100 * Integer.parseInt(range.substring(0, dash));
                            }
                        } else {
                            cnb = codename;
                            hiMajor = 0;
                        }
                        sb.append(sep);
                        sep = ", ";
                        sb.append(cnb).append(";bundle-version=\"[");
                        String conditionalDot = "";
                        for (int i = 0; i < version.length; i++) {
                            sb.append(conditionalDot);
                            sb.append(version[i]);
                            conditionalDot = ".";
                        }
                        sb.append(",").append((hiMajor + 1) * 100).append(")\"");
                    }
                    added.addConfiguredAttribute(new Manifest.Attribute("Require-Bundle", sb.toString())); // NOI18N
                } else {
                    added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Module-Dependencies", moduleDeps));
                }
            }
            String javaDep = getProject().getProperty("javac.target");
            if (javaDep != null && javaDep.matches("[0-9]+(\\.[0-9]+)*")) {
                if (isOSGiMode) {
                    if (javaDep.matches("1\\.[0-5]")) {
                        added.addConfiguredAttribute(new Manifest.Attribute("Bundle-RequiredExecutionEnvironment", "J2SE-" + javaDep));
                    } else {
                        added.addConfiguredAttribute(new Manifest.Attribute("Bundle-RequiredExecutionEnvironment", "JavaSE-" + javaDep));
                    }
                } else {
                    added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Java-Dependencies", "Java > " + javaDep));
                }
            }
            String buildNumber = getProject().getProperty("buildnumber");
            if (buildNumber == null) {
                throw new BuildException("Must have defined 'buildnumber'", getLocation());
            }
            String attrToAdd = implVers != null ? "OpenIDE-Module-Build-Version" : "OpenIDE-Module-Implementation-Version";
            added.addConfiguredAttribute(new Manifest.Attribute(attrToAdd, buildNumber));
            // If spec.version.base is defined, use it, after tacking on any numeric impl deps (sorted by CNB of the dep for stability),
            // and also using the implementation version of this module as well if it is numeric.
            // This trick makes sure that if you have an impl dep on some module which changes its (numeric) impl version,
            // your spec version will also change correspondingly, so e.g. Auto Update will see a new version of your module too.
            String specVersBase = getProject().getProperty("spec.version.base");
            if (specVersBase != null) {
                boolean edited = false;
                if (implVers != null) {
                    try {
                        parseInt(implVers);
                        specVersBase += "." + implVers;
                        edited = true;
                    } catch (NumberFormatException e) {
                        // OK, ignore it, not numeric.
                        specVersBaseWarning(manifestFile, "use of spec.version.base with non-integer OpenIDE-Module-Implementation-Version");
                    }
                }
                SortedMap<String,Integer> additions = new TreeMap<>();
                if (moduleDeps != null) {
                    for (String individualDep : COMMA_SPACE.split(moduleDeps)) {
                        Matcher m = IMPL_DEP.matcher(individualDep);
                        if (m.matches()) {
                            String cnb = m.group(1);
                            String version = m.group(3);
                            try {
                                additions.put(cnb, parseInt(version));
                            } catch (NumberFormatException e) {
                                // OK, ignore this one, not numeric.
                                specVersBaseWarning(manifestFile,
                                        "use of spec.version.base with non-integer OpenIDE-Module-Implementation-Version from " + cnb);
                            }
                        }
                    }
                }
                for (int version : additions.values()) {
                    specVersBase += "." + version;
                    edited = true;
                }

                String versionTag = isOSGiMode ? "Bundle-Version" : "OpenIDE-Module-Specification-Version";

                if (edited) {
                    log("Computed " + versionTag + ": " + specVersBase);
                } else {
                    specVersBaseWarning(manifestFile,
                            "using spec.version.base for no reason; could just use " + versionTag + " statically in the manifest");
                }
                if (staticManifest.getMainSection().getAttributeValue(versionTag) != null) {
                    specVersBaseWarning(manifestFile,
            "attempting to use spec.version.base while some " + versionTag + " is statically defined in manifest.mf; this cannot work");
                } else {
                    added.addConfiguredAttribute(new Manifest.Attribute(versionTag, specVersBase));
                }
            } else if (moduleDeps != null && moduleDeps.indexOf('=') != -1) {
                specVersBaseWarning(manifestFile,
                        "not using spec.version.base, yet declaring implementation dependencies; may lead to problems with Auto Update");
            } else if (implVers != null) {
                if (specVersBase == null) {
                    specVersBaseWarning(manifestFile,
                            "not using spec.version.base, yet declaring implementation version; may lead to problems with Auto Update");
                } else {
                    try {
                        parseInt(implVers);
                    } catch (NumberFormatException e) {
                        specVersBaseWarning(manifestFile,
                        "use of non-integer OpenIDE-Module-Implementation-Version may be problematic for clients trying to use spec.version.base");
                    }
                }
            }
            if (isOSGiMode && added.getMainSection().getAttribute("Bundle-Version") == null && specVer != null) {
                added.getMainSection().addConfiguredAttribute(new Manifest.Attribute("Bundle-Version", specVer));
            }

            boolean old = false; // #110661
            String destDir = getProject().getProperty("netbeans.dest.dir");
            if (destDir != null) {
                for (File cluster : getProject().resolveFile(destDir).listFiles()) {
                    if (new File(cluster, "modules/org-netbeans-modules-autoupdate.jar").isFile()) {
                        old = true;
                        break;
                    }
                }
            }
            if (!old) {
                added.addConfiguredAttribute(new Manifest.Attribute("AutoUpdate-Show-In-Client", Boolean.toString( // #110572
                        !Project.toBoolean(getProject().getProperty("is.autoload")) && !Project.toBoolean(getProject().getProperty("is.eager")) &&
                        "modules".equals(getProject().getProperty("module.jar.dir")))));
            }
            // Now ask the regular <jar> task to add all this stuff to the regular manifest.mf.
            added.merge(staticManifest);
            if (!"lib".equals (getProject().getProperty("module.jar.dir")) && !isOSGiMode) {
                // modules in lib cannot request this token
                String key = "OpenIDE-Module-Requires";
                String token = "org.openide.modules.ModuleFormat1";
                String requires = staticManifest.getMainSection().getAttributeValue(key);
                String newRequires;
                if (requires != null) {
                    // #59671: have to modify it, not just use super.setManifest(manifestFile).
                    added.getMainSection().removeAttribute(key);
                    newRequires = requires + ", " + token;
                } else {
                    newRequires = token;
                }
                added.addConfiguredAttribute(new Manifest.Attribute(key, newRequires));
            }
            addConfiguredManifest(added);
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }

    private int[] parseDecimal(String trim, int max) {
        String[] segments = trim.split("\\.");
        int[] arr = new int[segments.length > max ? max : segments.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = parseInt(segments[i]);
        }
        return arr;
    }
    private static int parseInt(String v) throws NumberFormatException {
        if (!v.matches("0|[1-9][0-9]*")) { // 050123 is a date, -12 is illegal, etc.
            throw new NumberFormatException(v);
        }
        return Integer.parseInt(v);
    }

    private void specVersBaseWarning(File manifestFile, String message) throws BuildException {
        message = manifestFile + ": " + message + "\n(see http://wiki.netbeans.org/DevFaqImplementationDependency)" +
                "\n(define spec.version.base.fatal.warning=false in project.properties to make this be a nonfatal warning)";
        if (Project.toBoolean(getProject().getProperty("spec.version.base.fatal.warning"))) {
            throw new BuildException(message);
        } else {
            log(message, Project.MSG_WARN);
        }
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
        if (stamp != null) {
            log("Stamp " + stamp + " against " + zipFile, Project.MSG_DEBUG);
            if (stamp.lastModified() < zipFile.lastModified()) {
                try {
                    stamp.getParentFile().mkdirs();
                    stamp.createNewFile();
                    stamp.setLastModified(zipFile.lastModified());
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }
        }
    }

    
}
