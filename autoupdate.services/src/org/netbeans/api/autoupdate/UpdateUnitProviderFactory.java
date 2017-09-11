/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * @param onlyEnabled 
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
