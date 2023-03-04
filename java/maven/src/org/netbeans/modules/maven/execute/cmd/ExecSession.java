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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.execution.ExecutionEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ExecSession extends ExecutionEventObject {

    public final int projectCount;
    private URL[] mnvcoreurls;

    public ExecSession(int projectCount, ExecutionEvent.Type type) {
        super(type);
        this.projectCount = projectCount;
    }
    
    public static ExecutionEventObject create(JSONObject obj, ExecutionEvent.Type t) {
        Long count = (Long) obj.get("prjcount");
        int prjCount = -1;
        if (count != null) {
            prjCount = count.intValue();
        }
        ExecSession toRet = new ExecSession(prjCount, t);
        JSONArray arr = (JSONArray) obj.get("mvncoreurls");
        if (arr != null) {
            List<URL> urlList = new ArrayList<URL>();
            Iterator it = arr.iterator();
            while (it.hasNext()) {
                String url = (String) it.next();
                try {
                    urlList.add(new URL(url));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            toRet.setMnvcoreurls(urlList.toArray(new URL[0]));
        }
        
        return toRet;
    }

    public URL[] getMnvcoreurls() {
        return mnvcoreurls;
    }

    private void setMnvcoreurls(URL[] mnvcoreurls) {
        this.mnvcoreurls = mnvcoreurls;
    }
    
}
