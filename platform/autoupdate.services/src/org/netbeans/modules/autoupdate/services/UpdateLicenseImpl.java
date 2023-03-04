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

package org.netbeans.modules.autoupdate.services;

import java.net.URL;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogCache;

/**
 *
 * @author Jiri Rechtacek
 * @author Dmitry Lipin
 */
public final class UpdateLicenseImpl {
    private String name;
    private URL url;
    
    /** Creates a new instance of UpdateLicense */
    public UpdateLicenseImpl (String licenseName, String agreement) {
        this.name = licenseName;
        setAgreement(agreement);
    }
    /** Creates a new instance of UpdateLicense */
    public UpdateLicenseImpl (String licenseName, String agreement, URL url) {
        this.name = licenseName;
        this.url = url;
        setAgreement(agreement);
    }
    
    public String getName () {
        return name;
    }
    
    public URL getURL() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getAgreement () {
        return (name == null) ? null :
            AutoupdateCatalogCache.getDefault().getLicense(name,url);
    }
    
    public void setAgreement (String content) {
        if(content!=null && name!=null) {
            AutoupdateCatalogCache.getDefault().storeLicense(name,content);
        }
    }    
}
