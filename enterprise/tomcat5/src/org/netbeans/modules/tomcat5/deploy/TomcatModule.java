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

package org.netbeans.modules.tomcat5.deploy;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/** Dummy implementation of target for Tomcat 5 server
 *
 * @author  Radim Kubacki
 */
public final class TomcatModule implements TargetModuleID {

    private static final Logger LOGGER = Logger.getLogger(TomcatModule.class.getName());

    private TomcatTarget target;

    private final String path;
    private final String docRoot;

    public TomcatModule (Target target, String path) {
        this(target, path, null);
    }

    public TomcatModule (Target target, String path, String docRoot) {
        if (!path.isEmpty() && !path.startsWith("/")) {
            LOGGER.log(Level.INFO, "Non empty module path must start with '/'; was {0}", path);
        }
        this.target = (TomcatTarget) target;
        this.path = "".equals(path) ? "/" : path; // NOI18N
        this.docRoot = docRoot;
    }
    
    public String getDocRoot () {
        return docRoot;
    }
    
    @Override
    public TargetModuleID[] getChildTargetModuleID () {
        return null;
    }
    
    @Override
    public String getModuleID () {
        return getWebURL ();
    }
    
    @Override
    public TargetModuleID getParentTargetModuleID () {
        return null;
    }
    
    @Override
    public Target getTarget () {
        return target;
    }
    
    /** Context root path of this module. */
    public String getPath () {
        return path;
    }

    @Override
    public String getWebURL () {
        return target.getServerUri () + path.replace(" ", "%20");
    }
    
    @Override
    public String toString () {
        return getModuleID ();
    }
}
