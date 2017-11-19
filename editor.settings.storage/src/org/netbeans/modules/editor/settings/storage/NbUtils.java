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

package org.netbeans.modules.editor.settings.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * This class contains support static methods for loading / saving and 
 * translating coloring (fontsColors.xml) files. It calls XMLStorage utilities.
 *
 * @author Jan Jancura
 */
public class NbUtils {
    
    private static final Logger LOG = Logger.getLogger(NbUtils.class.getName());
    
    /**
     * Creates unmodifiable copy of the original map converting <code>AttributeSet</code>s
     * to their immutable versions.
     */
    public static Map<String, AttributeSet> immutize(Map<String, ? extends AttributeSet> map, Object... filterOutKeys) {
        Map<String, AttributeSet> immutizedMap = new HashMap<String, AttributeSet>();
        
        for(String name : map.keySet()) {
            AttributeSet attribs = map.get(name);
            
            if (filterOutKeys.length == 0) {
                immutizedMap.put(name, AttributesUtilities.createImmutable(attribs));
            } else {
                List<Object> pairs = new ArrayList<Object>();

                // filter out attributes specified by filterOutKeys
                first:
                for(Enumeration<? extends Object> keys = attribs.getAttributeNames(); keys.hasMoreElements(); ) {
                    Object key = keys.nextElement();
                    
                    for(Object filterOutKey : filterOutKeys) {
                        if (Utilities.compareObjects(key, filterOutKey)) {
                            continue first;
                        }
                    }
                    
                    pairs.add(key);
                    pairs.add(attribs.getAttribute(key));
                }

                immutizedMap.put(name, AttributesUtilities.createImmutable(pairs.toArray()));
            }
        }
        
        return Collections.unmodifiableMap(immutizedMap);
    }
    
    public static Map<String, AttributeSet> immutize(Collection<AttributeSet> set) {
        Map<String, AttributeSet> immutizedMap = new HashMap<String, AttributeSet>();
    
        for(AttributeSet as : set) {
            Object nameObject = as.getAttribute(StyleConstants.NameAttribute);
            if (nameObject instanceof String) {
                immutizedMap.put((String) nameObject, as);
            } else {
                LOG.warning("Ignoring AttributeSet with invalid StyleConstants.NameAttribute. AttributeSet: " + as); //NOI18N
            }
        }
            
        return Collections.unmodifiableMap(immutizedMap);
    }
    

    @ServiceProvider(service=EntityCatalog.class)
    public static final class NoNetworkAccessEntityCatalog extends EntityCatalog {
        private final boolean NO_NETWORK_ACCESS = Boolean.getBoolean("editor.storage.no.network.access");

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (!NO_NETWORK_ACCESS) return null;
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    }
}
