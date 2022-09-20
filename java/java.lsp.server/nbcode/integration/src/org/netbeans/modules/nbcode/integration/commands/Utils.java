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
package org.netbeans.modules.nbcode.integration.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author sdedic
 */
class Utils {
    static FileObject extractFileObject(Object argument, Gson gson) {
        FileObject f = null;
        String s = "";
        if (argument instanceof JsonPrimitive) {
            s = ((JsonPrimitive)argument).getAsString();
            try {
                URI uri = new URI(s);
                f = URLMapper.findFileObject(uri.toURL());
            } catch (URISyntaxException | IllegalArgumentException | MalformedURLException ex) {
                f = FileUtil.toFileObject(new File(s));
            }
        } else {
            // accept something that looks like vscode.Uri structure
            JsonObject executeOn = gson.fromJson(gson.toJson(argument), JsonObject.class);
            if (executeOn.has("fsPath")) {
                s = executeOn.get("fsPath").getAsString();
                f = FileUtil.toFileObject(new File(s));
            }
        }
        if (f == null) {
            throw new IllegalArgumentException("Invalid path specified: " + s);
        }
        return f;
    }
}
