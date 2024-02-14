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
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.spi.WebTargetModuleID;
import org.netbeans.modules.j2ee.weblogic9.URLWait;
import org.openide.util.RequestProcessor;

/**
 *
 * @author whd
 */
public class WLTargetModuleID implements WebTargetModuleID {

    private final RequestProcessor requestProcessor = new RequestProcessor(WLTargetModuleID.class);

    private final Target target;

    private final File dir;

    private String jarName;

    private String contextUrl;

    private List<WLTargetModuleID> children = new ArrayList<>();

    private TargetModuleID  parent;

    private List<URL> serverUrls = new ArrayList<URL>();

    public WLTargetModuleID(Target target) {
        this(target, "");
    }

    public WLTargetModuleID(Target target, String jarName) {
        this(target, jarName, null);
    }

    public WLTargetModuleID(Target target, String jarName, File dir) {
        this.target = target;
        this.dir = dir;
        this.setJarName(jarName);
    }

    public void setContextURL(String contextUrl) {
        this.contextUrl = contextUrl;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public void setParent(WLTargetModuleID parent) {
        this.parent = parent;
    }

    public synchronized void addChild(WLTargetModuleID child) {
        children.add(child);
        child.setParent(this);
    }

    public synchronized TargetModuleID[] getChildTargetModuleID(){
        return (TargetModuleID[]) children.toArray(new TargetModuleID[0]);
    }
    
    public synchronized void addUrl(URL url) {
        serverUrls.add(url);
    }

    @Override
    public URL resolveWebURL() {
        List<URL> candidates;
        synchronized (this) {
            candidates = new ArrayList<URL>(serverUrls);
        }
        String web = getWebURL();
        if (web != null) {
            try {
                candidates.add(new URL(web));
            } catch (MalformedURLException ex) {
                // just continue
            }
        }

        for (URL c : candidates) {
            if (URLWait.waitForUrlReady(null, requestProcessor, c, 1000)) {
                // use the first one if it became available as well
                if (URLWait.waitForUrlReady(null, requestProcessor, candidates.get(0), 500)) {
                    return candidates.get(0);
                }
                return c;
            }
        }
        return null;
    }

    public String getModuleID() {
        return jarName;
    }

    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }

    public Target getTarget() {
        return target;
    }

    public String getWebURL() {
        return contextUrl;
    }

    public File getDir() {
        return dir;
    }

    @Override
    public String toString() {
        // XXX this is used as map key in org.netbeans.modules.j2ee.deployment.impl.TargetServer
        // so it can't be freely changed
        return getModuleID();
    }
}
