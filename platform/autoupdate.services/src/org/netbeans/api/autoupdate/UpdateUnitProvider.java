/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.UpdateUnitProviderImpl;
import org.netbeans.spi.autoupdate.*;


/**<code>UpdateUnitProvider</code> providers <code>UpdateUnit</code>. The units
 * are build of the of SPI <code>UpdateProvider</code>.
 *
 * @see UpdateProvider
 * @author Jiri Rechtacek
 */
public final class UpdateUnitProvider {
    public static enum CATEGORY {
        STANDARD,
        COMMUNITY,
        BETA
    }
    
    UpdateUnitProviderImpl impl;
    
    UpdateUnitProvider (UpdateUnitProviderImpl impl) {
        this.impl = impl;
    }
    
    /** Name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * 
     * @return name of provider
     */
    public String getName () {
        return impl.getName ();
    }
    
    /** Display name of provider. This display name can be visualized in UI.
     * 
     * @return display name of provider
     */
    public String getDisplayName () {
        return impl.getDisplayName ();
    }
    
    /** Sets the display name of the provider. This name can be presented 
     * to users in UI.
     * 
     * @param name 
     */
    public void setDisplayName (String name) {
        impl.setDisplayName (name);
    }
    
    /** Description of provider. This description can be visualized in UI.
     * 
     * @return description of provider or null
     */
    public String getDescription () {
        return impl.getDescription ();
    }


    /** @deprecated Use {@link #getSourceIcon()} and {@link #getSourceDescription()}.
     */
    @Deprecated
    public CATEGORY getCategory() {
        return impl.getCategory();
    }
    
    /** The icon associated with this provider. In case no specific icon is 
     * associated, a general one is returned.
     * 
     * @since 1.23
     * @see UpdateElement#getSourceIcon() 
     */
    public Image getSourceIcon() {
        return impl.getSourceIcon();
    }

    /** The description of this provider. Usually associated with {@link #getSourceIcon()}.
     * In case no special description is found, a general one is returned.
     * 
     * @since 1.23
     * @return textual description of the provider
     * @see UpdateElement#getSourceDescription() 
     */
    public String getSourceDescription() {
        return impl.getSourceDescription();
    }

    /** A description of content staging by this provider. The description might contains
     * HTML tags e.g. HTML Links.
     * 
     * @return textual description of content or <code>null</code>
     * @since 1.33
     */
    public String getContentDescription() {
        return impl.getContentDescription();
    }

    /** It's special support for <code>UpdateProvider</code> based on Autoupdate Catalog.
     * It's most kind of Update Providers and have a special support in UI.
     * 
     * @return URL of provider URL or null if and only if the UpdateProvider doesn't based of Autoupdate Catalog
     */
    public URL getProviderURL () {
        return impl.getProviderURL ();
    }
    
    /** Modified URL of URL-based provider. If the UpdateProvider doesn't support URL then the method has no affect.
     * 
     * @param url new URL
     */
    public void setProviderURL (URL url) {
        impl.setProviderURL (url);
    }
    
    /** Returns <code>java.util.List</code> of <code>UpdateUnit</code> build of the content of the
     * provider.
     * 
     * @return list of UpdateUnit
     */
    public List<UpdateUnit> getUpdateUnits () {
        return impl.getUpdateUnits ();
    }
    
    /** Returns <code>java.util.List</code> of <code>UpdateUnit</code> build of the content of the
     * provider.
     * 
     * @param types returns <code>UpdateUnit</code>s contain only given types, e.g. modules for <code>MODULE</code> type.
     * If types is <code>null</code> or null then returns default types
     * @return list of UpdateUnit
     */
    public List<UpdateUnit> getUpdateUnits (TYPE... types) {
        return impl.getUpdateUnits (types);
    }
    
    /** Make refresh of content of the provider. The content can be read from
     * a cache. The <code>force</code> parameter forces reading content from
     * remote server.
     * 
     * @param handle started ProgressHandle or null
     * @param force if true then forces to reread the content from server
     * @return true if refresh succeed
     * @throws java.io.IOException when any network problem appreared
     */
    public boolean refresh (ProgressHandle handle, boolean force) throws IOException {
        return impl.refresh (handle, force);
    }
    
    /** Returns <code>true</code> if the provider is automatically checked and its <code>UpdateUnit</code> are
     * returned from <code>UpdateManager</code>
     * 
     * @return enable flag
     */
    public boolean isEnabled () {
        return impl.isEnabled ();
    }
    
    /** Sets the enable flag.
     * 
     * @see #isEnabled
     * @param state 
     */
    public void setEnable (boolean state) {
        impl.setEnable (state);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + impl + "]";
    }

    /** Sets the trusted flag.
     *
     * @see #isTrusted
     * @param trusted
     * @since 1.62
     */
    public void setTrusted (boolean trusted) {
        impl.setTrusted(trusted);
    }

    /** Returns <code>true</code> if the provider is considered to be trusted.
     * If {@link UpdateItem} are provided by a trusted {@code UpdateUnitProvider}
     * and can be linked to it (for example because the provider provides
     * checksums in the catalog), they are considered trusted by the Autoupdate
     * mechanism and will not trigger certificate warnings.
     *
     * @return trusted flag
     * @since 1.62
     */
    public boolean isTrusted () {
        return impl.isTrusted();
    }
}
