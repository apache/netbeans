/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.dtd.ReaderProvider;
import org.netbeans.modules.html.editor.lib.api.dtd.ReaderProviderFactory;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Generic an implementation based on the existing html.editor catalog support
 *
 * @author Marek Fukala
 */
public final class HtmlCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver  {

    private Logger LOGGER = Logger.getLogger(HtmlCatalog.class.getSimpleName());
    private Collection<ReaderProvider> providers = new ArrayList<ReaderProvider>();

    public HtmlCatalog() {
        Collection<? extends ReaderProviderFactory> factories = Lookup.getDefault().lookupAll(ReaderProviderFactory.class);
        for(ReaderProviderFactory factory : factories) {
            providers.addAll(factory.getProviders());
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            for(ReaderProvider provider : providers) {
                LOGGER.log(Level.FINE, "adding provider" + provider.toString() + ", public ids:");
                for(String publicId : provider.getIdentifiers()) {
                    LOGGER.log(Level.FINE, "\tadding provider" + publicId);
                }
            }
        }

    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Resolving entity [publicId: '" + publicId + "', systemId: '" + systemId + "']");
        }

        for(ReaderProvider provider : providers) {
            FileObject systemIdFile = provider.getSystemId(publicId);
            if(systemIdFile != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Got resource: " + systemIdFile.getPath());
                }
                return new InputSource(provider.getReaderForIdentifier(publicId, systemId));
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "No resource found for publicId: " + publicId);
        }
        
        return null;
    }

    @Override
    public Iterator getPublicIDs() {
        List<String> result = new ArrayList<String>();
        for (ReaderProvider each : providers){
               result.addAll(each.getIdentifiers());
        }
        return result.iterator();
    }

    @Override
    public String getSystemID(String publicId) {
        if (publicId == null){
            return null;
        }
        for(ReaderProvider provider : providers) {
            FileObject systemIdFile = provider.getSystemId(publicId);
            if(systemIdFile != null) {
                URL url = URLMapper.findURL(systemIdFile, URLMapper.INTERNAL);
                if(url != null) {
                    return url.toExternalForm();
                }
            }
        }

        return null;
    }

    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/html/catalog/resources/DDCatalog.gif"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (HtmlCatalog.class, "LBL_HtmlCatalog");
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (HtmlCatalog.class, "DESC_HtmlCatalog");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }


    @Override
    public void refresh() {
    }

    @Override
    public String resolveURI(String name) {
        return null;
    }

    @Override
    public String resolvePublic(String publicId) {
        return null;
    }

    @Override
    public void addCatalogListener(CatalogListener l) {
    }

    @Override
    public void removeCatalogListener(CatalogListener l) {
    }

}
