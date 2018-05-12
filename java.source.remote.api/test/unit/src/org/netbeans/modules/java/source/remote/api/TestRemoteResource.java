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
package org.netbeans.modules.java.source.remote.api;

import com.google.gson.Gson;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@Path("/test")
public class TestRemoteResource {

    @GET
    public String highlightData(@QueryParam("parser-config") String config) throws IOException {
        try {
        Gson gson = new Gson();
        Parser.Config conf = gson.fromJson(config, Parser.Config.class);
        String result = Parser.runTask(conf, ci -> "good: " + ci.getTopLevelElements().get(0).getQualifiedName() + "/" + ci.getSourceVersion());

        return gson.toJson(result);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @ServiceProvider(service=ResourceRegistration.class)
    public static final class RegistrationImpl implements ResourceRegistration {

        @Override
        public Class<?> getResourceClass() {
            return TestRemoteResource.class;
        }

    }
}
