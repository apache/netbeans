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

package org.netbeans.modules.editor.impl;

import java.awt.Container;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.lib2.URLMapper;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 *
 * @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.lib2.URLMapper.class)
public final class NbURLMapper extends URLMapper {

    private static final Logger LOG = Logger.getLogger(NbURLMapper.class.getName());
    
    public NbURLMapper() {
    }

    protected JTextComponent getTextComponent(URL url) {
        FileObject f = org.openide.filesystems.URLMapper.findFileObject(url);
        
        if (f != null) {
            DataObject d = null;
            
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, "Can't get DataObject for " + f, e); //NOI18N
            }
            
            if (d != null) {
                EditorCookie cookie = d.getLookup().lookup(EditorCookie.class);
                if (cookie != null) {
                    JEditorPane [] allJeps = cookie.getOpenedPanes();
                    if (allJeps != null) {
                        return allJeps[0];
                    }
                }
            }
        }
        
        return null;
    }

    protected URL getUrl(JTextComponent comp) {
        FileObject f = null;
        
        if (comp instanceof Lookup.Provider) {
            f = ((Lookup.Provider) comp).getLookup().lookup(FileObject.class);
        }
        
        if (f == null) {
            Container container = comp.getParent();
            while (container != null) {
                if (container instanceof Lookup.Provider) {
                    f = ((Lookup.Provider) container).getLookup().lookup(FileObject.class);
                    if (f != null) {
                        break;
                    }
                }
                
                container = container.getParent();
            }
        }
        
        if (f != null) {
            return f.toURL();
        }
        
        return null;
    }

}
