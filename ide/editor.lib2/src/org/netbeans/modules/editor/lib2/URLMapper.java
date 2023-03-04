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

package org.netbeans.modules.editor.lib2;

import java.net.URL;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;

/**
 *
 * @author Vita Stejskal
 */
public abstract class URLMapper {

    public static JTextComponent findTextComponenet(URL url) {
        synchronized (LOCK) {
            Collection<? extends URLMapper> mappers = getMappers();
            for(URLMapper m : mappers) {
                JTextComponent jtc = m.getTextComponent(url);
                if (jtc != null) {
                    return jtc;
                }
            }
            return null;
        }
    }
    
    public static URL findUrl(JTextComponent component) {
        synchronized (LOCK) {
            Collection<? extends URLMapper> mappers = getMappers();
            for(URLMapper m : mappers) {
                URL url = m.getUrl(component);
                if (url != null) {
                    return url;
                }
            }
            return null;
        }
    }
    
    // ----------------------------------------------
    // Abstract URLMapper
    // ----------------------------------------------

    protected URLMapper() {
    }

    protected abstract JTextComponent getTextComponent(URL url);
        
    protected abstract URL getUrl(JTextComponent url);
    
    // ----------------------------------------------
    // Private implementation
    // ----------------------------------------------
    
    private static final String LOCK = new String("URLMapper.LOCK"); //NOI18N
    private static Lookup.Result<URLMapper> mappers = null;
    
    private static Collection<? extends URLMapper> getMappers() {
        if (mappers == null) {
            mappers = Lookup.getDefault().lookupResult(URLMapper.class);
        }
        return mappers.allInstances();
    }
}
