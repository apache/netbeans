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
package org.netbeans.modules.cloud.oracle.devops;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Finds OCI DevOps configuration if available.
 * 
 * @author jhorvath
 */
public class DevopsProjectService {
    private static DevopsConfigFinder finder = null;
    
    
    public static List<String> getDevopsProjectOcid() {
        List<FileObject> configs = getDefaultFinder().findDevopsConfig();
        Gson gson = new Gson();
        List<String> devopsOcid = new ArrayList<> ();
        try {
            for (FileObject config : configs) {
                JsonObject json = gson.fromJson(new InputStreamReader(configs.get(0).getInputStream()), JsonObject.class);
                JsonArray services = json.getAsJsonArray("cloudServices"); //NOI18N
                for (JsonElement service : services) {
                    JsonElement type = service.getAsJsonObject().get("type");
                    if (type != null && "oci".equals(type.getAsString())) { //NOI18N
                        JsonObject data = service.getAsJsonObject().getAsJsonObject("data"); //NOI18N
                        JsonObject context = data.getAsJsonObject("context"); //NOI18N
                        devopsOcid.add(context.get("devopsProject").getAsString()); //NOI18N
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return devopsOcid;
    }
    
    
    private static DevopsConfigFinder getDefaultFinder() {
        if (finder == null) {
            finder = Lookup.getDefault().lookup(DevopsConfigFinder.class);
        }
        if (finder == null) {
            finder = new DefaultDevopsConfigFinder();
        }
        return finder;
    }
    
    public interface DevopsConfigFinder {
        List<FileObject> findDevopsConfig();
    }

    static class DefaultDevopsConfigFinder implements DevopsConfigFinder {

        @Override
        public List<FileObject> findDevopsConfig() {
            return Collections.emptyList();
        }
        
    }
    
}
