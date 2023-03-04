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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;

/**
 * Helper class to simplify reading from web metadata model (WebAppMetadata class).
 * @author Petr Slechta
 */
public class WebAppMetadataHelper {

    private WebAppMetadataHelper() {
    }

    public static List<ServletInfo> getServlets(MetadataModel<WebAppMetadata> mm)
            throws MetadataModelException, IOException
    {
        /* Fix for IZ#168634 -NullPointerException at org.netbeans.modules.web.project.WebAppMetadataHelper.getServlets 
         *
         * It is possible to have null metamodel. It could happen as result
         * of metamodel factory corner case ( f.e. null argument for factory ).
         * So I put here check for <code>mm</code>. Just for the case.  
         */
        if ( mm == null ){
            return Collections.emptyList();
        }
        
        return mm.runReadAction(new MetadataModelAction<WebAppMetadata, List<ServletInfo>>() {
            public List<ServletInfo> run(WebAppMetadata metadata) throws Exception {
                return metadata.getServlets();
            }
        });
    }

}
