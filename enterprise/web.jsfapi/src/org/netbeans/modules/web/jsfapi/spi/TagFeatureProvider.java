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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.openide.util.Lookup;

/**
 * Interface to get additional informations about JSF Tag.
 * @author marekfukala
 */
public interface TagFeatureProvider {

    /**
     * Gets collection of needed {@link TagFeature}s for {@link Tag} from specified {@link Library}.<br />
     * This method can be used to obtain additional informations about {@link Tag} provided by {@link TagFeatureProvider}s.
     * 
     * @param tag JSF {@link Tag} to process. Can be null.
     * @param library JSF {@link Library} processed tag is from. Can be null.
     * @param clazz Class of required {@link TagFeature}. Can be null.
     * @return not null collection of needed {@link TagFeature}s for {@link Tag} from specified {@link Library}. 
     */
    <T extends TagFeature> Collection<T> getFeatures(Tag tag, Library library, Class<T> clazz);

    static class Query {
        
        /**
         * Gets collection of needed {@link TagFeature}s for {@link Tag} from specified {@link Library}.<br />
         * This method can be used to obtain additional informations about {@link Tag} provided by {@link TagFeatureProvider}s.
         * 
         * @param tag JSF {@link Tag} to process. Can be null.
         * @param library JSF {@link Library} processed tag is from. Can be null.
         * @param clazz Class of required {@link TagFeature}. Can be null.
         * @return not null collection of needed {@link TagFeature}s for {@link Tag} from specified {@link Library}. 
         */
        public static <T extends TagFeature> Collection<T> getFeatures(Tag tag, Library library, Class<T> clazz) {
            Collection<? extends TagFeatureProvider> tagFeatureProviders = Lookup.getDefault().lookupAll(TagFeatureProvider.class);
            
            Collection<T> query = new ArrayList<T>();
            
            if (tagFeatureProviders == null || tag == null || library == null || clazz == null) {
                return query;
            }
            
            for (TagFeatureProvider tagFeatureProvider : tagFeatureProviders) {
                query.addAll(tagFeatureProvider.getFeatures(tag, library, clazz));
            }
            return query;
        }
    }

}
