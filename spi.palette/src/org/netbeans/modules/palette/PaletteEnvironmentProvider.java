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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                String urlString = fo.getURL().toExternalForm();
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
