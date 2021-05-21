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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogParser extends DefaultHandler {
    private final Map<String, UpdateItem> items;
    private final AutoupdateCatalogProvider provider;
    private final EntityResolver entityResolver;
    private final URI baseUri;
    private final List<MessageDigestValue> messageDigestsBuffer = new ArrayList<>();
    
    private AutoupdateCatalogParser (Map<String, UpdateItem> items, AutoupdateCatalogProvider provider, URI base) {
        this.items = items;
        this.provider = provider;
        this.entityResolver = newEntityResolver();
        this.baseUri = base;
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
                    "    updater in platform exist = " + updaterInfo(updaterPlatform) + "\n" +
                    "    updater in userdir  exist = " + updaterInfo(updaterUserdir) + "\n" +
                    "    new updater in platform exist = " + updaterInfo(newUpdaterPlatform) + "\n" +
                    "    new updater in userdir  exist = " + updaterInfo(newUpdaterUserdir) + "\n";

            ERR.log(Level.WARNING, message);
        }
        return org.netbeans.updater.XMLUtil.createAUResolver ();
    }

    
    private static final Logger ERR = Logger.getLogger (AutoupdateCatalogParser.class.getName ());

    private String updaterInfo(final File updaterLocation) {
        StringBuilder sb = new StringBuilder();
        sb.append(updaterLocation.exists());
        if (updaterLocation.exists()) {
            try {
                sb.append(", length = ").append(updaterLocation.length()).append(" bytes");
                URLClassLoader url = new URLClassLoader(new URL[] { updaterLocation.toURI().toURL() });
                sb.append(", loading resource: ").append(url.getResource("org/netbeans/updater/XMLUtil.class"));
                sb.append(", loading class: ").append(url.loadClass("org.netbeans.updater.XMLUtil"));
            } catch (Throwable ex) {
                sb.append(", exception: ").append(ex.getMessage());
            }
        }
        return  sb.toString();
    }
    
    private static enum ELEMENTS {
        module_updates, module_group, notification, content_description, module, description,
        module_notification, external_package, manifest, l10n, license, message_digest
    }
    
    private static final String MODULE_UPDATES_ATTR_TIMESTAMP = "timestamp"; // NOI18N
    
    private static final String MODULE_GROUP_ATTR_NAME = "name"; // NOI18N
    
    private static final String NOTIFICATION_ATTR_URL = "url"; // NOI18N
    private static final String CONTENT_DESCRIPTION_ATTR_URL = "url"; // NOI18N
    
    private static final String LICENSE_ATTR_NAME = "name"; // NOI18N
    
    private static final String MODULE_ATTR_CODE_NAME_BASE = "codenamebase"; // NOI18N
    private static final String MODULE_ATTR_HOMEPAGE = "homepage"; // NOI18N
    private static final String MODULE_ATTR_DISTRIBUTION = "distribution"; // NOI18N
    private static final String MODULE_ATTR_DOWNLOAD_SIZE = "downloadsize"; // NOI18N
    private static final String MODULE_ATTR_NEEDS_RESTART = "needsrestart"; // NOI18N
    private static final String MODULE_ATTR_MODULE_AUTHOR = "moduleauthor"; // NOI18N
    private static final String MODULE_ATTR_RELEASE_DATE = "releasedate"; // NOI18N
    private static final String MODULE_ATTR_IS_GLOBAL = "global"; // NOI18N
    private static final String MODULE_ATTR_IS_PREFERRED_UPDATE = "preferredupdate";
    private static final String MODULE_ATTR_TARGET_CLUSTER = "targetcluster"; // NOI18N
    private static final String MODULE_ATTR_EAGER = "eager"; // NOI18N
    private static final String MODULE_ATTR_AUTOLOAD = "autoload"; // NOI18N
    private static final String MODULE_ATTR_LICENSE = "license"; // NOI18N
    private static final String LICENSE_ATTR_URL = "url"; // NOI18N
    
    private static final String MANIFEST_ATTR_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"; // NOI18N
    private static final String MANIFEST_ATTR_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host"; // NOI18N
    
    private static final String TIME_STAMP_FORMAT = "ss/mm/hh/dd/MM/yyyy"; // NOI18N
    
    private static final String L10N_ATTR_LOCALE = "langcode"; // NOI18N
    private static final String L10N_ATTR_BRANDING = "brandingcode"; // NOI18N
    private static final String L10N_ATTR_MODULE_SPECIFICATION = "module_spec_version"; // NOI18N
    private static final String L10N_ATTR_MODULE_MAJOR_VERSION = "module_major_version"; // NOI18N
    private static final String L10N_ATTR_LOCALIZED_MODULE_NAME = "OpenIDE-Module-Name"; // NOI18N
    private static final String L10N_ATTR_LOCALIZED_MODULE_DESCRIPTION = "OpenIDE-Module-Long-Description"; // NOI18N

    private static final String MESSAGE_DIGEST_ATTR_ALGORITHM = "algorithm"; // NOI18N
    private static final String MESSAGE_DIGEST_ATTR_VALUE = "value"; // NOI18N

    private static String GZIP_EXTENSION = ".gz"; // NOI18N
    private static String XML_EXTENSION = ".xml"; // NOI18N
    private static String GZIP_MIME_TYPE = "application/x-gzip"; // NOI18N
    
    public synchronized static Map<String, UpdateItem> getUpdateItems (URL url, AutoupdateCatalogProvider provider) throws IOException {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        URI base;
        try {
            if (provider != null) {
                base = provider.getUpdateCenterURL().toURI();
            } else {
                base = url.toURI();
            }
            InputSource is = null;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(true);
                SAXParser saxParser = factory.newSAXParser();
                is = getInputSource(url, provider, base);
                saxParser.parse(is, new AutoupdateCatalogParser(items, provider, base));
            } catch (Exception ex) {
                throw new IOException("Failed to parse " + base, ex);
            } finally {
                if (is != null && is.getByteStream() != null) {
                    try {
                        is.getByteStream().close();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (URISyntaxException ex) {
            ERR.log(Level.INFO, null, ex);
        }
        return items;
    }
    
    private static boolean isGzip (AutoupdateCatalogProvider p) {
        boolean res = false;
        if (p != null) {
            URL url = p.getUpdateCenterURL ();
            if (url != null) {
                String path = url.getPath ().toLowerCase ();
                res = path.endsWith (GZIP_EXTENSION);
                if (! res) {
                    boolean isXML = path.endsWith (XML_EXTENSION);
                    if (! isXML) {
                        try {
                            URLConnection conn = url.openConnection();
                            String contentType = conn.getContentType();
                            res = GZIP_MIME_TYPE.equals(contentType);
                        } catch (IOException ex) {
                            ERR.log (Level.INFO, "Cannot read Content-Type HTTP header, using file extension, cause: ", ex);
                        }
                    }
                }
                ERR.log (Level.FINER, "Is GZIP " + url + " ? " + res);
            } else {
                ERR.log (Level.WARNING, "AutoupdateCatalogProvider has not URL.");
            }
        }
        return res;
    }
    
    private static InputSource getInputSource(URL toParse, AutoupdateCatalogProvider p, URI base) {
        InputStream is = null;
        try {            
            is = toParse.openStream ();
            if (isGzip (p)) {
                try {
                    is = new GZIPInputStream(is);
                } catch (IOException e) {
                    ERR.log (Level.INFO,
                            "The file at " + toParse +
                            ", corresponding to the catalog at " + p.getUpdateCenterURL() +
                            ", does not look like the gzip file, trying to parse it as the pure xml" , e);
                    //#150034
                    // Sometimes the .xml.gz file is downloaded as the pure .xml file due to the strange content-encoding processing
                    is.close();
                    is = toParse.openStream();
                }
            }
            InputSource src = new InputSource(new BufferedInputStream (is));
            src.setSystemId(base.toString());
            return src;
        } catch (IOException ex) {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            ERR.log (Level.SEVERE, "Cannot estabilish input stream for {0}", toParse);
            ERR.log (Level.INFO, "Parsing exception", ex);
            return new InputSource();
        }
    }
    
    private Stack<String> currentGroup = new Stack<String> ();
    private String catalogDate;
    private Stack<ModuleDescriptor> currentModule = new Stack<ModuleDescriptor> ();
    private Stack<Map <String,String>> currentLicense = new Stack<Map <String,String>> ();
    private Stack<String> currentNotificationUrl = new Stack<String> ();
    private Stack<String> currentContentDescriptionUrl = new Stack<String>();
    private Map<String, UpdateLicenseImpl> name2license = new HashMap<String, UpdateLicenseImpl> ();
    private List<String> lines = new ArrayList<String> ();
    private int bufferInitSize = 0;

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
        lines.add (new String(ch, start, length));
        bufferInitSize += length;
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
        switch (ELEMENTS.valueOf (qName)) {
            case module_updates :
                break;
            case module_group :
                assert ! currentGroup.empty () : "Premature end of module_group " + qName;
                currentGroup.pop ();
                break;
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
            case notification :
                // write catalog notification
                if (this.provider != null && ! lines.isEmpty ()) {
                    StringBuilder sb = new StringBuilder (bufferInitSize);
                    for (String line : lines) {
                        sb.append (line);
                    }
                    String notification = sb.toString ();
                    String notificationUrl = currentNotificationUrl.peek ();
                    if (notificationUrl != null && notificationUrl.length () > 0) {
                        notification += (notification.length () > 0 ? "<br>" : "") + // NOI18N
                                "<a name=\"autoupdate_catalog_parser\" href=\"" + notificationUrl + "\">" + notificationUrl + "</a>"; // NOI18N
                    } else {
                        notification += (notification.length () > 0 ? "<br>" : "") +
                                "<a name=\"autoupdate_catalog_parser\"/>"; // NOI18N
                    }
                    provider.setNotification (notification);
                }
                currentNotificationUrl.pop ();
                break;
            case content_description :
                // write content description
                if (this.provider != null && ! lines.isEmpty ()) {
                    StringBuilder sb = new StringBuilder (bufferInitSize);
                    for (String line : lines) {
                        sb.append (line);
                    }
                    String contentDescription = sb.toString ();
                    String contentDescriptionUrl = currentContentDescriptionUrl.peek ();
                    if (contentDescriptionUrl != null && contentDescriptionUrl.length () > 0) {
                        contentDescription = "<a name=\"update_center_content_description\" href=\"" + // NOI18N
                                contentDescriptionUrl + "\">" + contentDescription + "</a>"; // NOI18N
                    }
                    provider.setContentDescription(contentDescription);
                }
                currentContentDescriptionUrl.pop();
                break;
            case module_notification :
                // write module notification
                if (! lines.isEmpty ()) {
                    ModuleDescriptor md = currentModule.peek ();
                    assert md != null : "ModuleDescriptor found for " + provider;
                    StringBuilder buf = new StringBuilder (bufferInitSize);
                    for (String line : lines) {
                        buf.append (line);
                    }
                    md.appendNotification (buf.toString ());
                }
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                assert ! currentLicense.empty () : "Premature end of license " + qName;
                Map <String, String> curLic = currentLicense.peek ();
                String licenseName = curLic.keySet().iterator().next();
                Collection<String> values = curLic.values();
                String licenseUrl = (values.size() > 0) ? values.iterator().next() : null;
                UpdateLicenseImpl updateLicenseImpl = this.name2license.get (licenseName);
                if (updateLicenseImpl == null) {
                    ERR.info("Unpaired license " + licenseName + " without any module.");
                } else {
                    if (!lines.isEmpty()) {
                        // find and fill UpdateLicenseImpl
                        StringBuilder sb = new StringBuilder(bufferInitSize);
                        for (String line : lines) {
                            sb.append(line);
                        }
                        updateLicenseImpl.setAgreement(sb.toString());
                    } else if (licenseUrl != null) {
                        updateLicenseImpl.setUrl(getDistribution(licenseUrl, baseUri));
                    }
                }
                
                currentLicense.pop ();
                break;
            case message_digest:
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }

    @Override
    public void endDocument () throws SAXException {
        ERR.fine ("End parsing " + (provider == null ? "" : provider.getUpdateCenterURL ()) + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startDocument () throws SAXException {
        ERR.fine ("Start parsing " + (provider == null ? "" : provider.getUpdateCenterURL ()) + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lines.clear();
        bufferInitSize = 0;
        final ELEMENTS elem;
        try {
            elem = ELEMENTS.valueOf (qName);
        } catch (IllegalArgumentException ex) {
            throw new SAXException("Wrong element " + qName); // NOI18N
        }
        switch (elem) {
            case module_updates :
                try {
                    catalogDate = "";
                    DateFormat format = new SimpleDateFormat (TIME_STAMP_FORMAT);
                    String timeStamp = attributes.getValue (MODULE_UPDATES_ATTR_TIMESTAMP);
                    if (timeStamp == null) {
                        ERR.info ("No timestamp is presented in " + (this.provider == null ? "" : this.provider.getUpdateCenterURL ()));
                    } else {
                        catalogDate = Utilities.formatDate (format.parse (timeStamp));
                        ERR.finer ("Successfully read time " + timeStamp); // NOI18N
                    }
                } catch (ParseException pe) {
                    ERR.log (Level.INFO, null, pe);
                }
                break;
            case module_group :
                currentGroup.push (attributes.getValue (MODULE_GROUP_ATTR_NAME));
                break;
            case module :
                ModuleDescriptor md = ModuleDescriptor.getModuleDescriptor (
                        currentGroup.size () > 0 ? currentGroup.peek () : null, /* group */
                        baseUri, /* base URI */
                        this.catalogDate); /* catalog date */
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
                String licName = impl.getUpdateLicenseImpl ().getName ();
                if (this.name2license.keySet ().contains (licName)) {
                    impl.setUpdateLicenseImpl (this.name2license.get (licName));
                } else {
                    this.name2license.put (impl.getUpdateLicenseImpl ().getName (), impl.getUpdateLicenseImpl ());
                }
                
                // put module into UpdateItems
                items.put (desc.getId (), m);
                
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case module_notification :
                break;
            case notification :
                currentNotificationUrl.push (attributes.getValue (NOTIFICATION_ATTR_URL));
                break;
            case content_description :
                currentContentDescriptionUrl.push(attributes.getValue (CONTENT_DESCRIPTION_ATTR_URL));
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                Map <String, String> map = new HashMap<String,String> ();
                map.put(attributes.getValue (LICENSE_ATTR_NAME), attributes.getValue (LICENSE_ATTR_URL));
                currentLicense.push (map);
                break;
            case message_digest:
                ModuleDescriptor desc2 = currentModule.peek ();
                // At this point the manifest element must have been seen
                UpdateItem ui = items.get (desc2.getId ());
                UpdateItemImpl uiImpl = Trampoline.SPI.impl (ui);
                uiImpl.getMessageDigests().add(new MessageDigestValue(
                    attributes.getValue(MESSAGE_DIGEST_ATTR_ALGORITHM),
                    attributes.getValue(MESSAGE_DIGEST_ATTR_VALUE)
                ));
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        parseError(e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        parseError(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        parseError(e);
    }

    private void parseError(SAXParseException e) {
        ERR.warning(e.getSystemId() + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + ": " + e.getLocalizedMessage());
    }

    @Override
    public InputSource resolveEntity (String publicId, String systemId) throws IOException, SAXException {
        return entityResolver.resolveEntity (publicId, systemId);
    }
    
    private static class ModuleDescriptor {
        private String moduleCodeName;
        private URL distributionURL;
        private String targetcluster;
        private String homepage;
        private String downloadSize;
        private String author;
        private String publishDate;
        private String notification;

        private Boolean needsRestart;
        private Boolean isGlobal;
        private Boolean isEager;
        private Boolean isAutoload;
        private Boolean isPreferredUpdate;

        private String specVersion;
        private Manifest mf;
        
        private String id;

        private UpdateLicense lic;
        
        private String group;
        private URI base;
        private String catalogDate;
        
        private String fragmentHost;

        private List<MessageDigestValue> hashes;
        
        private static ModuleDescriptor md = null;
        
        private ModuleDescriptor () {}
        
        public static ModuleDescriptor getModuleDescriptor (String group, URI base, String catalogDate) {
            if (md == null) {
                md = new ModuleDescriptor ();
            }
            
            md.group = group;
            md.base = base;
            md.catalogDate = catalogDate;
            
            return md;
        }
        
        public void appendModuleAttributes (Attributes module) {
            moduleCodeName = module.getValue (MODULE_ATTR_CODE_NAME_BASE);
            distributionURL = getDistribution (module.getValue (MODULE_ATTR_DISTRIBUTION), base);
            targetcluster = module.getValue (MODULE_ATTR_TARGET_CLUSTER);
            homepage = module.getValue (MODULE_ATTR_HOMEPAGE);
            downloadSize = module.getValue (MODULE_ATTR_DOWNLOAD_SIZE);
            author = module.getValue (MODULE_ATTR_MODULE_AUTHOR);
            publishDate = module.getValue (MODULE_ATTR_RELEASE_DATE);
            if (publishDate == null || publishDate.length () == 0) {
                publishDate = catalogDate;
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
            
            if (isPreferredUpdate) {
                Utilities.writeFirstClassModule(moduleCodeName);
            }
                        
            String licName = module.getValue (MODULE_ATTR_LICENSE);
            lic = UpdateLicense.createUpdateLicense (licName, null);
        }
        
        public void appendManifest (Attributes manifest) {
            specVersion = manifest.getValue (MANIFEST_ATTR_SPECIFICATION_VERSION);
            fragmentHost = manifest.getValue(MANIFEST_ATTR_FRAGMENT_HOST);   
            if (fragmentHost != null && fragmentHost.isEmpty()) {
                fragmentHost = null;
            }
            mf = getManifest (manifest);
            id = moduleCodeName + '_' + specVersion; // NOI18N
        }
        
        public void appendNotification (String notification) {
            this.notification = notification;
        }
        
        public String getId () {
            return id;
        }
        
        public UpdateItem createUpdateItem () {
            UpdateItem res = UpdateItem.createModule (
                    moduleCodeName,
                    specVersion,
                    distributionURL,
                    author,
                    downloadSize,
                    homepage,
                    publishDate,
                    group,
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
            if (fragmentHost != null) {
                ((ModuleItem) impl).setFragmentHost (fragmentHost);
            }
            // clean-up ModuleDescriptor
            cleanUp ();
            
            return res;
        }
        
        public void cleanUp (){
            this.specVersion = null;
            this.mf = null;
            this.notification = null;
        }
    }
    
    private static URL getDistribution (String distribution, URI base) {
        URL retval = null;
        if (distribution != null && distribution.length () > 0) {
            try {
                URI distributionURI = new URI (distribution);
                if (! distributionURI.isAbsolute ()) {
                    if (base != null) {
                        distributionURI = base.resolve (distributionURI);
                    }
                }
                retval = distributionURI.toURL ();
            } catch (MalformedURLException ex) {
                ERR.log (Level.INFO, null, ex);
            } catch (URISyntaxException ex) {
                ERR.log (Level.INFO, null, ex);
            }
        }
        return retval;
    }

    private static Manifest getManifest (Attributes attrList) {
        Manifest mf = new Manifest ();
        java.util.jar.Attributes mfAttrs = mf.getMainAttributes ();

        for (int i = 0; i < attrList.getLength (); i++) {
            mfAttrs.put (new java.util.jar.Attributes.Name (attrList.getQName (i)), attrList.getValue (i));
        }
        return mf;
    }

}
