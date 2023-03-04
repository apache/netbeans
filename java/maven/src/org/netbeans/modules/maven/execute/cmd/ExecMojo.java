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

package org.netbeans.modules.maven.execute.cmd;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.codehaus.plexus.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ExecMojo extends ExecutionEventObject {
        public final String goal;
        public final GAV plugin;
        public final String phase;
        public final String executionId;
        private String errorMessage;
        private InputLocation location;
        private URL[] classpathURLs;
        private String implementationClass;


    public ExecMojo(String goal, GAV plugin, String phase, String executionId, ExecutionEvent.Type type) {
        super(type);
        this.goal = goal;
        this.plugin = plugin;
        this.phase = phase;
        this.executionId = executionId;
    }

    
    public static ExecMojo create(JSONObject obj, ExecutionEvent.Type t) {
        JSONObject mojo = (JSONObject) obj.get("mojo");
        String id = (String) mojo.get("id");
        String[] ids = id.split(":");
        GAV mojoGav = new GAV(ids[0], ids[1], ids[2]);
        String goal = (String) mojo.get("goal");
        String execId = (String) mojo.get("execId");
        String phase = (String) mojo.get("phase");
        ExecMojo toRet = new ExecMojo(goal, mojoGav, phase, execId, t);
        JSONObject exc = (JSONObject) obj.get("exc");
        if (exc != null) {
            String message = (String) exc.get("msg");
            if (message != null) {
                byte[] bytes = Base64.decodeBase64(message.getBytes(StandardCharsets.UTF_8));
                toRet.setErrorMessage(new String(bytes, StandardCharsets.UTF_8));
            }
        }
        JSONObject loc = (JSONObject) mojo.get("loc");
        if (loc != null) {
            Long lineNumber = (Long) loc.get("ln");
            Long columnNumber = (Long) loc.get("col");
            String file = (String) loc.get("loc");
            String modelid = (String) loc.get("id");
            InputSource is = new InputSource();
            is.setLocation(file);
            is.setModelId(modelid);
            InputLocation location = new InputLocation(lineNumber.intValue(), columnNumber.intValue(), is);
            toRet.setLocation(location);
        }
        JSONArray urls = (JSONArray)mojo.get("urls");
        if (urls != null) {
            List<URL> urlList = new ArrayList<URL>();
            Iterator it = urls.iterator();
            while (it.hasNext()) {
                String url = (String) it.next();
                try {
                    urlList.add(new URL(url));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            toRet.setClasspathURLs(urlList.toArray(new URL[0]));
        }
        toRet.setImplementationClass((String) mojo.get("impl"));
        return toRet;
    }

    /**
     * only applicable in mojofailed
     */
    private void setErrorMessage(String string) {
        errorMessage = string;
    }

    /**
     * only applicable in mojofailed
     * @return 
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public InputLocation getLocation() {
        return location;
    }

    private void setLocation(InputLocation location) {
        this.location = location;
    }
    
    public URL[] getClasspathURLs() {
        return classpathURLs;
    }

    public void setClasspathURLs(URL[] classpathURLs) {
        this.classpathURLs = classpathURLs;
    }
    
    public URL getPluginJarURL() {
        if (classpathURLs != null) {
            String name = "/" + plugin.artifactId + "-" + plugin.version + ".jar";
            for (URL url : classpathURLs) {
                //simple if enough, the plugin jar should always be the first item..
                if (url.toExternalForm().endsWith(name)) {
                    return url;
                }
            }
        }
        return null;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }
}
