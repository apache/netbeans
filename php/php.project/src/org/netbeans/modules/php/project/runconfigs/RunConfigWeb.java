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
package org.netbeans.modules.php.project.runconfigs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.php.api.util.StringUtils;

/**
 * Base class for all run configs.
 */
public abstract class RunConfigWeb<T extends RunConfigWeb<?>> extends BaseRunConfig<T> {

    protected String url;


    //~ Methods

    public String getUrlHint() {
        try {
            URL fullUrl = getFullUrl();
            if (fullUrl != null) {
                return fullUrl.toExternalForm();
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            // ignored
        }
        return null;
    }

    private URL getFullUrl() throws MalformedURLException, URISyntaxException {
        URL retval = null;
        if (StringUtils.hasText(url)) {
            retval = new URL(url);
        }
        if (retval != null && StringUtils.hasText(indexRelativePath)) {
            String projectUrl = retval.toExternalForm();
            if (!projectUrl.endsWith("/")) { // NOI18N
                projectUrl += "/"; // NOI18N
            }
            retval = new URL(projectUrl + indexRelativePath);
        }
        if (retval != null && StringUtils.hasText(arguments)) {
            retval = new URI(retval.getProtocol(), retval.getUserInfo(), retval.getHost(), retval.getPort(),
                    retval.getPath(), arguments, retval.getRef()).toURL();
        }
        if (retval != null) {
            return retval;
        }
        return null;

    }

    //~ Getters & Setters

    public String getUrl() {
        return url;
    }

    @SuppressWarnings("unchecked")
    public T setUrl(String url) {
        this.url = url;
        return (T) this;
    }

}
