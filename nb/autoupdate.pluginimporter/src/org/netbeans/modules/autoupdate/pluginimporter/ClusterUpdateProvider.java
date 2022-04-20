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

package org.netbeans.modules.autoupdate.pluginimporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.util.NbBundle;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.autoupdate.UpdateProvider.class)
public class ClusterUpdateProvider implements UpdateProvider {
    
    private static File cluster = null;
    private static Logger LOG = Logger.getLogger (ClusterUpdateProvider.class.getName ());
    private static final String ELEMENT_MODULE = "module"; // NOI18N

    public ClusterUpdateProvider () {}

    public static void attachCluster (File newCluster) {
        if (newCluster == null) {
            throw new IllegalArgumentException ("Cluster cannot be null!"); // NOI18N
        }
        cluster = newCluster;
    }

    public String getName () {
        return Installer.CODE_NAME;
    }

    public String getDisplayName () {
        if (cluster == null) { // netbeans.full.hack etc.
            return NbBundle.getMessage(ClusterUpdateProvider.class, "ClusterUpdateProvider_DisplayName_disabled");
        }
        return NbBundle.getMessage (ClusterUpdateProvider.class, "ClusterUpdateProvider_DisplayName", cluster); // NOI18N
    }

    public String getDescription () {
        return NbBundle.getMessage (ClusterUpdateProvider.class, "ClusterUpdateProvider_Description"); // NOI18N
    }

    public CATEGORY getCategory () {
        return UpdateUnitProvider.CATEGORY.STANDARD;
    }

    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        for (File cf: readModules (cluster)) {
            String cnb = (cf.getName ().substring (0, cf.getName ().length () - ".xml".length ())).replace ("-", "."); // NOI18N
            Map<String, String> attr = new HashMap<String, String> (7);
            readConfigFile (cf, attr);
            String jarName = attr.get ("jar");
            if(jarName == null) {
                LOG.info ("Can`t get jar file name for " + cnb + ", skip checking.");
                continue;
            }
            File jarFile = new File (cluster, jarName); // NOI18N
            if (! jarFile.exists ()) {
                LOG.info ("Jar file " + jarFile + " doesn't exists. Skip checking " + cnb);
                continue;
            }
            File updateTrackingFile = new File(cluster, "update_tracking" + File.separator + cf.getName());
            if (! updateTrackingFile.exists ()) {
                LOG.info ("Update tracking file " + updateTrackingFile + " doesn't exists. Skip checking " + cnb);
                continue;
            }

            Manifest mf = new JarFile (jarFile).getManifest ();
            UpdateItem item = UpdateItem.createModule (
                cnb,
                attr.get ("specversion"), // NOI18N
                null,
                cluster.getName (), // XXX: to identify such items later
                "0", // NOI18N
                "",
                "",
                "",
                mf,
                Boolean.valueOf (attr.get ("eager")), // NOI18N
                Boolean.valueOf (attr.get ("autoload")), // NOI18N
                null,
                null,
                "",
                UpdateLicense.createUpdateLicense ("unknown-license", "none")); // NOI18N
            res.put (cnb + '_' + attr.get ("specversion"), item); // NOI18N
        }
        return res;
    }

    public boolean refresh (boolean force) throws IOException {
        return true;
    }
    
    private static Collection<File> readModules (File cluster) {
        if (cluster == null || ! cluster.exists ()) {
            return Collections.emptySet ();
        }
        Collection<File> res = new HashSet<File> ();
        File config = new File (new File (cluster, "config"), "Modules"); // NOI18N
        if (config.listFiles () == null) {
            return Collections.emptySet ();
        }
        for (File cf : config.listFiles ()) {
            if(cf.getName ().endsWith(".xml_hidden")) {
                //158204
                continue;
            }
            
            if (cf.getName ().endsWith (".xml")) { // NOI18N
                if(cf.length() > 0) {
                    res.add (cf);
                } else {
                    LOG.log(Level.INFO, "Found zero-sized xml file in config/Modules, ignoring: " + cf);
                }
            } else {
                LOG.log(Level.INFO, "Found non-xml file in config/Modules, ignoring: " + cf);
            }
        }
        return res;
    }

    private static void readConfigFile (File cf, Map<String, String> attr) {
        Document document = null;
        InputStream is = null;
        try {
            is = new BufferedInputStream (new FileInputStream (cf));
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, EntityCatalog.getDefault ());
        } catch (SAXException saxe) {
            LOG.log(Level.INFO, "Error while reading " + cf);
            LOG.log(Level.INFO, saxe.getLocalizedMessage (), saxe);
            return;
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "Error while reading " + cf);
            LOG.log(Level.WARNING, ioe.getLocalizedMessage (), ioe);
        } finally {
            if (is != null) {
                try {
                    is.close ();
                } catch (IOException e){
                    //ignore
                }
            }
        }

        assert document.getDocumentElement () != null : "File " + cf + " must contain document element.";
        Element element = document.getDocumentElement ();
        assert ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList children = element.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++) {
            Node n = children.item (i);
            if (Node.ELEMENT_NODE != n.getNodeType()) {
                continue;
            }
            Element e = (Element) n;
            String name = e.getAttributes ().getNamedItem ("name").getNodeValue (); // NOI18N
            String value = e.getChildNodes ().item (0).getNodeValue ();
            attr.put (name, value);
        }

    }

}
