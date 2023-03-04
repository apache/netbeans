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
package org.netbeans.modules.html.editor.lib.api.model;

import java.util.Collection;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.openide.util.Lookup;

/**
 * Creates a instance of {@link HtmlModel} for given {@link HtmlVersion}.
 * 
 * The factory gathers all instancies of {@link HtmlModelProvider} from global lookup and ask 
 * each of them for {@link HtmlModel}. If the provider returns null it proceeds with another
 * until a provider returns non-null {@link HtmlModel} 
 * 
 * @since 3.3
 * @author marekfukala
 */
public final class HtmlModelFactory {

    /**
     * Creates a instance of {@link HtmlModel} for given {@link HtmlVersion}.
     * 
     * @param version instance of {@link HtmlVersion}
     * @return instance of {@link HtmlModel} or null if none of the {@link HtmlModelProvider} provides
     * model for the given html version.
     */
    public static HtmlModel getModel(HtmlVersion version) {
        Collection<? extends HtmlModelProvider> providers = Lookup.getDefault().lookupAll(HtmlModelProvider.class);
        for(HtmlModelProvider provider : providers) {
            HtmlModel model = provider.getModel(version);
            if(model != null) {
                return model;
            }
        }
        return null;
        
    }
    
}
