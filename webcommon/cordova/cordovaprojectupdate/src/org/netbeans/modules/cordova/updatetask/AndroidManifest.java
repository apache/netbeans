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
package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * API for AndroidManifest.xml
 * @author Jan Becicka
 */
public class AndroidManifest extends XMLFile {

    public AndroidManifest(InputStream resource) throws IOException {
        super(resource);
    }

    public AndroidManifest(File manifestFile) throws IOException {
        super(manifestFile);
    }

    public String getName() {
        return getAttributeText("/manifest/application/activity", "android:name"); // NOI18N
    }
    
    public void setName(String name) {
        setAttributeText("/manifest/application/activity", "android:name", name); // NOI18N
    }

    public String getPackage() {
        return getAttributeText("/manifest","package"); // NOI18N
    }

    public void setPackage(String packageName) {
        setAttributeText("/manifest", "package", packageName); // NOI18N
    }
    
    public boolean isDebuggable() {
        return Boolean.parseBoolean(getAttributeText("/manifest/application", "android:debuggable")); // NOI18N
    }
    
    public void setDebuggable(boolean debuggable) {
        setAttributeText("/manifest/application", "android:debuggable", Boolean.toString(debuggable));
    }
    
}
