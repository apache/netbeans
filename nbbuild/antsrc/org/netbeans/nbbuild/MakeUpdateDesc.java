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
import java.util.Map;
import java.util.jar.Manifest;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/** Makes an XML file representing update information from NBMs.
 *
 * @author Jesse Glick
 */
public class MakeUpdateDesc extends MatchingTask {

    protected boolean usedMatchingTask = false;

    /** Set of NBMs presented as a folder in the Update Center. */
    public /*static*/ class Group {
        public List<FileSet> filesets = new ArrayList<>();
	public String name;

        /** Displayed name of the group. */
	public void setName (String s) {
	    name = s;
	}

        /** Add fileset to the group of NetBeans modules **/
        public void addFileSet (FileSet set) {
            filesets.add(set);
        }
    }
    
    /** pointer to another xml entity to include **/
    public class Entityinclude {
        public String file;
        /** Path to the entity file.
         * It included as an xml-entity pointer in master .xml file.
         */
	public void setFile (String f) {
	    file = f;
	}
    }

    private List<Entityinclude> entityincludes = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<FileSet> filesets = new ArrayList<>();

    private List<String> includeMessageDigests = Arrays.asList("SHA-512");

    public String getIncludeMessageDigests() {
        return includeMessageDigests.stream().collect(Collectors.joining(" "));
    }

    public void setIncludeMessageDigests(String includeMessageDigests) {
        if(includeMessageDigests == null) {
            includeMessageDigests = "";
        }
        this.includeMessageDigests = Arrays.asList(includeMessageDigests.split(" "));
    }

    private File desc;

    /** Description file to create. */
    public void setDesc(File d) {
        desc = d;
    }

    private File descLicense;

    /** Description file license to use. */
    public void setDescLicense(File d) {
        descLicense = d;
    }

    /** Module group to create **/
    public Group createGroup () {
	Group g = new Group ();
	groups.add (g);
	return g;
    }

    /** External XML entity include **/
    public Entityinclude createEntityinclude () {
        Entityinclude i = new Entityinclude ();
        entityincludes.add (i);
        return i;
    }

   /**
     * Adds a set of files (nested fileset attribute).
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }

    private boolean automaticGrouping;
    /**
     * Turn on if you want modules added to the root fileset
     * to be automatically added to a group based on their display category (if set).
     */
    public void setAutomaticgrouping(boolean b) {
        automaticGrouping = b;
    }
    
    private String dist_base;
   /**
    * Set distribution base, which will be enforced
    */
    public void setDistBase(String dbase) {
        dist_base = dbase;
    }

    private boolean useLicenseUrl;


    public void setUseLicenseUrl(boolean useLicenseUrl) {
        this.useLicenseUrl = useLicenseUrl;
    }
    
    private Path updaterJar;
    /** Fileset for platform/modules/ext/updater.jar, to be used in DTD validation. */
    public Path createUpdaterJar() {
        return updaterJar = new Path(getProject());
    }

    private String notificationMessage;
    private String notificationURL;

    public void setNotificationMessage(String message) {
        this.notificationMessage = message;
    }
    public void setNotificationURL(String url) {
        this.notificationURL = url;
    }

    
    private String contentDescription;
    private String contentDescriptionURL;

    public void setContentDescription(String message) {
        this.contentDescription = message;
    }
    public void setContentDescriptionURL(String url) {
        this.contentDescriptionURL = url;
    }

    
    // Similar to org.openide.xml.XMLUtil methods.
    private static String xmlEscape(String s) {
        int max = s.length();
        StringBuffer s2 = new StringBuffer((int)(max * 1.1 + 1));
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    s2.append("&lt;"); //NOI18N
                    break;
                case '>':
                    s2.append("&gt;"); //NOI18N
                    break;
                case '&':
                    s2.append("&amp;"); //NOI18N
                    break;
                case '"':
                    s2.append("&quot;"); //NOI18N
                    break;
                default:
                    s2.append(c);
                    break;
            }
        }
        return s2.toString();
    }

    public @Override void execute () throws BuildException {
        Group root = new Group();
        for (FileSet fs : filesets) {
            root.addFileSet(fs);
        }
        groups.add(root);
	if (desc.exists ()) {
	    // Simple up-to-date check.
	    long time = desc.lastModified ();
	    boolean uptodate = true;

	CHECK:
            for (Group g : groups) {
                for (FileSet n : g.filesets) {
                    if ( n != null ) {
                        DirectoryScanner ds = n.getDirectoryScanner(getProject());
                        String[] files = ds.getIncludedFiles();
                        File bdir = ds.getBasedir();
                        for (String file : files) {
                            File n_file = new File(bdir, file);
                            if (n_file.lastModified () > time) {
                                uptodate = false;
                                break CHECK;
                            }
                        }
		    }
		}
	    }
	    if (uptodate) return;
	}
	log ("Creating update description " + desc.getAbsolutePath ());
        
        Map<String,Collection<Module>> modulesByGroup = loadNBMs();
        boolean targetClustersDefined = false;
        for (Collection<Module> modules : modulesByGroup.values()) {
            for (Module m : modules) {
                targetClustersDefined |= m.xml.getAttributeNode("targetcluster") != null;
            }
        }
        boolean isPreferredUpdateDefined = false;
        for (Collection<Module> modules : modulesByGroup.values()) {
            for (Module m : modules) {
                isPreferredUpdateDefined |= m.xml.getAttributeNode("preferredupdate") != null;
            }
        }
        boolean use25DTD = false;
        for (Collection<Module> modules : modulesByGroup.values()) {
            for (Module m : modules) {
                Element manifest = ((Element) m.xml.getElementsByTagName("manifest").item(0));
                use25DTD |= (m.autoload || m.eager ||
                        manifest.getAttribute("AutoUpdate-Show-In-Client").length() > 0 ||
                        manifest.getAttribute("AutoUpdate-Essential-Module").length() > 0);
            }
        }
        
        // XXX Apparently cannot create a doc with entities using DOM 2.
	try {
            desc.delete();
            OutputStream os = new FileOutputStream(desc);
	    try {
                
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); //NOI18N
		pw.println ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //NOI18N
		pw.println ();
                if (descLicense != null) {
                    pw.println(new String(Files.readAllBytes(descLicense.toPath()), "UTF-8"));
                }
                DateFormat format = new SimpleDateFormat("ss/mm/HH/dd/MM/yyyy"); //NOI18N
                format.setTimeZone(TimeZone.getTimeZone("GMT")); //NOI18N
                String date = format.format(new Date());
                
            if ( entityincludes.size() > 0 ) {
                    // prepare .ent file
                    String ent_name = desc.getAbsolutePath();
                    int xml_idx = ent_name.indexOf(".xml"); //NOI18N
                    if (xml_idx != -1) {
                        ent_name = ent_name.substring (0, xml_idx) + ".ent"; //NOI18N
                    } else {
                        ent_name = ent_name + ".ent"; //NOI18N
                    }
                    File desc_ent = new File(ent_name);
                    desc_ent.delete();
                    if (includeMessageDigests != null && (! includeMessageDigests.isEmpty())) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.8//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_8.dtd\" [");
                    } else if (isPreferredUpdateDefined || (contentDescription != null && ! contentDescription.isEmpty())) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.7//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_7.dtd\" [");
                    } else if (useLicenseUrl) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.6//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd\" [");
                    } else if (use25DTD) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\" [");
                    } else if (targetClustersDefined) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_4.dtd\" [");
                    } else {
                        // #74866: no need for targetcluster, so keep compat w/ 5.0 AU.
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\" [");
                    }
                    // Would be better to follow order of groups and includes
                    pw.println ("    <!ENTITY entity SYSTEM \"" + xmlEscape(desc_ent.getName()) + "\">"); //NOI18N
                    int inc_num=0;
                    for (int i=0; i<entityincludes.size(); i++) {
                        Entityinclude ei = entityincludes.get(i);
                        pw.println ("    <!ENTITY include" + i + " SYSTEM \"" + xmlEscape(ei.file) + "\">"); //NOI18N
                    }
                    pw.println ("]>"); //NOI18N
                    pw.println ();
                    pw.println ("<module_updates timestamp=\"" + xmlEscape(date) + "\">"); //NOI18N
                    pw.println ("    &entity;"); //NOI18N
                    for (int i=0; i<entityincludes.size(); i++) {
                        pw.println ("    &include" + i + ";"); //NOI18N
                    }
                    pw.println ("</module_updates>"); //NOI18N
                    pw.println ();
                    pw.flush ();
                    pw.close ();
                
                    os = new FileOutputStream(desc_ent);
                    pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); //NOI18N
                    pw.println ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //NOI18N
                    pw.println ("<!-- external entity include " + date + " -->");
                    pw.println ();
                    
                } else {
                    if (includeMessageDigests != null && (! includeMessageDigests.isEmpty())) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.8//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_8.dtd\">");
                    } else if (isPreferredUpdateDefined || (contentDescription != null && ! contentDescription.isEmpty())) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.7//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_7.dtd\">");
                    } else if (useLicenseUrl) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.6//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd\">");
                    } else if (use25DTD) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\">");
                    } else if (targetClustersDefined) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_4.dtd\">");
                    } else {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\">");
                    }
                    pw.println ("<module_updates timestamp=\"" + date + "\">"); //NOI18N
                    pw.println ();
                }
                writeNotification(pw);
                pw.println ();
                writeContentDescription(pw);
                pw.println ();
		Map<String,Element> licenses = new HashMap<>();
                String prefix = null;
                if (dist_base != null) {
                    // fix/enforce distribution URL base
                    if (dist_base.equals(".")) {
                        prefix = "";
                    } else {
                        prefix = dist_base + "/";
                    }
                }
                final File licensesDir = new File(desc.getParentFile(), "licenses");
                if (useLicenseUrl) {
                     if (licensesDir.exists()) {
                         File [] licenseFiles = licensesDir.listFiles(new FileFilter() {
                            public boolean accept(File pathname) {
                                return pathname.getName().endsWith(".license");
                            }
                        });
                         for (File f : licenseFiles) {
                            f.delete();
                        }
                        } else {
                        licensesDir.mkdir();
                     }
                }

                for (Map.Entry<String,Collection<Module>> entry : modulesByGroup.entrySet()) {
                    String groupName = entry.getKey();
                    // Don't indent; embedded descriptions would get indented otherwise.
                    log("Creating group \"" + groupName + "\"");
                    if (groupName != null) {
                        pw.println("<module_group name=\"" + xmlEscape(groupName) + "\">");
                        pw.println();
                    }
                    for (Module m : entry.getValue()) {
                        Element module = m.xml;
                        if (module.getAttribute("downloadsize").equals("0")) {
                            module.setAttribute("downloadsize", Long.toString(m.nbm.length() + m.externalDownloadSize));
                        }
                        Element manifest = (Element) module.getElementsByTagName("manifest").item(0);
                        String name = manifest.getAttribute("OpenIDE-Module-Name");
                        if (name.length() > 0) {
                            log(" Adding module " + name + " (" + m.nbm.getAbsolutePath() + ")");
                        }                        
                        if(prefix!=null) {
                            module.setAttribute("distribution", prefix + m.relativePath);
                        }
                        NodeList licenseList = module.getElementsByTagName("license");
                        if (licenseList.getLength() > 0) {
                            Element license = (Element) licenseList.item(0);
                            if (useLicenseUrl) {
                                String relativePath = "licenses/" + license.getAttribute("name") + ".license";
                                String path = relativePath;
                                if (prefix != null) {
                                    path = prefix + relativePath;
                                }
                                license.setAttribute("url", path);
                                String licenseText = license.getTextContent();
                                license.setTextContent("");
                                try (FileOutputStream fos = new FileOutputStream(new File(desc.getParentFile(), relativePath))) {
                                    fos.write(licenseText.getBytes("UTF-8"));
                                }
                            }
                            // XXX ideally would compare the license texts to make sure they actually match up
                            licenses.put(license.getAttribute("name"), license);
                            module.removeChild(license);
                        }
                        if (m.autoload) {
                            module.setAttribute("autoload", "true");
                        }
                        if (m.eager) {
                            module.setAttribute("eager", "true");
                        }
                        pw.flush();
                        XMLUtil.write(module, os);
                        pw.println();
                    }
                    if (groupName != null) {
                        pw.println("</module_group>");
                        pw.println();
                    }
		}
                pw.flush();
                for (Element license : licenses.values()) {
                    XMLUtil.write(license, os);
                }

                if ( entityincludes.size() <= 0 ) {
                    pw.println ("</module_updates>"); //NOI18N
                    pw.println ();
                }
                pw.flush ();
		pw.close ();
	    } finally {
                os.flush ();
		os.close ();
	    }
	} catch (IOException ioe) {
	    desc.delete ();
	    throw new BuildException("Cannot create update description", ioe, getLocation());
	}
        if (updaterJar != null && updaterJar.size() > 0) {
            try {
                MakeNBM.validateAgainstAUDTDs(new InputSource(desc.toURI().toString()), updaterJar, this);
            } catch (Exception x) {
                desc.delete();
                throw new BuildException("Could not validate " + desc + " after writing: " + x, x, getLocation());
            }
        } else {
            log("No updater.jar specified, cannot validate " + desc + " against DTD", Project.MSG_WARN);
        }
    }

    private static class Module {
        public Module() {}
        public Element xml;
        public File nbm;
        public String relativePath;
        public boolean autoload, eager;
        public long externalDownloadSize;
    }

    private void writeNotification(PrintWriter pw) {
        // write notification message/url if defined
        if (notificationMessage == null) {
            notificationMessage = "";
        }
        if (notificationURL == null) {
            notificationURL = "";
        }
        if (notificationMessage.length() > 0 || notificationURL.length() > 0) {
            if (notificationMessage.length() == 0) {
                pw.println("<notification url=\"" + xmlEscape(notificationURL.toString()) + "\"/>");
            } else if (notificationURL.length() == 0) {
                pw.println("<notification>" + xmlEscape(notificationMessage) + "</notification>");
            } else {
                pw.println(
                        "<notification url=\"" + xmlEscape(notificationURL.toString()) + "\">" +
                        xmlEscape(notificationMessage) +
                        "</notification>");
            }
            pw.println();
        }
    }

    private void writeContentDescription(PrintWriter pw) {
        // write content_description message/url if defined
        if (contentDescription == null) {
            contentDescription = "";
        }
        if (contentDescriptionURL == null) {
            contentDescriptionURL = "";
        }
        if (contentDescription.length() > 0 || contentDescriptionURL.length() > 0) {
            if (contentDescription.length() == 0) {
                pw.println("<content_description url=\"" + xmlEscape(contentDescriptionURL.toString()) + "\"/>");
            } else if (contentDescriptionURL.length() == 0) {
                pw.println("<content_description>" + xmlEscape(contentDescription) + "</content_description>");
            } else {
                pw.println(
                        "<content_description url=\"" + xmlEscape(contentDescriptionURL.toString()) + "\">" +
                        xmlEscape(contentDescription) +
                        "</content_description>");
            }
            pw.println();
        }
    }

    private Map<String,Collection<Module>> loadNBMs() throws BuildException {
        final Collator COLL = Collator.getInstance(/* XXX any particular locale? */);
        // like COLL but handles nulls ~ ungrouped modules (sorted to top):
        Comparator<String> groupNameComparator = new Comparator<String>() {
            public int compare(String gn1, String gn2) {
                return gn1 != null ?
                    (gn2 != null ? COLL.compare(gn1, gn2) : 1) :
                    (gn2 != null ? -1 : 0);
            }
        };
        Map<String,Collection<Module>> r = automaticGrouping ?
            // generally will be creating groups on the fly, so sort them:
            new TreeMap<>(groupNameComparator) :
            // preserve explicit order of <group>s:
            new LinkedHashMap<>();
        // sort modules by display name (where available):
        Comparator<Module> moduleDisplayNameComparator = new Comparator<Module>() {
            public int compare(Module m1, Module m2) {
                int res = COLL.compare(getName(m1), getName(m2));
                return res != 0 ? res : System.identityHashCode(m1) - System.identityHashCode(m2);
            }
            String getName(Module m) {
                Element mani = (Element) m.xml.getElementsByTagName("manifest").item(0);
                String displayName = mani.getAttribute("OpenIDE-Module-Name");
                if (displayName.length() > 0) {
                    return displayName;
                } else {
                    return mani.getAttribute("OpenIDE-Module");
                }
            }
        };
        for (Group g : groups) {
            Collection<Module> modules = r.get(g.name);
            if (modules == null) {
                modules = new TreeSet<>(moduleDisplayNameComparator);
                r.put(g.name, modules);
            }
            for (FileSet fs : g.filesets) {
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                for (String file : ds.getIncludedFiles()) {
                    File n_file = new File(fs.getDir(getProject()), file);
                    try {
                        try (JarFile jar = new JarFile(n_file)) {
                            Module m = new Module();
                            m.nbm = n_file;
                            m.relativePath = file.replace(File.separatorChar, '/');
                            Manifest jarmani = jar.getManifest();
                            if (jarmani != null && jarmani.getMainAttributes().getValue("Bundle-SymbolicName") != null) {
                                // #181025: treat OSGi bundles specially.
                                m.xml = fakeOSGiInfoXml(jar, n_file);
                                modules.add(m);
                                continue;
                            }
                            ZipEntry entry = jar.getEntry("Info/info.xml");
                            if (entry == null) {
                                throw new BuildException("NBM " + n_file + " was malformed: no Info/info.xml", getLocation());
                            }
                            InputStream is = jar.getInputStream(entry);
                            try {
                                m.xml = XMLUtil.parse(new InputSource(is), false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver()).getDocumentElement();
                            } finally {
                                is.close();
                            }
                            Collection<Module> moduleCollection = modules;
                            Element manifest = ((Element) m.xml.getElementsByTagName("manifest").item(0));
                            if (automaticGrouping && g.name == null) {
                                // insert modules with no explicit grouping into group acc. to manifest:
                                String categ = manifest.getAttribute("OpenIDE-Module-Display-Category");
                                if (categ.length() > 0) {
                                    moduleCollection = r.get(categ);
                                    if (moduleCollection == null) {
                                        moduleCollection = new TreeSet<>(moduleDisplayNameComparator);
                                        r.put(categ, moduleCollection);
                                    }
                                }
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
                                String cnb = manifest.getAttribute("OpenIDE-Module").replaceFirst("/\\d+$", "");
                                entry = jar.getEntry("netbeans/config/Modules/" + cnb.replace('.', '-') + ".xml");
                                if (entry != null) {
                                    is = jar.getInputStream(entry);
                                    try {
                                        NodeList nl = XMLUtil.parse(new InputSource(is), false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver()).getElementsByTagName("param");
                                        for (int i = 0; i < nl.getLength(); i++) {
                                            String name = ((Element) nl.item(i)).getAttribute("name");
                                            String value = ((Text) nl.item(i).getFirstChild()).getData();
                                            if (name.equals("autoload") && value.equals("true")) {
                                                m.autoload = true;
                                            }
                                            if (name.equals("eager") && value.equals("true")) {
                                                m.eager = true;
                                            }
                                        }
                                    } finally {
                                        is.close();
                                    }
                                }
                            }
                            Enumeration<JarEntry> en = jar.entries();
                            while (en.hasMoreElements()) {
                                JarEntry e = en.nextElement();
                                if (e.getName().endsWith(".external")) {
                                    try (InputStream eStream = jar.getInputStream(e)) {
                                        m.externalDownloadSize += externalSize(eStream);
                                    }
                                }
                            }

                            Map<String,String> messageDigests = createMessageDigests(n_file);
                            for(Entry<String,String> messageDigest: messageDigests.entrySet()) {
                                Element messageDigestElement = m.xml.getOwnerDocument().createElement("message_digest");
                                messageDigestElement.setAttribute("algorithm", messageDigest.getKey());
                                messageDigestElement.setAttribute("value", messageDigest.getValue());
                                m.xml.appendChild(messageDigestElement);
                            }

                            moduleCollection.add(m);
                        }
                    } catch (BuildException x) {
                        throw x;
                    } catch (Exception e) {
                        throw new BuildException("Cannot process " + n_file + ": " + e, e, getLocation());
                    }
                }
            }
        }
        return r;
    }

    private long externalSize(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        for (;;) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("SIZE:")) {
                return Long.parseLong(line.substring(5).trim());
            }
        }
        return 0;
    }
    
    /**
     * Create the equivalent of {@code Info/info.xml} for an OSGi bundle.
     * @param jar a bundle
     * @return a {@code <module ...><manifest .../></module>} valid according to
     *         <a href="http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd">DTD</a>
     */
    private static Element fakeOSGiInfoXml(JarFile jar, File whereFrom) throws IOException {
        return fakeOSGiInfoXml(jar, whereFrom, XMLUtil.createDocument("module"));
    }
    //TODO: javadoc
    public static Element fakeOSGiInfoXml(JarFile jar, File whereFrom, Document doc) throws IOException {
        Attributes attr = jar.getManifest().getMainAttributes();
        Properties localized = new Properties();
        String bundleLocalization = attr.getValue("Bundle-Localization");
        if (bundleLocalization != null) {
            try (InputStream is = jar.getInputStream(jar.getEntry(bundleLocalization + ".properties"))) {
                localized.load(is);
            }
        }
        return fakeOSGiInfoXml(attr, localized, whereFrom, doc);
    }
    static Element fakeOSGiInfoXml(Attributes attr, Properties localized, File whereFrom) { //tests
        return fakeOSGiInfoXml(attr, localized, whereFrom, XMLUtil.createDocument("module"));
    }
    private static Element fakeOSGiInfoXml(Attributes attr, Properties localized, File whereFrom, Document doc) {
        Element module = doc.getDocumentElement();
        String cnb = JarWithModuleAttributes.extractCodeName(attr);
        module.setAttribute("codenamebase", cnb);
        module.setAttribute("distribution", ""); // seems to be ignored anyway
        module.setAttribute("downloadsize", "0"); // recalculated anyway
        module.setAttribute("targetcluster", whereFrom.getParentFile().getName()); // #207075 comment #3
        Element manifest = doc.createElement("manifest");
        module.appendChild(manifest);
        manifest.setAttribute("AutoUpdate-Show-In-Client", "false");
        manifest.setAttribute("OpenIDE-Module", cnb);
        String bundleName = loc(localized, attr, "Bundle-Name");
        manifest.setAttribute("OpenIDE-Module-Name", bundleName != null ? bundleName : cnb);
        String bundleVersion = attr.getValue("Bundle-Version");
        manifest.setAttribute("OpenIDE-Module-Specification-Version",
                bundleVersion != null ? bundleVersion.replaceFirst("^(\\d+([.]\\d+([.]\\d+)?)?)([.].+)?$", "$1") : "0");
        String requireBundle = attr.getValue("Require-Bundle");
        if (requireBundle != null) {
            StringBuilder b = new StringBuilder();
            boolean needsNetbinox = false;
            // http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
            for (String dep : requireBundle.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")) {
                Matcher m = Pattern.compile("([^;]+)(.*)").matcher(dep);
                if (!m.matches()) {
                    throw new BuildException("Could not parse dependency: " + dep + " in " + whereFrom);
                }
                String requiredBundleName = m.group(1); // dep CNB
                if(requiredBundleName.trim().equals("org.eclipse.osgi")) {
                    needsNetbinox = true;
                    continue;
                }
                Matcher m2 = Pattern.compile(";([^:=]+):?=\"?([^;\"]+)\"?").matcher(m.group(2));
                boolean isOptional = false;
                while(m2.find()) {
                    if(m2.group(1).equals("resolution") && m2.group(2).equals("optional")) {
                        isOptional = true;
                        break;
                    }
                }
                if(isOptional) {
                    continue;
                } 
                m2.reset();
                StringBuilder depSB = new StringBuilder();
                depSB.append(requiredBundleName); // dep CNB
                while (m2.find()) {
                    if (!m2.group(1).equals("bundle-version")) {
                        continue;
                    }
                    String val = m2.group(2);
                    if (val.matches("[0-9]+([.][0-9]+)*")) {
                        // non-range dep occasionally used in OSGi; no exact equivalent in NB
                        depSB.append(" > ").append(val);
                        continue;
                    }
                    Matcher m3 = Pattern.compile("\\[([0-9]+)((?:[.][0-9]+)*),([0-9.]+)\\)").matcher(val);
                    if (!m3.matches()) {
                        throw new BuildException("Could not parse version range: " + val + " in " + whereFrom);
                    }
                    int major = Integer.parseInt(m3.group(1));
                    String rest = m3.group(2);
                    try {
                        int max = Integer.parseInt(m3.group(3));
                        if (major > 99) {
                            depSB.append('/').append(major / 100);
                            if (max > major + 100) {
                                depSB.append('-').append(max / 100 - 1);
                            }
                        } else if (max > 100) {
                            depSB.append("/0-").append(max / 100 - 1);
                        }
                    } catch (NumberFormatException x) {
                        // never mind end boundary, does not match NB conventions
                    }
                    depSB.append(" > ").append(major % 100).append(rest);
                }
                if (b.indexOf(depSB.toString()) == -1) {
                    if (b.length() > 0) {
                        b.append(", ");
                    }
                    b.append(depSB);
                }
            }
            if(b.length() > 0) {
                manifest.setAttribute("OpenIDE-Module-Module-Dependencies", b.toString());
            }
            if(needsNetbinox) {
                manifest.setAttribute("OpenIDE-Module-Needs", "org.netbeans.Netbinox");
            }
        }
        String provides = computeExported(attr).toString();
        if (! provides.isEmpty()) {
            manifest.setAttribute("OpenIDE-Module-Provides", provides);
        }
        String recommends = computeImported(attr).toString();
        if (! recommends.isEmpty()) {
            manifest.setAttribute("OpenIDE-Module-Recommends", recommends);
        }
        String bundleCategory = loc(localized, attr, "Bundle-Category");
        if (bundleCategory != null) {
            manifest.setAttribute("OpenIDE-Module-Display-Category", bundleCategory);
        }
        String bundleDescription = loc(localized, attr, "Bundle-Description");
        if (bundleDescription != null) {
            manifest.setAttribute("OpenIDE-Module-Short-Description", bundleDescription);
        }
        // XXX anything else need to be set?
        return module;
    }
    private static String loc(Properties localized, Attributes attr, String key) {
        String val = attr.getValue(key);
        if (val == null) {
            return null;
        } else if (val.startsWith("%")) {
            return localized.getProperty(val.substring(1));
        } else {
            return val;
        }
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
    
    private static StringBuilder computeExported(Attributes attr) {
        StringBuilder sb = new StringBuilder();
        String pkgs = attr.getValue("Export-Package"); // NOI18N
        if (pkgs == null) {
            return sb;
        }
        StringTokenizer tok = createTokenizer(pkgs); // NOI18N
        String token;
        while (tok.hasMoreElements()) {
            token = beforeSemicolon(tok);
            if (sb.indexOf(token) == -1) {
                sb.append(sb.length() == 0 ? "" : ", ").append(token);
            }
        }
        return sb;
    }
    
    private static StringBuilder computeImported(Attributes attr) {
        StringBuilder sb = new StringBuilder();
        String pkgs = attr.getValue("Import-Package"); // NOI18N
        if (pkgs != null) {
            StringTokenizer tok = createTokenizer(pkgs); // NOI18N
            String token;
            while (tok.hasMoreElements()) {
                token = beforeSemicolon(tok);
                if (sb.indexOf(token) == -1) {
                    sb.append(sb.length() == 0 ? "" : ", ").append(token);
                }
            }
        }
        String recomm = attr.getValue("Require-Bundle"); // NOI18N
        if (recomm != null) {
            StringTokenizer tok = createTokenizer(recomm); // NOI18N
            String token;
            while (tok.hasMoreElements()) {
                token = beforeSemicolon(tok);
                if (sb.indexOf(token) == -1) {
                    sb.append(sb.length() == 0 ? "" : ", ").append("cnb.").append(token);
                }
            }
        }
        return sb;
    }

    private Map<String,String> createMessageDigests(File targetFile) {
        if(includeMessageDigests.isEmpty()) {
            return Collections.<String,String>emptyMap();
        }

        Map<String,MessageDigest> digests = new HashMap<>(includeMessageDigests.size());

        for(String messageDigest: includeMessageDigests) {
            try {
                digests.put(messageDigest, MessageDigest.getInstance(messageDigest));
            } catch (NoSuchAlgorithmException ex) {
                throw new BuildException("Failed to load requested messageDigest: " + messageDigest, ex);
            }
        }

        try(FileInputStream fis = new FileInputStream(targetFile)) {
            byte[] buffer = new byte[102400];
            int read;
            while((read = fis.read(buffer)) >= 0) {
                for(MessageDigest md: digests.values()) {
                    md.update(buffer, 0, read);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MakeUpdateDesc.class.getName()).log(Level.SEVERE, null, ex);
        }

        Map<String,String> result = new HashMap<>();
        for(Entry<String,MessageDigest> md: digests.entrySet()) {
            result.put(md.getKey(), hexEncode(md.getValue().digest()));
        }
        return result;
    }

    private static String hexEncode(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * 2);
        for(byte b: input) {
            sb.append(Character.forDigit((b & 0xF0) >> 4, 16));
            sb.append(Character.forDigit((b & 0x0F), 16));
        }
        return sb.toString();
    }

    private static byte[] hexDecode(String input) {
        int length = input.length() / 2;
        byte[] result = new byte[length];
        for(int i = 0; i < length; i++) {
            int b = Character.digit(input.charAt(i * 2), 16) << 4;
            b |= Character.digit(input.charAt(i * 2 + 1), 16);
            result[i] = (byte) (b & 0xFF);
        }
        return result;
    }
}
