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

package org.netbeans.spi.autoupdate;

import java.io.IOException;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;

/** <code>UpdateProvider</code> providers items for Autoupdate infrastructure. The items
 * are available on e.g. Update Center. Items can represents NetBeans Module,
 * its Localization, Feature as group of NetBeans Modules or special
 * components which needs own native installer to make them accessible in NetBeans product.
 * The infrastructure finds out <code>UpdateProvider</code> in <code>Lookup.getDefault()</code>,
 * the provider can be registring declaratively in XML layer.
 * Note: the former Autoupdate module allows declaration of former <code>AutoupdateType</code> on XML
 * layer, these declaration are read as new one UpdateProvider by reason of backward compatability.
 *
 * @author Jiri Rechtacek
 */
public interface UpdateProvider {
    
    /** Name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * 
     * @return name of provider
     */
    public String getName ();
    
    /** Display name of provider. This display name can be visualized in UI.
     * 
     * @return display name of provider
     */
    public String getDisplayName ();
    
    /** Description of provider. This description can be visualized in UI.
     * 
     * @return description of provider or null
     */
    public String getDescription ();


    /**
     * @return <code>UpdateUnitProvider.CATEGORY</code> for a quality classification 
     * of updates comming from this instance
     */    
    public CATEGORY getCategory();
    
    /** Returns <code>UpdateItem</code>s which is mapped to its unique ID.
     * Unique ID depends on the type of <code>UpdateItem</code>.
     * 
     * @see UpdateItem
     * @return Map of code name of UpdateItem and instance of UpdateItem
     * @throws java.io.IOException when any network problem appreared
     */
    public Map<String, UpdateItem> getUpdateItems () throws IOException;
    
    /** Make refresh of content of the provider. The content can by read from
     * a cache. The <code>force</code> parameter forces reading content from
     * remote server.
     * 
     * @param force if true then forces to reread the content from server
     * @return true if refresh succeed
     * @throws java.io.IOException when any network problem appreared
     */
    public boolean refresh (boolean force) throws IOException;
    
}
