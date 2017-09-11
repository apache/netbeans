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
