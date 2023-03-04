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

package org.netbeans.modules.javahelp;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import org.openide.loaders.DataObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.help.HelpSet;
import org.openide.cookies.InstanceCookie;

import org.openide.loaders.Environment;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;

/** An XML processor for help set references.
 * Provides an instance of javax.swing.HelpSet.
 * @author Jesse Glick
 */
public final class HelpSetProcessor implements Environment.Provider {
    
    /** "context" for merge attribute on helpsets
     */    
    private static final String HELPSET_MERGE_CONTEXT = "OpenIDE"; // NOI18N
    
    /** attribute (type Boolean) on helpsets indicating
     * whether they should be merged into the master or
     * not; by default, true
     */    
    private static final String HELPSET_MERGE_ATTR = "mergeIntoMaster"; // NOI18N
    
    public @Override Lookup getEnvironment(final DataObject obj) {
        try {
            Class.forName("javax.help.HelpSet");
        } catch (ClassNotFoundException ex) {
            //JavaHelp not available, ignore:
            return Lookup.EMPTY;
        }
        Installer.log.log(Level.FINE, "creating help set from ref: {0}", obj.getPrimaryFile());
        return Lookups.singleton(new InstanceCookie() {
            public @Override String instanceName() {
                return obj.getName();
            }
            public @Override Class<?> instanceClass() throws IOException, ClassNotFoundException {
                return HelpSet.class;
            }
            public @Override Object instanceCreate() throws IOException, ClassNotFoundException {
                try {
                    Document doc = XMLUtil.parse(new InputSource(obj.getPrimaryFile().toURL().toString()), true, false, XMLUtil.defaultErrorHandler(), EntityCatalog.getDefault());
                    Element el = doc.getDocumentElement();
                    if (!el.getNodeName().equals("helpsetref")) { // NOI18N
                        throw new IOException();
                    }
                    String url = el.getAttribute("url"); // NOI18N
                    if (url == null || url.isEmpty()) {
                        throw new IOException("no url attr on <helpsetref>! doc.class=" + doc.getClass().getName() + " doc.documentElement=" + el);
                    }
                    String mergeS = el.getAttribute("merge"); // NOI18N
                    boolean merge = mergeS.isEmpty() || Boolean.valueOf(mergeS);
                    // Make sure nbdocs: protocol is ready:
                    Object ignore = NbDocsStreamHandler.class; // DO NOT DELETE THIS LINE
                    HelpSet hs = new HelpSet(Lookup.getDefault().lookup(ClassLoader.class), new URL(url));
                    hs.setKeyData(HELPSET_MERGE_CONTEXT, HELPSET_MERGE_ATTR, merge);
                    return hs;
                } catch (IOException x) {
                    throw x;
                } catch (Exception x) {
                    throw new IOException(x);
                }
            }
        });
    }
    
}

