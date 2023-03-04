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

package org.netbeans.api.autoupdate;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.UpdateUnitProviderImpl;
import org.netbeans.modules.autoupdate.updateprovider.ProviderCategory;

/** The factory handles <code>UpdateUnitProvider</code>, allow to create or removed them,
 * browse the providers or refresh its content.
 *
 * @author Jiri Rechtacek
 */
public final class UpdateUnitProviderFactory {
    
    private static final UpdateUnitProviderFactory INSTANCE = new UpdateUnitProviderFactory ();
    
    /**
     * Creates a new instance of UpdateProviderFactory
     */
    private  UpdateUnitProviderFactory () {
    }
    
    /** Returns singleton instance of <code>UpdateUnitProviderFactory</code>
     * 
     * @return UpdateUnitProviderFactory singleton instance of UpdateUnitProviderFactory
     */
    public static UpdateUnitProviderFactory getDefault () {
        return INSTANCE;
    }
    
    /** Returns <code>java.util.List</code> of <code>UpdateUnitProvider</code>. The parameter
     * onlyEnabled specifies if only enabled provider should be returned or all.
     * 
     * @param onlyEnabled if only enabled provider should be returned or all
     * @return list of providers
     */
    public List<UpdateUnitProvider> getUpdateUnitProviders (boolean onlyEnabled) {
        return UpdateUnitProviderImpl.getUpdateUnitProviders (onlyEnabled);
    }

    /** Creates new <code>UpdateUnitProvider</code> and store its preferences. The new provider 
     * is based of the given URL where is the Autoupdate Catalog.
     * 
     * @param name name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * @param displayName display name of provider
     * @param url URL to Autoupdate Catalog
     * @param category quality classification for every <code>UpdateElement</code> 
     * comming from returned <code>UpdateUnitProvider</code>
     * @return URL-based UpdateUnitProvider
     */        
    public UpdateUnitProvider create(
        String name, String displayName, URL url, UpdateUnitProvider.CATEGORY category
    ) {
        return UpdateUnitProviderImpl.createUpdateUnitProvider (name, displayName, url, ProviderCategory.forValue(category));
    }

    /** 
     * @since 1.23
     */
    public UpdateUnitProvider create(
        String name, String displayName, URL url,
        String categoryIconBase, String categoryDisplayName
    ) {
        return UpdateUnitProviderImpl.createUpdateUnitProvider (name, displayName, url, ProviderCategory.create(categoryIconBase, categoryDisplayName));
    }

    /** Creates new <code>UpdateUnitProvider</code> and store its preferences. The new provider 
     * is based of the given URL where is the Autoupdate Catalog.
     * 
     * @param name name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * @param displayName display name of provider
     * @param url URL to Autoupdate Catalog
     * @return URL-based UpdateUnitProvider
     */    
    public UpdateUnitProvider create (String name, String displayName, URL url) {
        return UpdateUnitProviderImpl.createUpdateUnitProvider (name, displayName, url, ProviderCategory.forValue(UpdateUnitProvider.CATEGORY.COMMUNITY));
    }
    
    /** Creates new <code>UpdateUnitProvider</code> for temporary usage. This provider contains
     * content of given NBMs.
     * 
     * @param name name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * @param files NBM files
     * @return UpdateUnitProvider
     */
    public UpdateUnitProvider create (String name, File... files) {
        return UpdateUnitProviderImpl.createUpdateUnitProvider (name, files);
    }
    
    /** Removes the <code>UpdateUnitProvider</code> from the infrastucture.
     * 
     * @param unitProvider 
     */
    public void remove(UpdateUnitProvider unitProvider) {
        UpdateUnitProviderImpl.remove(unitProvider);
    }
    
    /** Re-read list of <code>UpdateUnitProvider</code> from infrastucture and refresh its content
     * if <code>force</code> parameter is <code>true</code>.
     * 
     * @param handle started ProgressHandle or null
     * @param force if true then <code>refresh(true)</code> is called on all <code>UpdateUnitProvider</code>
     * @throws java.io.IOException 
     */
    public void refreshProviders (ProgressHandle handle, boolean force) throws IOException {
        UpdateUnitProviderImpl.refreshProviders (handle, force);
    }
}
