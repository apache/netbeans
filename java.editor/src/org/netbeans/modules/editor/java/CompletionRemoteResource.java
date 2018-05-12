/**
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
package org.netbeans.modules.editor.java;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaCompletionTask.Options;
import org.netbeans.modules.java.source.remote.api.Parser;
import org.netbeans.modules.java.source.remote.api.Parser.Config;
import org.netbeans.modules.java.source.remote.api.ResourceRegistration;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@Path("/completion")
public class CompletionRemoteResource {
    
    @GET
    @Path("/compute")
    public String compute(@QueryParam("caretOffset") int caretOffset, @QueryParam("parser-config") String config) throws IOException {
        try {
        //TODO: options, etc.
        Gson gson = new Gson();
        Config conf = gson.fromJson(config, Config.class);
        return Parser.runControllerTask(conf, cc -> {
            JavaCompletionTask<JavaCompletionItem> task = JavaCompletionTask.create(caretOffset, new JavaCompletionItemFactory(cc.getFileObject()), EnumSet.noneOf(Options.class), new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return false;
                }
            });

            task.resolve(cc);
            return gson.toJson(new CompletionShim(task));
        });
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
    
    public static final class CompletionShim {
        public CompletionItemShim[] completions;
        //TODO: other attributes...

        public CompletionShim(JavaCompletionTask<JavaCompletionItem> task) {
            this.completions = task.getResults().stream().map(CompletionItemShim::new).toArray(v -> new CompletionItemShim[v]);
        }
        
    }

    public static final class CompletionItemShim {
        public Map<Object, Object> content = new HashMap<>();

        public CompletionItemShim(JavaCompletionItem ci) {
            ci.serialize(content);
        }
        
    }

    @ServiceProvider(service=ResourceRegistration.class)
    public static final class RegistrationImpl implements ResourceRegistration {

        @Override
        public Class<?> getResourceClass() {
            return CompletionRemoteResource.class;
        }
        
    }
}
