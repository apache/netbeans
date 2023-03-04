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
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.services.UpdateElementImpl;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;
import org.netbeans.modules.autoupdate.updateprovider.ProviderCategory;

/** Instances provided by the <code>UpdateUnit</code> which represents specific version
 * of update (e.g. module or feature). The <code>UpdateElement</code> can be installed,
 * available on a remote server or stored in backup.
 * 
 * @author Jiri Rechtacek (jrechtacek@netbeans.org)
 */
public final class UpdateElement {
    final UpdateElementImpl impl;
    
    UpdateElement (UpdateElementImpl elementImpl) {
        if (elementImpl == null) {
            throw new IllegalArgumentException ("UpdateElementImpl cannot be null while creating UpdateElement.");
        }
        this.impl = elementImpl;
    }
    
    /** Returns <code>UpdateUnit</code> where is this <code>UpdateElement</code> contained.
     * 
     * @return UpdateUnit in which belongs to
     */
    public UpdateUnit getUpdateUnit () {
        assert impl.getUpdateUnit () != null : "UpdateUnit for UpdateElement " + this + " is not null.";
        return impl.getUpdateUnit ();
    }
   
    /** Returns the code name of the update, sans release version.
     * 
     * @return code name of the update
     */
    public String getCodeName () {
        return impl.getCodeName ();
    }
    
    /** Returns the display name of the update, displaying in UI to end users.
     * 
     * @return display name
     */
    public String getDisplayName () {
        return impl.getDisplayName ();
    }
    
    /** Returns the specification version.
     * 
     * @return specification version or null
     */
    public String getSpecificationVersion () {
        return impl.getSpecificationVersion () == null ? null : impl.getSpecificationVersion ().toString ();
    }
    
    /** Returns if the <code>UpdateElement</code> is active in the system.
     * 
     * @return true of UpdateElement is active
     */
    public boolean isEnabled () {
        return impl.isEnabled ();
    }
    
    /** Returns the description of update, displaying in UI to end users.
     * 
     * @return description
     */
    public String getDescription () {
        return impl.getDescription ();
    }
    
    /** Returns the special notification text of update.
     * 
     * @return notification text or null
     */
    public String getNotification () {
        return impl.getNotification();
    }
    
    /** Returns name of <code>UpdateProvider</code>
     * 
     * @return name of UpdateProvider
     */
    public String getSource () {
        return impl.getSource ();
    }

    /**
     * @return <code>UpdateUnitProvider.CATEGORY</code> for a quality classification 
     * for update represented by this instance
     * @deprecated Use {@link #getSourceIcon()} and {@link #getSourceDescription()}.
     */
    @Deprecated
    public CATEGORY getSourceCategory () {
        UpdateUnitProvider provider = getUpdateUnitProvider();
        return (provider != null) ? provider.getCategory() : CATEGORY.COMMUNITY;
    }
    
    /** Provides an icon associated with the provider of this update element. 
     * @return icon representing the provider of this element
     * @since 1.23
     * @see UpdateUnitProvider#getSourceIcon() 
     */
    public Image getSourceIcon() {
        UpdateUnitProvider provider = getUpdateUnitProvider();
        return provider != null ? provider.getSourceIcon() : ProviderCategory.forValue(CATEGORY.COMMUNITY).getIcon();
    }
    
    /** Description of the provider of this element. 
     * @return textual description of the provider of this element
     * @since 1.23
     * @see UpdateUnitProvider#getSourceDescription()
     */
    public String getSourceDescription() {
        UpdateUnitProvider provider = getUpdateUnitProvider();
        return provider != null ? provider.getSourceDescription() : ProviderCategory.forValue(CATEGORY.COMMUNITY).getDisplayName();
    }
    
    private UpdateUnitProvider getUpdateUnitProvider() {
        return UpdateManagerImpl.getInstance().getUpdateUnitProvider(getSource());
    }
    
    /** Returns name of the author of the update element.
     * 
     * @return name or null
     */
    public String getAuthor () {
        return impl.getAuthor ();
    }
    
    /** Returns the <code>String</code> representation of <code>URL</code>.
     * 
     * @return String or null
     */
    public String getHomepage () {
        return impl.getHomepage ();
    }
    
    /** Returns size of <code>UpdateElement</code> in Bytes.
     * 
     * @return size
     */
    public int getDownloadSize () {
        return impl.getDownloadSize ();
    }
    
    /** Returns display name of category where <code>UpdateElement</code> belongs to.
     * 
     * @return name of category
     */
    public String getCategory () {
        return impl.getCategory ();
    }

    /** Returns date when <code>UpdateElement</code> was published or install time
     * if the <code>UpdateElement</code> is installed already. Can return null
     * if the date is unknown.
     * 
     * @return date in format "yyyy/MM/dd" or null
     */
    public String getDate () {
        return impl.getDate ();
    }

    /** Returns ID of license agreement if the <code>UpdateElement</code> has a copyright.
     * 
     * @return String or null
     * @since 1.33
     */
    public String getLicenseId () {                
        return impl.getLicenseId ();
    }
    
    /** Returns text of license agreement if the <code>UpdateElement</code> has a copyright.
     * 
     * @return String or null
     */
    public String getLicence () {                
        return impl.getLicence ();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateElement other = (UpdateElement) obj;

        if (this.impl != other.impl &&
            (this.impl == null || !this.impl.equals(other.impl)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 53 * hash + (this.impl != null ? this.impl.hashCode()
                                              : 0);
        return hash;
    }
    
    @Override
    public String toString () {
        return impl.getDisplayName() + "[" + impl.getCodeName () + "/" + impl.getSpecificationVersion () + "]";
    }

}

