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

package org.netbeans.modules.palette;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Libor Kotouc
 */
public class PaletteEnvironmentProvider implements Environment.Provider {
    
    private static PaletteEnvironmentProvider createProvider() {
        return new PaletteEnvironmentProvider();
    }

    private PaletteEnvironmentProvider() {
    }

// ----------------   Environment.Provider ----------------------------    
    
    public Lookup getEnvironment(DataObject obj) {
        if (!FileUtil.isParentOf(
                    FileUtil.getConfigRoot(), obj.getPrimaryFile())) {
            return Lookup.EMPTY;
        }
        PaletteItemNodeFactory nodeFactory = new PaletteItemNodeFactory((XMLDataObject)obj);
        return nodeFactory.getLookup();
    }

    
    private static class PaletteItemNodeFactory implements InstanceContent.Convertor<Class,PaletteItemNode> {

//        private static final String URL_PREFIX_INSTANCES = "PaletteItems/";
        
        private XMLDataObject xmlDataObject = null;

        private Lookup lookup = null;
        
        Reference<PaletteItemNode> refNode = new WeakReference<PaletteItemNode>(null);

        PaletteItemNodeFactory(XMLDataObject obj) {

            xmlDataObject = obj;

            InstanceContent content = new InstanceContent();
            content.add(Node.class, this);

            lookup = new AbstractLookup(content);
        }
        
        Lookup getLookup() {
            return lookup;
        }
        
        // ----------------   InstanceContent.Convertor ----------------------------    

        public Class<? extends PaletteItemNode> type(Class obj) {
            if( obj == Node.class )
                return PaletteItemNode.class;
            return null;
        }

        public String id(Class obj) {
            return obj.toString();
        }

        public String displayName(Class obj) {
            return obj.getName();
        }

        public PaletteItemNode convert(Class obj) {
            PaletteItemNode o = null;
            if (obj == Node.class) {
                try {
                    o = getInstance();
                } catch (Exception ex) {
                    Logger.getLogger( getClass().getName() ).log( Level.INFO, null, ex );
                }
            }
           
            return o;
        }
        
        // ----------------   helper methods  ----------------------------    
        private WeakReference<XMLReader> cachedReader;
        private XMLReader getXMLReader() throws SAXException {
            XMLReader res = null == cachedReader ? null : cachedReader.get();
            if( null == res ) {
                res = XMLUtil.createXMLReader(true);
                res.setEntityResolver(EntityCatalog.getDefault());
                cachedReader = new WeakReference<XMLReader>(res);
            }
            return res;
        }
        
        public synchronized PaletteItemNode getInstance() {

            PaletteItemNode node = refNode.get();
            if (node != null)
                return node;

            FileObject file = xmlDataObject.getPrimaryFile();
            if (file.getSize() == 0L) // item file is empty
                return null;

            PaletteItemHandler handler = new PaletteItemHandler();
            try {
                XMLReader reader = getXMLReader();
                FileObject fo = xmlDataObject.getPrimaryFile();
                String urlString = fo.toURL().toExternalForm();
                InputSource is = new InputSource(fo.getInputStream());
                is.setSystemId(urlString);
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.parse(is);
            }
            catch (SAXException saxe) {
                Logger.getLogger( getClass().getName() ).log( Level.INFO, null, saxe );
                return null;
            } 
            catch (IOException ioe) {
                Logger.getLogger( getClass().getName() ).log( Level.INFO, null, ioe );
                return null;
            }
            if( handler.isError() )
                return null;

            node = createPaletteItemNode(handler);
            refNode = new WeakReference<PaletteItemNode>(node);

            return node;
        }

        private PaletteItemNode createPaletteItemNode(PaletteItemHandler handler) {

            String name = xmlDataObject.getName();
            
            InstanceContent ic = new InstanceContent();
            String s = handler.getClassName();
            if (s != null)
                ic.add(s, ActiveEditorDropProvider.getInstance());
            else {
                s = handler.getBody();
                if (s != null)
                    ic.add(s, ActiveEditorDropDefaultProvider.getInstance());
            }
            
            return (null == handler.getDisplayName())
                ? new PaletteItemNode(
                    new DataNode(xmlDataObject, Children.LEAF), 
                    name, 
                    handler.getBundleName(), 
                    handler.getDisplayNameKey(), 
                    handler.getClassName(), 
                    handler.getTooltipKey(), 
                    handler.getIcon16URL(), 
                    handler.getIcon32URL(), 
                    ic )
                : new PaletteItemNode(
                        new DataNode(xmlDataObject, Children.LEAF), 
                        name, 
                        handler.getDisplayName(), 
                        handler.getTooltip(), 
                        handler.getIcon16URL(), 
                        handler.getIcon32URL(), 
                        ic );
        }
    }        

}
