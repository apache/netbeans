/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.cmd;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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
                try {
                    byte[] bytes = Base64.decodeBase64(message.getBytes("UTF-8"));
                    toRet.setErrorMessage(new String(bytes, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
