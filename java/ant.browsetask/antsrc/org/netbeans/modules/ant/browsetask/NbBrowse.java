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

package org.netbeans.modules.ant.browsetask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.*;

import org.openide.awt.HtmlBrowser;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Opens a web browser.
 * @author Jesse Glick
 */
public class NbBrowse extends Task {

    private String url;
    public void setUrl(String s) {
        url = s;
    }

    private File file;
    public void setFile(File f) {
        file = f;
    }

    private File context;
    public void setContext(File f) {
        context = f;
    }

    private String urlPath;
    public void setUrlPath(String s) {
        urlPath = s;
    }

    public void execute() throws BuildException {
        if (url != null ^ file == null) throw new BuildException("You must define the url or file attributes", getLocation());
        if (url == null) {
            url = file.toURI().toString();
        }
        log("Browsing: " + url);
        try {
            URL u = new URL(url);
            URL appRoot = null;
            if (context != null) {
                FileObject fo = FileUtil.toFileObject(context);
                org.netbeans.api.project.Project p = null;
                if (fo != null) {
                    p = FileOwnerQuery.getOwner(fo);
                }
                if (urlPath != null && urlPath.length() > 0) {
                    if (!url.endsWith(urlPath)) {
                        throw new BuildException("The urlPath("+urlPath+") is not part of the url("+url+")", getLocation());
                    }
                    appRoot = new URL(url.substring(0, url.length()-urlPath.length()));
                }
                if (p != null) {
                    URLDisplayerImplementation urlDisplayer = (URLDisplayerImplementation)
                            p.getLookup().lookup(URLDisplayerImplementation.class);
                    if (urlDisplayer != null) {
                        urlDisplayer.showURL(appRoot != null ? appRoot : u, u, fo);
                        return;
                    }
                }
            }
            HtmlBrowser.URLDisplayer.getDefault().showURL(u);
        } catch (MalformedURLException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}
