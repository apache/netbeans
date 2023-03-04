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

package org.netbeans.installer.utils.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.List;

public class NbiClassLoader extends URLClassLoader {
    public NbiClassLoader(final List<ExtendedUri> uris) throws MalformedURLException {
        super(new URL[]{}, NbiClassLoader.class.getClassLoader());
        
        for(ExtendedUri uri : uris) {
            addURL(uri.getLocal().toURL());
        }
    }
    
    protected PermissionCollection getPermissions(final CodeSource source) {
        return getClass().getProtectionDomain().getPermissions();
    }
}
