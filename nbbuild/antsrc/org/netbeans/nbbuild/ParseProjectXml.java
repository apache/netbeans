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

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Parse a projectized module's <code>nbproject/project.xml</code> and
 * define various useful Ant properties based on the result.
 * @author Jesse Glick
 */
public final class ParseProjectXml extends Task {

    static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1";
    static final String NBM_NS2 = "http://www.netbeans.org/ns/nb-module-project/2";
    static final String NBM_NS3 = "http://www.netbeans.org/ns/nb-module-project/3";
    
    private File moduleProject;
    /**
     * Set the NetBeans module project to work on.
     */
    public void setProject(File f) {
        moduleProject = f;
    }
    private File projectFile;
    /**
     * Another option is to directly point to project file.
     * Used only in unit testing.
     */
    public void setProjectFile (File f) {
        projectFile = f;
    }
    private File getProjectFile () {
        if (projectFile != null) {
            return projectFile;
        }
        return new File(new File(moduleProject, "nbproject"), "project.xml");
    }

    private String publicPackagesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Public-Packages to.
     */
    public void setPublicPackagesProperty(String s) {
        publicPackagesProperty = s;
    }
    
    private String friendsProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Friends to.
     */
    public void setFriendsProperty(String s) {
        friendsProperty = s;
    }

    private String javadocPackagesProperty;
    /**
     * Set the property to set a list of public packages for Javadoc
     * to.
     */
    public void setJavadocPackagesProperty(String s) {
        javadocPackagesProperty = s;
    }

    private String moduleDependenciesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Module-Dependencies to, based on the list of
     * stated run-time dependencies.
     */
    public void setModuleDependenciesProperty(String s) {
        moduleDependenciesProperty = s;
    }

    private String codeNameBaseProperty;
    /**
     * Set the property to set the module code name base (separated by
     * dashes not dots) to.
     */
    public void setCodeNameBaseProperty(String s) {
        codeNameBaseProperty = s;
    }
    private String codeNameBaseDashesProperty;
    /**
     * Set the property to set the module code name base (separated by
     * dashes not dots) to.
     */
    public void setCodeNameBaseDashesProperty(String s) {
        codeNameBaseDashesProperty = s;
    }

    private String codeNameBaseSlashesProperty;
    /**
     * Set the property to set the module code name base (separated by
     * slashes not dots) to.
     */
    public void setCodeNameBaseSlashesProperty(String s) {
        codeNameBaseSlashesProperty = s;
    }

    private String commitMailProperty;
    /**
     * Set the property to set the module's commit mail address(es) to.
     * Only applicable to modules in netbeans.org (i.e. no {@code <path>}).
     */
    public void setCommitMailProperty(String s) {
        commitMailProperty = s;
    }

    private String moduleClassPathProperty;
    /**
     * Set the property to set the computed module class path to,
     * based on the list of stated compile-time dependencies.
     */
    public void setModuleClassPathProperty(String s) {
        moduleClassPathProperty = s;
    }

    private String moduleProcessorClassPathProperty;
    /**
     * Set the property to set the computed module processor class path to,
     * based on the transitive closure of compile-time dependencies.
     */
    public void setModuleProcessorClassPathProperty(String s) {
        moduleProcessorClassPathProperty = s;
    }

    private String moduleRunClassPathProperty;
    /**
     * Set the property to set the computed module runtime class path to.
     * This uses the transitive closure of runtime dependencies.
     */
    public void setModuleRunClassPathProperty(String s) {
        moduleRunClassPathProperty = s;
    }
    
    private File publicPackageJarDir;
    /**
     * Set the location of a directory in which to look for and create
     * JARs containing just the public packages of appropriate
     * compile-time dependencies.
     */
    public void setPublicPackageJarDir(File d) {
        publicPackageJarDir = d;
    }
    
    private String classPathExtensionsProperty;
    /**
     * Set the property to set the declared Class-Path attribute to.
     */
    public void setClassPathExtensionsProperty(String s) {
        classPathExtensionsProperty = s;
    }

    // test distribution path 
    private static String cachedTestDistLocation;

    public static class TestType {
        private String name;
        private String folder;
        private String runtimeCP;
        private String compileCP;
        /** compilation dependency supported only unit tests
         */
        private String compileDep;

        public TestType() {}
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getRuntimeCP() {
            return runtimeCP;
        }

        public void setRuntimeCP(String runtimeCP) {
            this.runtimeCP = runtimeCP;
        }

        public String getCompileCP() {
            return compileCP;
        }

        public void setCompileCP(String compileCP) {
            this.compileCP = compileCP;
        }

        public String getCompileDep() {
            return compileDep;
        }

        public void setCompileDep(String compileDep) {
            this.compileDep = compileDep;
        }
     }
      List<TestType> testTypes = new LinkedList<>();
      
      public void addTestType(TestType testType) {
          testTypes.add(testType);
      }
      public void add(TestType testType) {
          testTypes.add(testType);
      }
  
      
      private TestType getTestType(String name) {
          for (TestType testType : testTypes) {
              if (testType.getName().equals(name)) {
                  return testType;
              }
          }
          return null;
      }
 
 
    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }
    
    
    public @Override void execute() throws BuildException {
        try {
            if (getProjectFile() == null) {
                throw new BuildException("You must set 'project' or 'projectfile'", getLocation());
            }
            // XXX share parse w/ ModuleListParser
            Document pDoc = XMLUtil.parse(new InputSource(getProjectFile ().toURI().toString()),
                                          false, true, /*XXX*/null, null);
            VALIDATE: if (getModuleType(pDoc) == ModuleType.NB_ORG) {
                // Ensure project.xml is valid according to schema.
                File nball = new File(getProject().getProperty("nb_all"));
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                List<Source> sources = new ArrayList<>();
                String[] xsds = {
                    "project.ant/src/org/netbeans/modules/project/ant/project.xsd",
                    "apisupport.project/src/org/netbeans/modules/apisupport/project/resources/nb-module-project2.xsd",
                    "apisupport.project/src/org/netbeans/modules/apisupport/project/resources/nb-module-project3.xsd",
                };
                for (String xsd : xsds) {
                    File xsdF = new File(nball, xsd);
                    if (!xsdF.isFile()) {
                        break VALIDATE;
                    }
                    sources.add(new StreamSource(xsdF));
                }
                Schema schema = schemaFactory.newSchema(sources.toArray(new Source[0]));
                Validator validator = schema.newValidator();
                validator.setErrorHandler(new ErrorHandler() {
                    public void warning(SAXParseException x) throws SAXException {
                        throw x;
                    }
                    public void error(SAXParseException x) throws SAXException {
                        throw x;
                    }
                    public void fatalError(SAXParseException x) throws SAXException {
                        throw x;
                    }
                });
                validator.validate(new DOMSource(pDoc));
            }
            if (publicPackagesProperty != null || javadocPackagesProperty != null) {
                PublicPackage[] pkgs = getPublicPackages(pDoc);
                if (publicPackagesProperty != null) {
                    String val;
                    if (pkgs.length > 0) {
                        String sep = "";
                        StringBuffer b = new StringBuffer();
                        for (PublicPackage p : pkgs) {
                            b.append(sep);
                            
                            String name = p.name;
                            if (name.indexOf (',') >= 0) {
                                throw new BuildException ("Package name cannot contain ',' as " + p, getLocation ());
                            }
                            if (name.indexOf ('*') >= 0) {
                                throw new BuildException ("Package name cannot contain '*' as " + p, getLocation ());
                            }
                            
                            b.append(name);
                            if (p.subpackages) {
                                b.append (".**");
                            } else {
                                b.append(".*");
                            }
                            sep = ", ";
                        }
                        val = b.toString();
                    } else {
                        val = "-";
                    }
                    define(publicPackagesProperty, val);
                }
                NO_JAVA_DOC_PROPERTY_SET: if (javadocPackagesProperty != null) {
                    if (pkgs.length > 0) {
                        String sep = "";
                        StringBuffer b = new StringBuffer();
                        for (PublicPackage p : pkgs) {
                            b.append(sep);
                            if (p.subpackages) {
                                if (getProject().getProperty(javadocPackagesProperty) == null) {
                                    String msg = javadocPackagesProperty + " cannot be set as <subpackages> does not work for Javadoc (see <subpackages>" + p.name + "</subpackages> tag in " + getProjectFile () + "). Set the property in project.properties if you want to build Javadoc.";
                                    // #52135: do not halt the build, just leave it.
                                    getProject().log("Warning: " + msg, Project.MSG_WARN);
                                }
                                break NO_JAVA_DOC_PROPERTY_SET;
                            }
                            b.append(p.name);
                            sep = ", ";
                        }
                        define(javadocPackagesProperty, b.toString());
                    }
                }
            }
            if (friendsProperty != null) {
                String[] friends = getFriends(pDoc);
                if (friends != null) {
                    StringBuffer b = new StringBuffer();
                    for (String f : friends) {
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(f);
                    }
                    define(friendsProperty, b.toString());
                }
            }
            ModuleListParser modules = null;
            Dep[] rawDeps = null;
            Dep[] translatedDeps = null;
            String cnb = getCodeNameBase(pDoc);
            if (moduleDependenciesProperty != null || 
                    moduleClassPathProperty != null || 
                    moduleRunClassPathProperty != null ||
                    testTypes.size() > 0) {
                Hashtable<String,Object> properties = getProject().getProperties();
                properties.put("project", moduleProject.getAbsolutePath());
                modules = new ModuleListParser(properties, getModuleType(pDoc), getProject());
                ModuleListParser.Entry myself = modules.findByCodeNameBase(cnb);
                if (myself == null) { // #71130
                    ModuleListParser.resetCaches();
                    modules = new ModuleListParser(properties, getModuleType(pDoc), getProject());
                    myself = modules.findByCodeNameBase(cnb);
                    assert myself != null : "Cannot find myself as " + cnb;
                }
                Dep[][] deps = getDeps(cnb, pDoc, modules);
                rawDeps = deps[0];
                translatedDeps = deps[1];
            }
            if (moduleDependenciesProperty != null) {
                StringBuffer b = new StringBuffer();
                for (Dep d : rawDeps) {
                    if (!d.run) {
                        continue;
                    }
                    if (b.length() > 0) {
                        b.append(", ");
                    }
                    b.append(d);
                }
                if (b.length() > 0) {
                    define(moduleDependenciesProperty, b.toString());
                }
            }
            if (codeNameBaseProperty != null) {
                define(codeNameBaseProperty, cnb);
            }
            if (codeNameBaseDashesProperty != null) {
                define(codeNameBaseDashesProperty, cnb.replace('.', '-'));
            }
            if (codeNameBaseSlashesProperty != null) {
                define(codeNameBaseSlashesProperty, cnb.replace('.', '/'));
            }
            if (moduleClassPathProperty != null) {
                String cp = computeClasspath(cnb, modules, translatedDeps, false, false);
                define(moduleClassPathProperty, cp);
            }
            if (moduleProcessorClassPathProperty != null) {
                String cp = computeClasspath(cnb, modules, translatedDeps, false, true);
                define(moduleProcessorClassPathProperty, cp);
            }
            if (moduleRunClassPathProperty != null) {
                String cp = computeClasspath(cnb, modules, translatedDeps, true, true);
                define(moduleRunClassPathProperty, cp);
            }
            if (commitMailProperty != null) {
                if (getModuleType(pDoc) != ModuleType.NB_ORG) {
                    throw new BuildException("Cannot set " + commitMailProperty + " for a non-netbeans.org module", getLocation());
                }
                String name = getProject().getBaseDir().getName() + "/";
                StringBuilder aliases = null;
                File hgmail = new File(getProject().getProperty("nb_all"), ".hgmail");
                if (hgmail.canRead()) {
                    try (Reader r = new FileReader(hgmail)) {
                        BufferedReader br = new BufferedReader(r);
                        String line;
                        while ((line = br.readLine()) != null) {
                            int equals = line.indexOf('=');
                            if (equals == -1) {
                                continue;
                            }
                            for (String piece: line.substring(equals + 1).split(",")) {
                                if (name.matches(piece.replace(".", "[.]").replace("*", ".*"))) {
                                    if (aliases == null) {
                                        aliases = new StringBuilder();
                                    } else {
                                        aliases.append(' ');
                                    }
                                    aliases.append(line.substring(0, equals));
                                }
                            }
                        }
                    }
                } else {
                    log("Cannot find " + hgmail + " to read addresses from", Project.MSG_VERBOSE);
                }
                if (aliases != null) {
                    define(commitMailProperty, aliases.toString());
                }
            }
            if (classPathExtensionsProperty != null) {
                String val = computeClassPathExtensions(pDoc);
                if (val != null) {
                    define(classPathExtensionsProperty, val);
                }
            }
            
            // Test dependecies
            //
            if (modules != null) {
               String testDistLocation = getProject().getProperty(TestDeps.TEST_DIST_VAR);
               if (testDistLocation == null) {
                   testDistLocation = "${" + TestDeps.TEST_DIST_VAR + "}";
               }
               ParseProjectXml.cachedTestDistLocation = testDistLocation;
    
               for (TestDeps td : getTestDeps(pDoc, modules, cnb)) {
                   // unit tests
                   TestType testType = getTestType(td.testtype);
                   if (testType!= null ) {
                       if (testType.getFolder() != null) {
                           define(testType.getFolder(),td.getTestFolder());
                       }
                       if (testType.getCompileCP() != null) {
                           String cp = td.getCompileClassPath();
                           if (cp != null && cp.trim().length() > 0) {
                               define(testType.getCompileCP(), cp);
                           }
                       }
                       if (testType.getRuntimeCP() != null) {
                           String cp = td.getRuntimeClassPath();
                           if (cp != null && cp.trim().length() > 0) {
                               define(testType.getRuntimeCP(), cp);
                           }
                       }
                       String testCompileDep = td.getTestCompileDep();
                       if (testType.getCompileDep() != null && testCompileDep != null) {
                           define(testType.getCompileDep(), testCompileDep);
                       }
                   }
               }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }

    private Element getConfig(Document pDoc) throws BuildException {
        Element e = pDoc.getDocumentElement();
        Element c = XMLUtil.findElement(e, "configuration", PROJECT_NS);
        if (c == null) {
            throw new BuildException("No <configuration>", getLocation());
        }
        Element d = findNBMElement(c, "data");
        if (d == null) {
            throw new BuildException("No <data> in " + getProjectFile(), getLocation());
        }
        return d;
    }
    
    private static final class PublicPackage extends Object {
        public final String name;
        public boolean subpackages;
        
        public PublicPackage (String name, boolean subpackages) {
            this.name = name;
            this.subpackages = subpackages;
        }
    }

    private PublicPackage[] getPublicPackages(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = findNBMElement(cfg, "public-packages");
        if (pp == null) {
            pp = findNBMElement(cfg, "friend-packages");
        }
        if (pp == null) {
            throw new BuildException("No <public-packages>", getLocation());
        }
        List<PublicPackage> pkgs = new ArrayList<>();
        for (Element p : XMLUtil.findSubElements(pp)) {
            boolean sub = false;
            if ("friend".equals(p.getNodeName())) {
                continue;
            }
            if (!"package".equals (p.getNodeName ())) {
                if (!("subpackages".equals (p.getNodeName ()))) {
                    throw new BuildException ("Strange element name, should be package or subpackages: " + p.getNodeName (), getLocation ());
                }
                sub = true;
            }
            
            String t = XMLUtil.findText(p);
            if (t == null) {
                throw new BuildException("No text in <package>", getLocation());
            }
            pkgs.add(new PublicPackage(t, sub));
        }
        return pkgs.toArray(new PublicPackage[pkgs.size()]);
    }
    
    private String[] getFriends(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = findNBMElement(cfg, "friend-packages");
        if (pp == null) {
            return null;
        }
        List<String> friends = new ArrayList<>();
        boolean other = false;
        for (Element p : XMLUtil.findSubElements(pp)) {
            if ("friend".equals(p.getNodeName())) {
                String t = XMLUtil.findText(p);
                if (t == null) {
                    throw new BuildException("No text in <friend>", getLocation());
                }
                friends.add(t);
            } else {
                other = true;
            }
        }
        if (friends.isEmpty()) {
            throw new BuildException("Must have at least one <friend> in <friend-packages>", getLocation());
        }
        if (!other) {
            throw new BuildException("Must have at least one <package> in <friend-packages>", getLocation());
        }
        return friends.toArray(new String[friends.size()]);
    }

    private final class Dep {
        private final ModuleListParser modules;
        /** will be e.g. org.netbeans.modules.form */
        public String codenamebase;
        public String release = null;
        public String spec = null;
        public boolean impl = false;
        public boolean compile = false;
        public boolean run = false;
        
        public Dep(ModuleListParser modules) {
            this.modules = modules;
        }

        public Dep(String parse, ModuleListParser modules) {
            this(modules);
            Matcher m = Pattern.compile("([^=>/ ]+)(?:/([\\d-]+))?(?: > (.+)| = (.+))?").matcher(parse);
            if (!m.matches()) {
                throw new BuildException("Malformed dep: " + parse);
            }
            codenamebase = m.group(1);
            release = m.group(2);
            spec = m.group(3);
            impl = m.group(4) != null;
        }
        
        public @Override String toString() throws BuildException {
            StringBuffer b = new StringBuffer(codenamebase);
            if (release != null) {
                b.append('/');
                b.append(release);
            }
            if (spec != null) {
                b.append(" > ");
                b.append(spec);
                assert !impl;
            }
            if (impl) {
                b.append(" = "); // NO18N
                String implVers = implementationVersionOf(modules, codenamebase);
                if (implVers == null) {
                    throw new BuildException("No OpenIDE-Module-Implementation-Version found in " + codenamebase);
                }
                if (implVers.equals(getProject().getProperty("buildnumber"))) {
                    throw new BuildException("Cannot depend on module " + codenamebase + " using build number as an implementation version");
                }
                b.append(implVers);
            }
            return b.toString();
        }
        
        private String implementationVersionOf(ModuleListParser modules, String cnb) throws BuildException {
            File jar = computeClasspathModuleLocation(modules, cnb, null, null, false, Collections.emptyList());
            try {
                try (JarFile jarFile = new JarFile(jar, false)) {
                    return jarFile.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version");
                }
            } catch (IOException e) {
                throw new BuildException(e, getLocation());
            }
        }

        private boolean matches(Attributes attr, String[] version) {
            boolean[] osgi = new boolean[1];
            String givenCodeName = JarWithModuleAttributes.extractCodeName(attr, osgi);
            int slash = givenCodeName.indexOf('/');
            int givenRelease = -1;
            if (slash != -1) {
                assert codenamebase.equals(givenCodeName.substring(0, slash));
                givenRelease = Integer.parseInt(givenCodeName.substring(slash + 1));
            }
            if (release != null) {
                int dash = release.indexOf('-');
                if (dash == -1) {
                    if (Integer.parseInt(release) != givenRelease) {
                        return false;
                    }
                } else {
                    int lower = Integer.parseInt(release.substring(0, dash));
                    int upper = Integer.parseInt(release.substring(dash + 1));
                    if (givenRelease < lower || givenRelease > upper) {
                        return false;
                    }
                }
            } else if (run && givenRelease != -1) {
                return false;
            }
            if (spec != null) {
                String givenSpec = attr.getValue(
                    osgi[0] ?
                        "Bundle-Version" :
                        "OpenIDE-Module-Specification-Version"
                );
                if (givenSpec == null) {
                    return false;
                }
                version[0] = " found " + givenSpec;
                // XXX cannot use org.openide.modules.SpecificationVersion from here
                int[] specVals = digitize(spec, osgi[0]);
                int[] givenSpecVals = digitize(givenSpec, osgi[0]);
                int len1 = specVals.length;
                int len2 = givenSpecVals.length;
                int max = Math.max(len1, len2);
                for (int i = 0; i < max; i++) {
                    int d1 = ((i < len1) ? specVals[i] : 0);
                    int d2 = ((i < len2) ? givenSpecVals[i] : 0);
                    if (d1 < d2) {
                        break;
                    } else if (d1 > d2) {
                        return false;
                    }
                }
            }
            if (impl) {
                if (attr.getValue("OpenIDE-Module-Implementation-Version") == null) {
                    return false;
                }
            }
            return true;
        }
        private int[] digitize(String spec, boolean osgi) throws NumberFormatException {
            StringTokenizer tok = new StringTokenizer(spec, ".");
            int len = tok.countTokens();
            if (osgi && len > 3) {
                len = 3;
            }
            int[] digits = new int[len];
            for (int i = 0; i < len; i++) {
                digits[i] = Integer.parseInt(tok.nextToken());
            }
            return digits;
        }
        
    }

    private Dep[][] getDeps(String myCNB, Document pDoc, ModuleListParser modules) throws Exception {
        Element cfg = getConfig(pDoc);
        Element md = findNBMElement(cfg, "module-dependencies");
        if (md == null) {
            throw new BuildException("No <module-dependencies>", getLocation());
        }
        List<Dep> deps = new ArrayList<>();
        List<URL> moduleAutoDeps = new ArrayList<>();
        for (Element dep : XMLUtil.findSubElements(md)) {
            Element cnb = findNBMElement(dep, "code-name-base");
            if (cnb == null) {
                throw new BuildException("No <code-name-base>", getLocation());
            }
            String t = XMLUtil.findText(cnb);
            if (t == null) {
                throw new BuildException("No text in <code-name-base>", getLocation());
            }
            ModuleListParser.Entry other = modules.findByCodeNameBase(t);
            if (other != null) {
                File autodeps = other.getModuleAutoDeps();
                if (autodeps.exists()) {
                    moduleAutoDeps.add(autodeps.toURI().toURL());
                }
            }
            Dep d = new Dep(modules);
            d.codenamebase = t;
            Element rd = findNBMElement(dep, "run-dependency");
            if (rd != null) {
                d.run = true;
                Element rv = findNBMElement(rd, "release-version");
                if (rv != null) {
                    t = XMLUtil.findText(rv);
                    if (t == null) {
                        throw new BuildException("No text in <release-version>", getLocation());
                    }
                    d.release = t;
                }
                Element sv = findNBMElement(rd, "specification-version");
                if (sv != null) {
                    t = XMLUtil.findText(sv);
                    if (t == null) {
                        throw new BuildException("No text in <specification-version>", getLocation());
                    }
                    d.spec = t;
                }
                Element iv = findNBMElement(rd, "implementation-version");
                if (iv != null) {
                    d.impl = true;
                }
            }
            d.compile = findNBMElement(dep, "compile-dependency") != null;
            deps.add(d);
        }
        Dep[] rawDeps = deps.toArray(new Dep[deps.size()]);
        translateModuleAutoDeps(myCNB, deps, moduleAutoDeps, modules);
        Dep[] translatedDeps = deps.toArray(new Dep[deps.size()]);
        return new Dep[][] {rawDeps, translatedDeps};
    }

    private void translateModuleAutoDeps(String myCNB, List<Dep> deps, List<URL> moduleAutoDeps, ModuleListParser modules) throws Exception { // #178260
        if (moduleAutoDeps.isEmpty()) {
            return;
        }
        // determine warning level
        int warnLevel = Project.MSG_WARN;
        String s = getProject().getProperty("nbbuild.warn.missing.autodeps");
        if (null != s) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(s)) {
                warnLevel = Project.MSG_WARN;
            } else if (Boolean.FALSE.toString().equalsIgnoreCase(s)) {
                warnLevel = Project.MSG_DEBUG + 100; // should be ignored even when -d is present
            } else switch (s.toLowerCase()) {
                case "warn":    warnLevel = Project.MSG_WARN; break;
                case "err":     warnLevel = Project.MSG_ERR; break;
                case "info":    warnLevel = Project.MSG_INFO; break;
                case "verbose": warnLevel = Project.MSG_VERBOSE; break;
                case "debug":   warnLevel = Project.MSG_DEBUG; break;
                default:
                    throw new BuildException("Invalid value of nbbuild.warn.missing.autodeps property (" + s + "). See Project MSG_ constants.");
            }
        }
        Set<String> depsS = new HashSet<>();
        String result;
        AntClassLoader loader = new AntClassLoader();
        try {
        for (String[] coreModuleVariants : new String[][] {{"org.openide.util", "org.openide.util.base"}, {"org.openide.modules"}, {"org.netbeans.bootstrap"}, {"org.netbeans.core.startup"}, {"org.netbeans.core.startup.base"}}) {
            ModuleListParser.Entry entry = null;
            for (String coreModule : coreModuleVariants) {
                entry = modules.findByCodeNameBase(coreModule);
                if (entry != null) {
                    break;
                }
            }
            if (entry == null) {
                log("Cannot translate according to " + moduleAutoDeps + " because could not find none of" + Arrays.toString(coreModuleVariants), Project.MSG_WARN);
                return;
            }
            File jar = entry.getJar();
            if (!jar.isFile()) {
                log("Cannot translate according to " + moduleAutoDeps + " because could not find " + jar, warnLevel);
                return;
            }
            loader.addPathComponent(jar);
        }
        Class<?> automaticDependenciesClazz = loader.loadClass("org.netbeans.core.startup.AutomaticDependencies");
        Method refineDependenciesSimple;
        try {
            refineDependenciesSimple = automaticDependenciesClazz.getMethod("refineDependenciesSimple", String.class, Set.class);
        } catch (NoSuchMethodException x) {
            log("Cannot translate according to " + moduleAutoDeps + " because AutomaticDependencies is too old", Project.MSG_WARN);
            return;
        }
        for (Dep d : deps) {
            if (d.run) {
                depsS.add(d.toString());
            }
        }
        Object automaticDependencies = automaticDependenciesClazz.getMethod("parse", URL[].class).invoke(
                null, (Object) moduleAutoDeps.toArray(new URL[moduleAutoDeps.size()]));
        try {
            result = (String) refineDependenciesSimple.invoke(automaticDependencies, myCNB, depsS);
        } catch (InvocationTargetException x) {
            // Can occur when core modules are being recompiled in the same Ant run.
            log("Cannot translate according to " + moduleAutoDeps + " due to " + x.getCause(), Project.MSG_WARN);
            return;
        }
        } finally {
            loader.cleanup(); // #180970
        }
        if (result == null) {
            return;
        }
        log("warning: " + result, Project.MSG_WARN);
        Set<String> noCompileDeps = new HashSet<>();
        Iterator<Dep> it = deps.iterator();
        while (it.hasNext()) {
            Dep d = it.next();
            if (d.run) {
                it.remove();
                if (!d.compile) {
                    noCompileDeps.add(d.codenamebase);
                }
            }
        }
        for (String dS : depsS) {
            Dep d = new Dep(dS, modules);
            d.run = true;
            if (!noCompileDeps.contains(d.codenamebase)) {
                d.compile = true;
            }
            deps.add(d);
        }
    }

    private String getCodeNameBase(Document d) throws BuildException {
        Element data = getConfig(d);
        Element name = findNBMElement(data, "code-name-base");
        if (name == null) {
            throw new BuildException("No <code-name-base>", getLocation());
        }
        String t = XMLUtil.findText(name);
        if (t == null) {
            throw new BuildException("No text in <code-name-base>", getLocation());
        }
        return t;
    }

    private ModuleType getModuleType(Document d) throws BuildException {
        Element data = getConfig(d);
        if (findNBMElement(data, "suite-component") != null) {
            return ModuleType.SUITE;
        } else if (findNBMElement(data, "standalone") != null) {
            return ModuleType.STANDALONE;
        } else {
            return ModuleType.NB_ORG;
        }
    }

    private String computeClasspath(String myCNB, ModuleListParser modules, Dep[] deps, boolean runtime, boolean recursive) throws BuildException, IOException, SAXException {
        StringBuilder cp = new StringBuilder();
        Path clusterPathS = (Path) getProject().getReference("cluster.path.id");
        Set<File> clusterPath = null;
        if (clusterPathS != null) {
            clusterPath = new HashSet<>();
            for (Iterator<?> it = clusterPathS.iterator(); it.hasNext();) {
                File oneCluster = ((FileResource) it.next()).getFile();
                clusterPath.add(oneCluster);
            }
        }
        String excludedModulesProp = getProject().getProperty("disabled.modules");
        Set<String> excludedModules = excludedModulesProp != null ?
            new HashSet<>(Arrays.asList(excludedModulesProp.split(" *, *"))) :
            null;
        for (Dep dep : deps) {
            if (!runtime && !dep.compile) {
                continue;
            }
            String cnb = dep.codenamebase;
            List<String> path = new ArrayList<>();
            path.add(myCNB);
            File depJar = computeClasspathModuleLocation(modules, cnb, clusterPath, excludedModules, runtime, path);

            List<File> additions = new ArrayList<>();
            additions.add(depJar);
            if (recursive) {
                addRecursiveDeps(additions, modules, cnb, clusterPath, excludedModules, new HashSet<>(), runtime, path);
            }
            
            // #52354: look for <class-path-extension>s in dependent modules.
            ModuleListParser.Entry entry = modules.findByCodeNameBase(cnb);
            if (entry != null) {
                additions.addAll(Arrays.asList(entry.getClassPathExtensions()));
            }
            
            if (depJar.isFile()) { // might be false for m.run.cp if DO_NOT_RECURSE and have a runtime-only dep
                Attributes attr;
                try {
                    try (JarFile jarFile = new JarFile(depJar, false)) {
                        attr = jarFile.getManifest().getMainAttributes();
                    }
                } catch (ZipException x) {
                    throw new BuildException("Could not open " + depJar + ": " + x, x, getLocation());
                }

                String[] version = { "" };
                if (!dep.matches(attr, version)) { // #68631
                    throw new BuildException("Cannot compile against a module: " + depJar + " because of dependency: " + dep
                        + version[0], getLocation()
                    );
                }

                if (!runtime && Boolean.parseBoolean(attr.getValue("OpenIDE-Module-Deprecated"))) {
                    log("The module " + cnb + " has been deprecated", Project.MSG_WARN);
                }

                if (!dep.impl && /* #71807 */ dep.run && !recursive && !runtime) {
                    String friends = attr.getValue("OpenIDE-Module-Friends");
                    if (friends != null && !Arrays.asList(friends.split(" *, *")).contains(myCNB)) {
                        throw new BuildException("The module " + myCNB + " is not a friend of " + depJar, getLocation());
                    }
                    String pubpkgs = attr.getValue("OpenIDE-Module-Public-Packages");
                    if ("-".equals(pubpkgs)) {
                        throw new BuildException("The module " + depJar + " has no public packages and so cannot be compiled against", getLocation());
                    } else if (pubpkgs != null && publicPackageJarDir != null) {
                        File splitJar = createPublicPackageJar(additions, pubpkgs, publicPackageJarDir, cnb);
                        additions.clear();
                        additions.add(splitJar);
                    }
                }
            }

            for (File f : additions) {
                if (cp.length() > 0) {
                    cp.append(':');
                }
                cp.append(f.getAbsolutePath());
            }
        }
        // Also look for <class-path-extension>s for myself and put them in my own classpath.
        ModuleListParser.Entry entry = modules.findByCodeNameBase(myCNB);
        if (entry == null) {
            throw new IllegalStateException("Cannot find myself as " + myCNB);
        }
        for (File f : entry.getClassPathExtensions()) {
            cp.append(':');
            cp.append(f.getAbsolutePath());
        }
        return cp.toString();
    }
    
    private void addRecursiveDeps(List<File> additions, ModuleListParser modules, String cnb, 
            Set<File> clusterPath, Set<String> excludedModules, Set<String> skipCnb, boolean runtime, List<String> path) {
        if (!skipCnb.add(cnb)) {
            return;
        }
        log("Processing for recursive deps: " + cnb, Project.MSG_DEBUG);
        ModuleListParser.Entry entry = modules.findByCodeNameBase(cnb);
        if (entry == null) {
            log("No entry for " + cnb, Project.MSG_WARN);
            return;
        }
        String[] deps;
        if (runtime) {
            deps = entry.getRuntimeDependencies();
        } else {
            // XXX not quite right as we want <compile-dependency/> rather than <build-prerequisite/>, but probably close enough?
            deps = entry.getBuildPrerequisites();
            if (deps == null) {
                // XXX for binary entries we have no record of what is a compile vs. a runtime dependency
                deps = entry.getRuntimeDependencies();
            }
        }
        for (File f : entry.getClassPathExtensions()) {
            if (!additions.contains(f)) {
                additions.add(f);
            }
        }
        List<String> inPath = new ArrayList<>(path);
        inPath.add(cnb);
        for (String nextModule : deps) {
            log("  Added dep " + nextModule + " due to " + cnb, Project.MSG_DEBUG);
            File depJar = computeClasspathModuleLocation(modules, nextModule, clusterPath, excludedModules, true, inPath);
            if (!additions.contains(depJar)) {
                additions.add(depJar);
            }
            addRecursiveDeps(additions, modules, nextModule, clusterPath, excludedModules, skipCnb, runtime, inPath);
        }
    }

    static final String DO_NOT_RECURSE = "do.not.recurse";
    private File computeClasspathModuleLocation(ModuleListParser modules, String cnb,
            Set<File> clusterPath, Set<String> excludedModules, boolean runtime, List<String> path) throws BuildException {
        ModuleListParser.Entry module = modules.findByCodeNameBase(cnb);
        if (module == null && cnb.contains("-")) {
            final String alternativeCnb = cnb.replace('-', '_');
            module = modules.findByCodeNameBase(alternativeCnb);
            if (module != null) {
                cnb = alternativeCnb;
            }
        }
        if (module == null) {
            throw new BuildException("No dependent module " + cnb, getLocation());
        }
        File jar = module.getJar();
        if (jar == null) return null;

        OK: if (module.getClusterName() != null && clusterPath != null) {
            File clusterF = jar.getParentFile();
            while (clusterF != null) {
                if (clusterPath.contains(clusterF)) {
                    break OK;
                }
                clusterF = clusterF.getParentFile();
            }
            String msg = "The module " + cnb + " cannot be " + (runtime ? "run" : "compiled") +
                    " against because it is part of the cluster " +
                    jar.getParentFile().getParentFile() + " which is not part of cluster.path in your suite configuration.\n\n" +
                    "Cluster.path is: " + clusterPath;
            throw new BuildException(msg, getLocation());
        }
        if (excludedModules != null && excludedModules.contains(cnb)) { // again #68716
            throw new BuildException("Module " + cnb + " excluded from the target platform; the path is: " + path.toString(), getLocation());
        }
        if (!jar.isFile()) {
            File srcdir = module.getSourceLocation();
            if (Project.toBoolean(getProject().getProperty(DO_NOT_RECURSE))) {
                log(jar + " missing for " + moduleProject + " but will not first try to build " + srcdir, Project.MSG_VERBOSE);
                return jar;
            }
            if (srcdir != null && srcdir.isDirectory()) {
                log(jar + " missing for " + moduleProject + "; will first try to build " + srcdir, Project.MSG_WARN);
                Ant ant = new Ant();
                ant.setProject(getProject());
                ant.setOwningTarget(getOwningTarget());
                ant.setLocation(getLocation());
                ant.setInheritAll(false);
                ant.setDir(srcdir);
                ant.execute();
            }
        }
        if (!jar.isFile()) {
            throw new BuildException("No such classpath entry: " + jar, getLocation());
        }
        return jar;
    }
 
  final class TestDeps {
      public static final String UNIT = "unit";
      public static final String QA_FUNCTIONAL = "qa-functional";
      // unit, qa-functional, performance
      final String testtype;
      // all dependecies for the testtype
      final  List<TestDep> dependencies = new ArrayList<>();
      // code name base of tested module
      final String cnb;
      final ModuleListParser modulesParser;
      boolean fullySpecified;
      
      private Set<String> missingEntries;
  
      public  static final String TEST_DIST_VAR = "test.dist.dir";
      public TestDeps(String testtype,String cnb,ModuleListParser modulesParser) {
          assert modulesParser != null;
          this.testtype = testtype;
          this.cnb = cnb;
          this.modulesParser = modulesParser;
      }

       @Override
       public String toString() {
           return cnb + "/" + testtype + ":" + dependencies;
       }
      
      public List<String> getFiles(boolean compile) {
          List<String> files = new ArrayList<>();
          for (TestDep d : dependencies) {
              files.addAll(d.getFiles(compile));
          }
          return files;
      }
      public void addDependency(TestDep dep) {
          dependencies.add(dep);
          fullySpecified |= dep.cnb.equals("org.netbeans.libs.junit4");
          fullySpecified |= dep.cnb.equals("org.netbeans.libs.testng");
      }
      public void addOptionalDependency(TestDep dep) {
          if (dep.modulesParser.findByCodeNameBase(dep.cnb) != null) {
              dependencies.add(dep);
          }
      }

        private String getTestFolder() {
            ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
            assert entry.getNetbeansOrgPath() != null;
            String sep = "/";
            
            String cluster = entry.getClusterName(); 
            if (cluster == null) {
                // no cluster name is specified for standalone or module in module suite
                cluster = "cluster";
            }
            return ParseProjectXml.cachedTestDistLocation + sep + testtype + sep + cluster + sep + cnb.replace('.','-');
        }

        String getCompileClassPath() {
            return getPath(getFiles(true)) + getMissingEntries();
        }
        private String getPath(List<String> files) {
            StringBuffer path = new StringBuffer();
            Set<String> filesSet = new HashSet<>();
            for (String filePath : files) {
                if (!filesSet.contains(filePath)) {
                    if (path.length() > 0) {
                        path.append(File.pathSeparatorChar);
                    } 
                    filesSet.add(filePath);
                    path.append(filePath);
                }
            }
            return path.toString().replace(File.separatorChar,'/');    
        }

        String getRuntimeClassPath() {
            return getPath(getFiles(false)) + getMissingEntries();
        }
        
    /** construct test compilation compilation dependencies.
     * Use case: unit tests of masterfs depends on tests of fs
     * @return relative project folder paths separated by comma
     */
    public  String getTestCompileDep() {
        Set<String> cnbs = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        computeCompileDep(cnb,cnbs,builder);
        return (builder.length() > 0) ? builder.toString() : null;
    }
    
    private void computeCompileDep(String cnb,Set<String> cnbs,StringBuilder sb) {
        if (cnbs.contains(cnb)) {
            return;
        }
        ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
        if (!cnbs.isEmpty() && entry != null) {
            // check if is tests are already built
            for (String othertesttype : new String[] {"unit", "qa-functional"}) {
                // don't compile already compiled tests dependencies
                String p = testJarPath(entry, othertesttype);
                if (p != null && new File(p).exists()) {
                    return;
                }
            }
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            File srcPath = entry.getSourceLocation();
            if (srcPath != null) {
                sb.append(srcPath.getAbsolutePath());
            }
        }
        cnbs.add(cnb);
        if (entry != null) {
            for (String othertesttype : new String[] {"unit", "qa-functional"}) {
                String testDeps[] = entry.getTestDependencies().get(othertesttype);
                if (testDeps != null) {
                    for (String cnb2 : testDeps) {
                        computeCompileDep(cnb2,cnbs,sb);
                    }
                }
            }
        }
    }
    
   private void addMissingEntry(String cnb) {
        if (missingEntries == null) {
            missingEntries = new HashSet<>();
        }
        missingEntries.add(cnb);
    }
    
   private String getMissingEntries() {
       if ( missingEntries != null) {
           StringBuilder builder = new StringBuilder();
           if (missingEntries.contains("org.netbeans.libs.junit4")) {
               File junitJar = new File(System.getProperty("user.home"), ".m2/repository/junit/junit/4.13.2/junit-4.13.2.jar");
               if (junitJar.isFile()) {
                   builder.append(File.pathSeparatorChar).append(junitJar);
                   missingEntries.remove("org.netbeans.libs.junit4");
               } else {
                   builder.append("\nYou need to download and install org-netbeans-libs-junit4.nbm into the platform to run tests.");
                   builder.append("\nIf you have Maven and agree to http://www.opensource.org/licenses/cpl1.0.txt it suffices to run:");
                   builder.append("\nmvn dependency:get -Dartifact=junit:junit:4.13.2 -DrepoUrl=https://repo.maven.apache.org/maven2/");
               }
           }
           if (!missingEntries.isEmpty()) {
               builder.append("\n-missing-Module-Entries-: ");
               for (String missingEntry : missingEntries) {
                   builder.append(missingEntry).append('\n');
               }
           }
           return builder.toString();
       }
       return "";
    }
  }
   /** Test dependency for module and type
    */ 
   final class TestDep {
       final ModuleListParser modulesParser;
       // code name base
       final String cnb;
       // dependencies on tests of modules
       final boolean recursive;
       final boolean test;
       // runtime classpath
       final boolean compile;
       TestDeps testDeps;
       
       TestDep (String cnb,ModuleListParser modules, boolean recursive,boolean test, boolean compile,TestDeps testDeps) {   
           this.modulesParser = modules;
           this.cnb = cnb;
           this.recursive = recursive;
           this.test = test;
           this.testDeps = testDeps;
           this.compile = compile;
       }

       @Override
       public String toString() {
           return cnb + (recursive ? "/recursive" : "") + (test ? "/test" : "") + (compile ? "/compile" : "");
       }
       /* get modules dependecies
        */
       List<ModuleListParser.Entry> getModules() {
           List<ModuleListParser.Entry> entries = new ArrayList<>();
           if (recursive ) {
               Map<String,ModuleListParser.Entry> entriesMap = new HashMap<>();
               addRecursiveModules(cnb,entriesMap);
               entries.addAll(entriesMap.values());
           } else {
               ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
               if (entry == null) {
                   //throw new BuildException("Module "  + cnb + " doesn't exist.");
                   testDeps.addMissingEntry(cnb);
               } else {
                    entries.add(modulesParser.findByCodeNameBase(cnb));
               }
           }
           return entries;      
           
       } 
       
       private void addRecursiveModules(String cnb, Map<String,ModuleListParser.Entry> entriesMap) {
           if (!entriesMap.containsKey(cnb)) {
               ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
               if (entry == null) {
//                   throw new BuildException("Module "  + cnd + " doesn't exist.");
                   testDeps.addMissingEntry(cnb);
               } else {
                   entriesMap.put(cnb,entry);
                   String cnbs[] = entry.getRuntimeDependencies();
                   // cnbs can be null
                   if (cnbs != null) {
                       for (String c : cnbs) {
                           log("adding " + c + " due to " + cnb, Project.MSG_DEBUG);
                           addRecursiveModules(c, entriesMap);
                       }
                   }
               }
           }
       }
       List<String> getFiles(boolean compile) {
           List<String> files = new ArrayList<>();
           if (!compile ||  ( compile && this.compile)) {
               List<ModuleListParser.Entry> modules = getModules();
               for (ModuleListParser.Entry entry : getModules()) {
                   if (entry != null) {
                       files.add(entry.getJar().getAbsolutePath());
                   } else {
                       log("Entry doesn't exist.");
                   }
               }
               // get tests files
               if (test) {
                   // get test folder
                   String jarPath = getTestJarPath(false);
                   if (jarPath != null) {
                      files.add(jarPath);
                   }
                   jarPath = getTestJarPath(true);
                   if (jarPath != null) {
                      files.add(jarPath);
                   }
               }
           }
           return files;
       }
       /**
        * @param useUnit if true, try unit tests, even if this is of another type (so we can use unit test utils in any kind of tests)
        */
       public String getTestJarPath(boolean useUnit) {
           ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
           if (entry == null) {
               testDeps.addMissingEntry(cnb);
               return null;
           } else {
               String type = testDeps.testtype;
               if (useUnit) {
                   if (type.equals("unit")) {
                       return null;
                   } else {
                       type = "unit";
                   }
               }
               return testJarPath(entry, type);
           }
       }
   }

    private String testJarPath(ModuleListParser.Entry entry, String testType) {
        if (entry.getNetbeansOrgPath() != null) {
            String sep = File.separator;
            String cluster = entry.getClusterName();
            if (cluster == null) {
                cluster = "cluster";
            }
            return ParseProjectXml.cachedTestDistLocation + sep + testType + sep + cluster + sep + entry.getCnb().replace('.', '-') + sep + "tests.jar";
        } else {
            File src = entry.getSourceLocation();
            if (src != null) {
                return new File(src, "build/test/" + testType + "/classes").getAbsolutePath();
            } else {
                log("No source location for " + entry + " so cannot use as a test dependency", Project.MSG_VERBOSE);
                return null;
            }
        }
    }

    private String computeClassPathExtensions(Document pDoc) {
        Element data = getConfig(pDoc);
        StringBuffer list = null;
        for (Element ext : XMLUtil.findSubElements(data)) {
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element runtimeRelativePath = findNBMElement(ext, "runtime-relative-path");
            if (runtimeRelativePath == null) {
                throw new BuildException("Have malformed <class-path-extension> in " + getProjectFile(), getLocation());
            }
            String reltext = XMLUtil.findText(runtimeRelativePath);
            // interpret empty string as indication there's NO runtime-relative path
            if (reltext == null) {
                continue;
            }
            if (list == null) {
                list = new StringBuffer();
            } else {
                list.append(' ');
            }
            list.append(reltext.replace(" ", "%20"));
        }
        return list != null ? list.toString() : null;
    }

    /**
     * Create a compact JAR containing only classes in public packages.
     * Forces the compiler to honor public package restrictions.
     * @see "#59792"
     */
    private File createPublicPackageJar(List<File> jars, String pubpkgs, File dir, String cnb) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException("No such directory " + dir);
        }
        File ppjar = new File(dir, cnb.replace('.', '-') + ".jar");
        if (ppjar.exists()) {
            // Check if it is up to date first. Must be as new as any input JAR.
            boolean uptodate = true;
            long stamp = ppjar.lastModified();
            for (File jar : jars) {
                if (jar.lastModified() > stamp) {
                    uptodate = false;
                    break;
                }
            }
            if (uptodate) {
                log("Distilled " + ppjar + " was already up to date", Project.MSG_VERBOSE);
                return ppjar;
            }
        }
        log("Distilling " + ppjar + " from " + jars);
        String corePattern = pubpkgs.
                replaceAll(" +", "").
                replaceAll("\\.", "/").
                replaceAll(",", "|").
                replaceAll("\\*\\*", "(.+/)?").
                replaceAll("\\*", "");
        // include e.g. icons so that annotation processors using validateResource can confirm they exist
        Pattern p = Pattern.compile("(" + corePattern + ")[^/]+[.].+");
        boolean foundAtLeastOneEntry = false;
        // E.g.: (org/netbeans/api/foo/|org/netbeans/spi/foo/)[^/]+[.].+
        try (OutputStream os = new FileOutputStream(ppjar)) {
            ZipOutputStream zos = new ZipOutputStream(os);
            Set<String> addedPaths = new HashSet<>();
            for (File jar : jars) {
                if (!jar.isFile()) {
                    log("Classpath entry " + jar + " does not exist; skipping", Project.MSG_WARN);
                    continue;
                }
                try (InputStream is = new FileInputStream(jar)) {
                    ZipInputStream zis = new ZipInputStream(is);
                    ZipEntry inEntry;
                    while ((inEntry = zis.getNextEntry()) != null) {
                        String path = inEntry.getName();
                        if (!addedPaths.add(path)) {
                            continue;
                        }
                        long size = inEntry.getSize();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(size == -1 ? 4096 : (int) size);
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = zis.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                        byte[] data = baos.toByteArray();
                        boolean isEnum = isEnum(path, data);
                        if (!isEnum && !p.matcher(path).matches()) {
                            continue;
                        }
                        foundAtLeastOneEntry = true;
                        ZipEntry outEntry = new ZipEntry(path);
                        outEntry.setSize(data.length);
                        CRC32 crc = new CRC32();
                        crc.update(data);
                        outEntry.setCrc(crc.getValue());
                        zos.putNextEntry(outEntry);
                        zos.write(data);
                    }
                }
            }
            zos.close();
        }
        if (!foundAtLeastOneEntry) {
            ppjar.delete();
            throw new BuildException("The JARs " + jars + " contain no classes in the supposed public packages " +
                    pubpkgs + " and so cannot be compiled against", getLocation());
        }
        return ppjar;
    }
    private boolean isEnum(String path, byte[] data) throws UnsupportedEncodingException { // #152562: workaround for javac bug
        if (!path.endsWith(".class")) {
            return false;
        }
        String jvmName = path.substring(0, path.length() - ".class".length());
        String bytecode = new String(data, "ISO-8859-1");
        if (!bytecode.contains("$VALUES")) {
            return false;
        }
        if (!bytecode.contains("java/lang/Enum<L" + new String(jvmName.getBytes("UTF-8"), "ISO-8859-1") + ";>;")) {
            return false;
        }
        // XXX crude heuristic but unlikely to result in false positives
        return true;
    }

    private TestDeps[] getTestDeps(Document pDoc,ModuleListParser modules,String testCnb) {
        assert modules != null;
        Element cfg = getConfig(pDoc);
        List<TestDeps> testDepsList = new ArrayList<>();
        Element pp = findNBMElement(cfg, "test-dependencies");
        boolean existsUnitTests = false;
        boolean existsQaFunctionalTests = false;
        if (pp != null) {
            for (Element depssEl : XMLUtil.findSubElements(pp)) {
                String testType = findTextOrNull(depssEl,"name");
                if (testType == null) {
                    testType = TestDeps.UNIT; // default variant
                    existsUnitTests = true;
                } else if (testType.equals(TestDeps.UNIT)) {
                    existsUnitTests = true;
                } else if (testType.equals(TestDeps.QA_FUNCTIONAL)) {
                    existsQaFunctionalTests = true;
                }
                TestDeps testDeps = new TestDeps(testType,testCnb,modules);
                testDepsList.add(testDeps);
                for (Element el : XMLUtil.findSubElements(depssEl)) {
                    if (el.getTagName().equals("test-dependency")) {
                        // parse test dep
                        boolean  test =   (findNBMElement(el,"test") != null);
                        String cnb =  findTextOrNull(el,"code-name-base");
                        boolean  recursive = (findNBMElement(el,"recursive") != null);
                        boolean  compile = (findNBMElement(el,"compile-dependency") != null);
                        testDeps.addDependency(new TestDep(cnb,
                                                         modules,
                                                         recursive,
                                                         test,
                                                         compile,
                                                         testDeps)); 
                    }

                }
            }
        }
        for (TestDeps testDeps : testDepsList) {
            File testSrcDir = new File(moduleProject, "test/" + testDeps.testtype + "/src");
            if (!testSrcDir.isDirectory()) {
                String error = "No such dir " + testSrcDir + "; should not define test deps";
                if (getModuleType(pDoc) == ModuleType.NB_ORG) {
                    throw new BuildException(error, getLocation());
                } else {
                    // For compatibility reasons probably cannot make this fatal.
                    log(error, Project.MSG_WARN);
                }
            }
        }
        // #82204 intialize default testtypes when are not  in project.xml
        if (!existsUnitTests) {
            log("Default TestDeps for unit", Project.MSG_VERBOSE);
            testDepsList.add(new TestDeps(TestDeps.UNIT,testCnb,modules));
        }
        if (!existsQaFunctionalTests) {
            log("Default TestDeps for qa-functional", Project.MSG_VERBOSE);
            testDepsList.add(new TestDeps(TestDeps.QA_FUNCTIONAL,testCnb,modules));
        }
        for (TestDeps testDeps : testDepsList) {
            if (testDeps.fullySpecified) {
                continue;
            }
            if (new File(moduleProject, "test/" + testDeps.testtype + "/src").isDirectory()) {
                log("Warning: " + testCnb + " lacks a " + testDeps.testtype +
                        " test dependency on org.netbeans.libs.junit4; using default dependencies for compatibility", Project.MSG_WARN);
            }
            for (String library : new String[]{"org.netbeans.libs.junit4", "org.netbeans.modules.nbjunit", "org.netbeans.insane"}) {
                testDeps.addOptionalDependency(new TestDep(library, modules, false, false, true, testDeps));
            }
            if (testDeps.testtype.startsWith("qa-")) {
                // ProjectSupport moved from the old nbjunit.ide:
                testDeps.addOptionalDependency(new TestDep("org.netbeans.modules.java.j2seproject", modules, false, true, true, testDeps));
                // Need to include transitive deps of j2seproject in CP:
                testDeps.addOptionalDependency(new TestDep("org.netbeans.modules.java.j2seproject", modules, true, false, false, testDeps));
                // Common GUI testing tools:
                for (String library : new String[]{"org.netbeans.modules.jemmy"/* XXX now split up anyway: "org.netbeans.modules.jellytools"*/}) {
                    testDeps.addOptionalDependency(new TestDep(library, modules, false, false, true, testDeps));
                }
                // For NbModuleSuite, which needs to find the platform:
                testDeps.addOptionalDependency(new TestDep("org.openide.util", modules, false, false, false, testDeps));
            }
        }
        return testDepsList.toArray(new TestDeps[testDepsList.size()]);
    }
    static String findTextOrNull(Element parentElement,String elementName) {
        Element el = findNBMElement(parentElement,elementName);
        return (el == null) ? null :
                              XMLUtil.findText(el);
                
    }
    private static String NBM_NS_CACHE = NBM_NS3;
    static Element findNBMElement(Element el,String name) {
        Element retEl = XMLUtil.findElement(el,name,NBM_NS_CACHE) ;
        if (retEl == null) {
            NBM_NS_CACHE = (NBM_NS_CACHE.equals(NBM_NS3)) ? NBM_NS2 :NBM_NS3;
            retEl = XMLUtil.findElement(el,name,NBM_NS_CACHE) ;            
        }
        return retEl;
    }
 
}
