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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.updater.XMLUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateInfoParser extends DefaultHandler {
    private static final String INFO_NAME = "info";
    private static final String INFO_EXT = ".xml";
    private static final String INFO_FILE = INFO_NAME + INFO_EXT;
    private static final String INFO_DIR = "Info";
    private static final String INFO_LOCALE = "locale";
    
    private final Map<String, UpdateItem> items;
    private final EntityResolver entityResolver;
    private final File nbmFile;
    private UpdateLicenseImpl currentUpdateLicenseImpl;
    
    private AutoupdateInfoParser (Map<String, UpdateItem> items, File nbmFile) {
        this.items = items;
        this.entityResolver = newEntityResolver();
        this.nbmFile = nbmFile;
    }

    private EntityResolver newEntityResolver () {
        try {
            Class.forName("org.netbeans.updater.XMLUtil");
        } catch (ClassNotFoundException e) {
            final File netbeansHomeFile = new File(System.getProperty("netbeans.home"));
            final File userdir = new File(System.getProperty("netbeans.user"));

            final String updaterPath = "modules/ext/updater.jar";
            final String newUpdaterPath = "update/new_updater/updater.jar";

            final File updaterPlatform = new File(netbeansHomeFile, updaterPath);
            final File updaterUserdir  = new File(userdir, updaterPath);

            final File newUpdaterPlatform = new File(netbeansHomeFile, newUpdaterPath);
            final File newUpdaterUserdir  = new File(userdir, newUpdaterPath);

            String message =
                    "    org.netbeans.updater.XMLUtil is not accessible\n" +
                    "    platform dir = " + netbeansHomeFile.getAbsolutePath() + "\n" +
                    "    userdir  dir = " + userdir.getAbsolutePath() + "\n" +
                    "    updater in platform exist = " + updaterPlatform.exists() + (updaterPlatform.exists() ? (", length = " + updaterPlatform.length() + " bytes") : "") + "\n" +
                    "    updater in userdir  exist = " + updaterUserdir.exists() + (updaterUserdir.exists() ? (", length = " + updaterUserdir.length() + " bytes") : "") + "\n" +
                    "    new updater in platform exist = " + newUpdaterPlatform.exists() + (newUpdaterPlatform.exists() ? (", length = " + newUpdaterPlatform.length() + " bytes") : "") + "\n" +
                    "    new updater in userdir  exist = " + newUpdaterUserdir.exists() + (newUpdaterUserdir.exists() ? (", length = " + newUpdaterUserdir.length() + " bytes") : "") + "\n";

            ERR.log(Level.WARNING, message);
        }
        return org.netbeans.updater.XMLUtil.createAUResolver ();
    }
    
    private static final Logger ERR = Logger.getLogger (AutoupdateInfoParser.class.getName ());
    
    private static enum ELEMENTS {
        module, description,
        module_notification, external_package, manifest, l10n, license
    }
    
    private static final String LICENSE_ATTR_NAME = "name";
    
    private static final String MODULE_ATTR_CODE_NAME_BASE = "codenamebase";
    private static final String MODULE_ATTR_HOMEPAGE = "homepage";
    private static final String MODULE_ATTR_DOWNLOAD_SIZE = "downloadsize";
    private static final String MODULE_ATTR_NEEDS_RESTART = "needsrestart";
    private static final String MODULE_ATTR_MODULE_AUTHOR = "moduleauthor";
    private static final String MODULE_ATTR_RELEASE_DATE = "releasedate";
    private static final String MODULE_ATTR_IS_GLOBAL = "global";
    private static final String MODULE_ATTR_IS_PREFERRED_UPDATE = "preferredupdate";
    private static final String MODULE_ATTR_TARGET_CLUSTER = "targetcluster";
    private static final String MODULE_ATTR_EAGER = "eager";
    private static final String MODULE_ATTR_AUTOLOAD = "autoload";
    private static final String MODULE_ATTR_LICENSE = "license";
    
    private static final String MANIFEST_ATTR_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version";
    private static final String MANIFEST_ATTR_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host";
    
    private static final String L10N_ATTR_LOCALE = "langcode";
    private static final String L10N_ATTR_BRANDING = "brandingcode";
    private static final String L10N_ATTR_MODULE_SPECIFICATION = "module_spec_version";
    private static final String L10N_ATTR_MODULE_MAJOR_VERSION = "module_major_version";
    private static final String L10N_ATTR_LOCALIZED_MODULE_NAME = "OpenIDE-Module-Name";
    private static final String L10N_ATTR_LOCALIZED_MODULE_DESCRIPTION = "OpenIDE-Module-Long-Description";
    
    public static Map<String, UpdateItem> getUpdateItems (File nbmFile) throws IOException, SAXException {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance ().newSAXParser ();
            if (!isOSGiBundle(nbmFile)) {
                // standard NBM
                saxParser.parse(getAutoupdateInfoInputSource(nbmFile), new AutoupdateInfoParser(items, nbmFile));
            } else {
                // OSGi
                saxParser.parse(getAutoupdateInfoInputStream(nbmFile), new AutoupdateInfoParser(items, nbmFile));
            }
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (ParserConfigurationException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        }
        return items;
    }
    
    private final Stack<ModuleDescriptor> currentModule = new Stack<ModuleDescriptor> ();
    private final Stack<String> currentLicense = new Stack<String> ();
    private final List<String> lines = new ArrayList<String> ();

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
        lines.add (new String(ch, start, length));
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
        switch (ELEMENTS.valueOf (qName)) {
            case module :
                assert ! currentModule.empty () : "Premature end of module " + qName;
                currentModule.pop ();
                break;
            case l10n :
                break;
            case manifest :
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case module_notification :
                // write module notification
                ModuleDescriptor md = currentModule.peek ();
                assert md != null : "ModuleDescriptor found for " + nbmFile;
                StringBuilder buf = new StringBuilder ();
                for (String line : lines) {
                    buf.append (line);
                }
                md.appendNotification (buf.toString ());
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                assert ! currentLicense.empty () : "Premature end of license " + qName;
                
                // find and fill UpdateLicenseImpl
                StringBuilder sb = new StringBuilder ();
                for (String line : lines) {
                    sb.append (line);
                }
                
                assert currentUpdateLicenseImpl != null : "UpdateLicenseImpl found for " + nbmFile;
                currentUpdateLicenseImpl.setAgreement (sb.toString ());
                
                currentLicense.pop ();
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }

    @Override
    public void endDocument () throws SAXException {
        ERR.fine ("End parsing " + nbmFile + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startDocument () throws SAXException {
        ERR.fine ("Start parsing " + nbmFile + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lines.clear();
        switch (ELEMENTS.valueOf (qName)) {
            case module :
                ModuleDescriptor md = new ModuleDescriptor (nbmFile);
                md.appendModuleAttributes (attributes);
                currentModule.push (md);
                break;
            case l10n :
                // construct l10n
                // XXX
                break;
            case manifest :
                
                // construct module
                ModuleDescriptor desc = currentModule.peek ();
                desc.appendManifest (attributes);
                UpdateItem m = desc.createUpdateItem ();
                
                // put license impl to map for future refilling
                UpdateItemImpl impl = Trampoline.SPI.impl (m);
                currentUpdateLicenseImpl = impl.getUpdateLicenseImpl ();
                
                // put module into UpdateItems
                items.put (desc.getId (), m);
                
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case module_notification :
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                currentLicense.push (attributes.getValue (LICENSE_ATTR_NAME));
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }
    
    @Override
    public InputSource resolveEntity (String publicId, String systemId) throws IOException, SAXException {
        return entityResolver.resolveEntity (publicId, systemId);
    }
    
    private static class ModuleDescriptor {
        private String moduleCodeName;
        private String targetcluster;
        private String homepage;
        private String downloadSize;
        private String author;
        private String publishDate;
        private String notification;
        private String fragmentHost;

        private Boolean needsRestart;
        private Boolean isGlobal;
        private Boolean isEager;
        private Boolean isAutoload;
        private Boolean isPreferredUpdate;

        private String specVersion;
        private Manifest mf;

        private UpdateLicense lic;
        
        private final File nbmFile;
        
        public ModuleDescriptor (File nbmFile) {
            this.nbmFile = nbmFile;
        }
        
        public void appendModuleAttributes (Attributes module) {
            moduleCodeName = module.getValue (MODULE_ATTR_CODE_NAME_BASE);
            targetcluster = module.getValue (MODULE_ATTR_TARGET_CLUSTER);
            homepage = module.getValue (MODULE_ATTR_HOMEPAGE);
            downloadSize = module.getValue (MODULE_ATTR_DOWNLOAD_SIZE);
            author = module.getValue (MODULE_ATTR_MODULE_AUTHOR);
            publishDate = module.getValue (MODULE_ATTR_RELEASE_DATE);
            if (publishDate == null || publishDate.length () == 0) {
                publishDate = Utilities.formatDate (new Date (nbmFile.lastModified ()));
            }
            String needsrestart = module.getValue (MODULE_ATTR_NEEDS_RESTART);
            String global = module.getValue (MODULE_ATTR_IS_GLOBAL);
            String eager = module.getValue (MODULE_ATTR_EAGER);
            String autoload = module.getValue (MODULE_ATTR_AUTOLOAD);
            String preferred = module.getValue(MODULE_ATTR_IS_PREFERRED_UPDATE);
                        
            needsRestart = needsrestart == null || needsrestart.trim ().length () == 0 ? null : Boolean.valueOf (needsrestart);
            isGlobal = global == null || global.trim ().length () == 0 ? null : Boolean.valueOf (global);
            isEager = Boolean.parseBoolean (eager);
            isAutoload = Boolean.parseBoolean (autoload);
            isPreferredUpdate = Boolean.parseBoolean(preferred);
                        
            String licName = module.getValue (MODULE_ATTR_LICENSE);
            lic = UpdateLicense.createUpdateLicense (licName, null);
        }
        
        public void appendManifest (Attributes manifest) {
            specVersion = manifest.getValue (MANIFEST_ATTR_SPECIFICATION_VERSION);
            fragmentHost = manifest.getValue(MANIFEST_ATTR_FRAGMENT_HOST);
            mf = getManifest (manifest);
        }
        
        public void appendNotification (String notification) {
            this.notification = notification;
        }
        
        public String getId () {
            return moduleCodeName + '_' + specVersion; // NOI18N
        }
        
        public UpdateItem createUpdateItem () {
            URL distributionUrl = null;
            try {
                distributionUrl = org.openide.util.Utilities.toURI(nbmFile).toURL (); // nbm as a source
            } catch (MalformedURLException ex) {
                ERR.log (Level.INFO, null, ex);
            }
            UpdateItem res = UpdateItem.createModule (
                    moduleCodeName,
                    specVersion,
                    distributionUrl,
                    author,
                    downloadSize,
                    homepage,
                    publishDate,
                    null, // no group
                    mf,
                    isEager,
                    isAutoload,
                    needsRestart,
                    isGlobal,
                    isPreferredUpdate,
                    targetcluster,
                    lic);
            
            // read module notification
            UpdateItemImpl impl = Trampoline.SPI.impl(res);
            ((ModuleItem) impl).setModuleNotification (notification);
            ((ModuleItem) impl).setFragmentHost(fragmentHost);
            
            return res;
        }
    }
    
    private static Manifest getManifest (Attributes attrList) {
        Manifest mf = new Manifest ();
        java.util.jar.Attributes mfAttrs = mf.getMainAttributes ();

        for (int i = 0; i < attrList.getLength (); i++) {
            mfAttrs.put (new java.util.jar.Attributes.Name (attrList.getQName (i)), attrList.getValue (i));
        }
        return mf;
    }

    private static InputSource getAutoupdateInfoInputSource (File nbmFile) throws IOException, SAXException {
        // find info.xml entry
        JarFile jf = null;
        try {
            jf = new JarFile (nbmFile);
        } catch (IOException ex) {
            throw new IOException("Cannot open NBM file " + nbmFile + ": " + ex, ex);
        }
        String locale = Locale.getDefault ().toString ();
        ZipEntry entry = jf.getEntry (INFO_DIR + '/' + INFO_LOCALE + '/' + INFO_NAME + '_' + locale + INFO_EXT);
        if (entry == null) {
            entry = jf.getEntry (INFO_DIR + '/' + INFO_FILE);
        }
        if (entry == null) {
            throw new IllegalArgumentException ("info.xml found in file " + nbmFile);
        }        

        return new InputSource (new BufferedInputStream (jf.getInputStream (entry)));
    }
    
    private static InputStream getAutoupdateInfoInputStream (File nbmFile) throws IOException, SAXException {
        try {
            // find info.xml entry
            JarFile jf = null;
            try {
                jf = new JarFile (nbmFile);
            } catch (IOException ex) {
                throw new IOException("Cannot open NBM file " + nbmFile + ": " + ex, ex);
            }
            Element fakeOSGiInfoXml = fakeOSGiInfoXml(jf, nbmFile);
            
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            Source source = new DOMSource(fakeOSGiInfoXml.getOwnerDocument());
            transformer.transform(source, result);
            writer.close();
            String xml = writer.toString();
            
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));  
            
            return inputStream;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (TransformerException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private static boolean isOSGiBundle(File jarFile) {
        try {
            JarFile jar = new JarFile(jarFile);
            Manifest mf = jar.getManifest();
            return mf != null && mf.getMainAttributes().getValue("Bundle-SymbolicName") != null; // NOI18N
        } catch (IOException ioe) {
            ERR.log(Level.INFO, ioe.getLocalizedMessage(), ioe);
        }
        return false;
    }

    /**
     * Create the equivalent of {@code Info/info.xml} for an OSGi bundle.
     *
     * @param jar a bundle
     * @return a {@code <module ...><manifest .../></module>} valid according to
     * <a href="http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd">DTD</a>
     */
    private static Element fakeOSGiInfoXml(JarFile jar, File whereFrom) throws IOException {
        java.util.jar.Attributes attr = jar.getManifest().getMainAttributes();
        Properties localized = new Properties();
        String bundleLocalization = attr.getValue("Bundle-Localization");
        if (bundleLocalization != null) {
            InputStream is = jar.getInputStream(jar.getEntry(bundleLocalization + ".properties"));
            try {
                localized.load(is);
            } finally {
                is.close();
            }
        }
        return fakeOSGiInfoXml(attr, localized, whereFrom);
    }

    public static final String BUNDLE_EXPORT_PACKAGE = "Export-Package"; // NOI18N
    public static final String BUNDLE_IMPORT_PACKAGE = "Import-Package"; // NOI18N
    
    static Element fakeOSGiInfoXml(java.util.jar.Attributes attr, Properties localized, File whereFrom) {
        Document doc = XMLUtil.createDocument("module");
        Element module = doc.getDocumentElement();
        String cnb = extractCodeName(attr, null);
        module.setAttribute("codenamebase", cnb);
        module.setAttribute("distribution", ""); // seems to be ignored anyway
        module.setAttribute("downloadsize", "0"); // recalculated anyway
        module.setAttribute("targetcluster", whereFrom.getParentFile().getName()); // #207075 comment #3
        Element manifest = doc.createElement("manifest");
        module.appendChild(manifest);
        manifest.setAttribute("AutoUpdate-Show-In-Client", "true"); // show me in UI
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
                    throw new RuntimeException("Could not parse dependency: " + dep + " in " + whereFrom);
                }
                String requiredBundleName = m.group(1); // dep CNB
                if (requiredBundleName.trim().equals("org.eclipse.osgi")) {
                    needsNetbinox = true;
                    continue;
                }
                Matcher m2 = Pattern.compile(";([^:=]+):?=\"?([^;\"]+)\"?").matcher(m.group(2));
                boolean isOptional = false;
                while (m2.find()) {
                    if (m2.group(1).equals("resolution") && m2.group(2).equals("optional")) {
                        isOptional = true;
                        break;
                    }
                }
                if (isOptional) {
                    continue;
                }
                m2.reset();
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(requiredBundleName.replace('-', '_')); // dep CNB
                while (m2.find()) {
                    if (!m2.group(1).equals("bundle-version")) {
                        continue;
                    }
                    String val = m2.group(2);
                    if (val.matches("[0-9]+([.][0-9]+)*")) {
                        // non-range dep occasionally used in OSGi; no exact equivalent in NB
                        b.append(" > ").append(val);
                        continue;
                    }
                    Matcher m3 = Pattern.compile("\\[([0-9]+)((?:[.][0-9]+)*),([0-9.]+)\\)").matcher(val);
                    if (!m3.matches()) {
                        throw new RuntimeException("Could not parse version range: " + val + " in " + whereFrom);
                    }
                    int major = Integer.parseInt(m3.group(1));
                    String rest = m3.group(2);
                    try {
                        int max = Integer.parseInt(m3.group(3));
                        if (major > 99) {
                            b.append('/').append(major / 100);
                            if (max > major + 100) {
                                b.append('-').append(max / 100 - 1);
                            }
                        } else if (max > 100) {
                            b.append("/0-").append(max / 100 - 1);
                        }
                    } catch (NumberFormatException x) {
                        // never mind end boundary, does not match NB conventions
                    }
                    b.append(" > ").append(major % 100).append(rest);
                }
            }
            if (b.length() > 0) {
                manifest.setAttribute("OpenIDE-Module-Module-Dependencies", b.toString());
            }
            if (needsNetbinox) {
                manifest.setAttribute("OpenIDE-Module-Needs", "org.netbeans.Netbinox");
            }
        }

        String pp = attr.getValue(BUNDLE_EXPORT_PACKAGE);
        StringBuilder provides = new StringBuilder();
        if (pp != null) {
            for (String p : pp.replaceAll("\"[^\"]*\"", "").split(",")) {
                if (provides.length() > 0) {
                    provides.append(',');
                }
                provides.append(p.replaceAll(";.*$", "").trim());
            }
        }
        if (provides.length() > 0) {
            manifest.setAttribute("OpenIDE-Module-Provides", provides.toString());
        }

        String ip = attr.getValue(BUNDLE_IMPORT_PACKAGE);
        StringBuilder recommends = new StringBuilder();
        if (ip != null) {
            for (String p : ip.replaceAll("\"[^\"]*\"", "").split(",")) {
                String pkg = p.replaceAll(";.*$", "").trim();
                if (JAVA_PLATFORM_PACKAGES.contains(pkg)) {
                    continue;
                }
                if (recommends.length() > 0) {
                    recommends.append(',');
                }
                recommends.append(p.replaceAll(";.*$", "").trim());
            }
        }
        if (recommends.length() > 0) {
            manifest.setAttribute("OpenIDE-Module-Recommends", recommends.toString().replace('-', '_'));
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

    private static String loc(Properties localized, java.util.jar.Attributes attr, String key) {
        String val = attr.getValue(key);
        if (val == null) {
            return null;
        } else if (val.startsWith("%")) {
            return localized.getProperty(val.substring(1));
        } else {
            return val;
        }
    }
    
    private static String extractCodeName(java.util.jar.Attributes attr, boolean[] osgi) {
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

    /**
     * List of packages guaranteed to be in the Java platform;
     * taken from JDK 6 Javadoc package-list after removing java.* packages.
     * Note that Felix's default.properties actually includes a few more packages
     * (such as org.w3c.dom.ranges) which can be found in src.zip but are not documented.
     * COPIED FROM: MakeOSGi
     */
    private static final Set<String> JAVA_PLATFORM_PACKAGES = new TreeSet<String>(Arrays.asList(
        "javax.accessibility",
        "javax.activation",
        "javax.activity",
        "javax.annotation",
        "javax.annotation.processing",
        "javax.crypto",
        "javax.crypto.interfaces",
        "javax.crypto.spec",
        "javax.imageio",
        "javax.imageio.event",
        "javax.imageio.metadata",
        "javax.imageio.plugins.bmp",
        "javax.imageio.plugins.jpeg",
        "javax.imageio.spi",
        "javax.imageio.stream",
        "javax.jws",
        "javax.jws.soap",
        "javax.lang.model",
        "javax.lang.model.element",
        "javax.lang.model.type",
        "javax.lang.model.util",
        "javax.management",
        "javax.management.loading",
        "javax.management.modelmbean",
        "javax.management.monitor",
        "javax.management.openmbean",
        "javax.management.relation",
        "javax.management.remote",
        "javax.management.remote.rmi",
        "javax.management.timer",
        "javax.naming",
        "javax.naming.directory",
        "javax.naming.event",
        "javax.naming.ldap",
        "javax.naming.spi",
        "javax.net",
        "javax.net.ssl",
        "javax.print",
        "javax.print.attribute",
        "javax.print.attribute.standard",
        "javax.print.event",
        "javax.rmi",
        "javax.rmi.CORBA",
        "javax.rmi.ssl",
        "javax.script",
        "javax.security.auth",
        "javax.security.auth.callback",
        "javax.security.auth.kerberos",
        "javax.security.auth.login",
        "javax.security.auth.spi",
        "javax.security.auth.x500",
        "javax.security.cert",
        "javax.security.sasl",
        "javax.sound.midi",
        "javax.sound.midi.spi",
        "javax.sound.sampled",
        "javax.sound.sampled.spi",
        "javax.sql",
        "javax.sql.rowset",
        "javax.sql.rowset.serial",
        "javax.sql.rowset.spi",
        "javax.swing",
        "javax.swing.border",
        "javax.swing.colorchooser",
        "javax.swing.event",
        "javax.swing.filechooser",
        "javax.swing.plaf",
        "javax.swing.plaf.basic",
        "javax.swing.plaf.metal",
        "javax.swing.plaf.multi",
        "javax.swing.plaf.synth",
        "javax.swing.table",
        "javax.swing.text",
        "javax.swing.text.html",
        "javax.swing.text.html.parser",
        "javax.swing.text.rtf",
        "javax.swing.tree",
        "javax.swing.undo",
        "javax.tools",
        "javax.transaction",
        "javax.transaction.xa",
        "javax.xml",
        "javax.xml.bind",
        "javax.xml.bind.annotation",
        "javax.xml.bind.annotation.adapters",
        "javax.xml.bind.attachment",
        "javax.xml.bind.helpers",
        "javax.xml.bind.util",
        "javax.xml.crypto",
        "javax.xml.crypto.dom",
        "javax.xml.crypto.dsig",
        "javax.xml.crypto.dsig.dom",
        "javax.xml.crypto.dsig.keyinfo",
        "javax.xml.crypto.dsig.spec",
        "javax.xml.datatype",
        "javax.xml.namespace",
        "javax.xml.parsers",
        "javax.xml.soap",
        "javax.xml.stream",
        "javax.xml.stream.events",
        "javax.xml.stream.util",
        "javax.xml.transform",
        "javax.xml.transform.dom",
        "javax.xml.transform.sax",
        "javax.xml.transform.stax",
        "javax.xml.transform.stream",
        "javax.xml.validation",
        "javax.xml.ws",
        "javax.xml.ws.handler",
        "javax.xml.ws.handler.soap",
        "javax.xml.ws.http",
        "javax.xml.ws.soap",
        "javax.xml.ws.spi",
        "javax.xml.ws.wsaddressing",
        "javax.xml.xpath",
        "org.ietf.jgss",
        "org.omg.CORBA",
        "org.omg.CORBA.DynAnyPackage",
        "org.omg.CORBA.ORBPackage",
        "org.omg.CORBA.TypeCodePackage",
        "org.omg.CORBA.portable",
        "org.omg.CORBA_2_3",
        "org.omg.CORBA_2_3.portable",
        "org.omg.CosNaming",
        "org.omg.CosNaming.NamingContextExtPackage",
        "org.omg.CosNaming.NamingContextPackage",
        "org.omg.Dynamic",
        "org.omg.DynamicAny",
        "org.omg.DynamicAny.DynAnyFactoryPackage",
        "org.omg.DynamicAny.DynAnyPackage",
        "org.omg.IOP",
        "org.omg.IOP.CodecFactoryPackage",
        "org.omg.IOP.CodecPackage",
        "org.omg.Messaging",
        "org.omg.PortableInterceptor",
        "org.omg.PortableInterceptor.ORBInitInfoPackage",
        "org.omg.PortableServer",
        "org.omg.PortableServer.CurrentPackage",
        "org.omg.PortableServer.POAManagerPackage",
        "org.omg.PortableServer.POAPackage",
        "org.omg.PortableServer.ServantLocatorPackage",
        "org.omg.PortableServer.portable",
        "org.omg.SendingContext",
        "org.omg.stub.java.rmi",
        "org.w3c.dom",
        "org.w3c.dom.bootstrap",
        "org.w3c.dom.events",
        "org.w3c.dom.ls",
        "org.xml.sax",
        "org.xml.sax.ext",
        "org.xml.sax.helpers"
    ));

}
