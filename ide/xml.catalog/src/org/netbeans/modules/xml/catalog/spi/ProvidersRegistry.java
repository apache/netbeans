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
package org.netbeans.modules.xml.catalog.spi;

import java.util.*;
import java.io.IOException;

import org.openide.util.Lookup;

/**
 * A utility class representing the registry of SPI implementations.
 * It contains implementations classes.
 * <p>
 * It should be moved out of SPI package.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class ProvidersRegistry {

    /**
     * Queries Lookup for all registered catalog providers returning provided Classes.
     * @param  filter an array of SPI interfaces that must the catalog Class implement or <tt>null</tt>
     * @return Iterator<Class> of currently registered catalogs.
     */
    public static final synchronized Iterator getProviderClasses(Class[] filter) {

        Lookup.Template template = new Lookup.Template(CatalogProvider.class);
        Lookup.Result res = Lookup.getDefault().lookup(template);
        Iterator it = res.allInstances().iterator();
        Set set = new LinkedHashSet();

        while(it.hasNext()) {
            try {
                CatalogProvider next = (CatalogProvider) it.next();
                set.add(next.provideClass());
            } catch (ClassNotFoundException ex) {
                //ignore it
            } catch (IOException ex) {
                //ignore it
            }                                                                               
        }
        
        it = set.iterator();
        
        if (filter == null)
            return it;

        ArrayList list = new ArrayList();
                
try_next_provider_class:
        while (it.hasNext()) {
            Class next = (Class) it.next();
            
            // provider test
            
            for (int i=0; i<filter.length; i++) {
                
                if (filter[i].isAssignableFrom(next) == false)
                    continue try_next_provider_class;
            }
            
            // test passed
            
            list.add(next);
        }
        
        return list.iterator();
    }
          
}
