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

package org.netbeans.modules.xml.catalog;

import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.xml.catalog.spi.CatalogWriter;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * AddCatalogEntryAction.java
 *
 * Created on May 31, 2005
 * @author mkuchtiak
 */
public class AddCatalogEntryAction extends NodeAction {

    /** Creates a new instance of AddCatalogEntryAction */
    public AddCatalogEntryAction() {}

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        perform(activatedNodes);
    }

    public static void perform(org.openide.nodes.Node[] activatedNodes) {
        CatalogNode node = activatedNodes[0].getCookie(CatalogNode.class);
        CatalogWriter catalog = (CatalogWriter)node.getCatalogReader();
        CatalogEntryPanel panel = new CatalogEntryPanel();
        DialogDescriptor dd = new DialogDescriptor(panel,
                              NbBundle.getMessage(AddCatalogEntryAction.class, "TITLE_addCatalogEntry")); //NOI18N
        //dd.setHelpCtx(new HelpCtx(CatalogMounterPanel.class));
        panel.setEnclosingDesc(dd);
        java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dd.setValid(false);
        dialog.setVisible(true);
        if (dd.getValue().equals(DialogDescriptor.OK_OPTION)) {
            if (panel.isPublic()) {
                catalog.registerCatalogEntry("PUBLIC:"+panel.getPublicId(), panel.getUri()); //NOI18N
            } else if (isXmlSchema(panel.getSystemId(), panel.getUri())) {
                catalog.registerCatalogEntry("SCHEMA:"+panel.getSystemId(), panel.getUri()); //NOI18N
            } else {
                catalog.registerCatalogEntry("SYSTEM:"+panel.getSystemId(), panel.getUri()); //NOI18N
            }
        }
    }
    
    private static final String PROTOCOL_HTTP = "http://"; // NOI18N
    private static final String PROTOCOL_HTTPS = "https://"; // NOI18N
    private static final String PROTOCOL_FILE = "file:/"; // NOI18N
    private static final String MIME_SCHEMA = "application/x-schema+xml"; // NOI18N
    private static final String MIME_SCHEMA2 = "text/xsd+xml"; // NOI18N

    private static boolean isXmlSchema(String uri, String localURI) {
        // check the remote protocol, only http(s) is supported at the moment
        if (!(uri.startsWith(PROTOCOL_HTTP) || uri.startsWith(PROTOCOL_HTTPS))) {
            return false;
        }
        // check MIME type of the target:
        if (!localURI.startsWith(PROTOCOL_FILE)) {
            return false;
        }
        
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(localURI));
            if (fo == null) {
                return false;
            }
        } catch (MalformedURLException ex) {
            return false;
        }
        return MIME_SCHEMA.equals(fo.getMIMEType()) || MIME_SCHEMA2.equals(fo.getMIMEType());
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length>0)  {
            Object node = activatedNodes[0].getCookie(CatalogNode.class);
            if (node instanceof CatalogNode && ((CatalogNode)node).getCatalogReader() instanceof CatalogWriter) return true;
        }
        return false;
    }

    protected boolean asynchronous() {
        return false;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }

    public String getName() {
        return NbBundle.getMessage(AddCatalogEntryAction.class,"TXT_AddCatalogEntry");
    }
    
}
