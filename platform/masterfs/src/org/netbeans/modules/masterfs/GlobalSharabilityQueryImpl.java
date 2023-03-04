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

package org.netbeans.modules.masterfs;

import java.net.URI;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides implementation of <code>SharabilityQueryImplementation</code> that
 * is tightly coupled with <code>GlobalVisibilityQueryImpl</code> which is based on regular
 * expression provided by users via  property in IDESettings with property name 
 * IDESettings.PROP_IGNORED_FILES in Tools/Options.  
 *
 * Invisible files are considered as not shared. 
 *
 * @author Radek Matous
 */
@ServiceProvider(service=SharabilityQueryImplementation2.class, position=0)
public class GlobalSharabilityQueryImpl implements SharabilityQueryImplementation2 {
    private GlobalVisibilityQueryImpl visibilityQuery;

    @Override public SharabilityQuery.Sharability getSharability(URI uri) {
        if (visibilityQuery == null) {
            visibilityQuery = Lookup.getDefault().lookup(GlobalVisibilityQueryImpl.class);
            assert visibilityQuery != null;
        }
        return (visibilityQuery.isVisible(uri.toString().replaceFirst(".+/", ""))) ? SharabilityQuery.Sharability.UNKNOWN : SharabilityQuery.Sharability.NOT_SHARABLE;
    }    
}
