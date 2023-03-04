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

package org.netbeans.modules.web.jsfapi.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * To be registered as a @Service into the global lookup
 *
 * @author marekfukala
 */
public interface InputTextTagValueProvider {

    /**
     *
     * @param fo
     * @return null if the provider is not interested in the file type, or an instance of Map
     */
    public Map<String, String> getInputTextValuesMap(FileObject fo);

    public static class Query {

        public static Map<String, String> getInputTextValuesMap(FileObject fo) {
            Collection<? extends InputTextTagValueProvider> all = Lookup.getDefault().lookupAll(InputTextTagValueProvider.class);
            Map<String, String> result = new HashMap<String, String>();
            for(InputTextTagValueProvider provider : all) {
                Map<String, String> map = provider.getInputTextValuesMap(fo);
                if(map != null) {
                    result.putAll(map);
                }
            }
            return result;
        }
        
    }

}
