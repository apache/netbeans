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

package org.netbeans.modules.gradle.tooling;

import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 *
 * @author Laszlo Kishalmi
 */
public class NetBeansToolingPlugin implements Plugin<Project> {

    private final ToolingModelBuilderRegistry registry;

    /**
     * Need to use a {@link ToolingModelBuilderRegistry} to register the custom tooling model, so inject this into the
     * constructor.
     */
    @Inject
    public NetBeansToolingPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project t) {
        registry.register(new NetBeansToolingModelBuilder());
    }

    private static class NetBeansToolingModelBuilder implements ToolingModelBuilder {

        @Override
        public boolean canBuild(String modelName) {
            return NbProjectInfo.class.getName().equals(modelName);
        }

        @Override
        public Object buildAll(String modelName, Project prj) {
            try {
                NbProjectInfo model = new NbProjectInfoBuilder(prj).buildAll();
                Map<String, Object> info = model.getInfo();
                if (prj.hasProperty("nbSerializeCheck")) {
                    List<String> serializeProblems = new LinkedList<>();

                    try (ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream())) {
                        for (String key : info.keySet()) {
                            try {
                                os.writeObject(info.get(key));
                            } catch (NotSerializableException ex) {
                                serializeProblems.add(key);
                                System.err.println("Field '" + key + "' is not serializable: " + ex.getMessage());
                            }
                        }
                    } catch (IOException ie) {
                    }
                    if (!serializeProblems.isEmpty()) {
                        BaseModel ret = new NbProjectInfoModel();
                        ret.setGradleException(new NotSerializableException(serializeProblems.toString()).toString());
                        return ret;

                    }
                }
                return model;
            } catch (NeedOnlineModeException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println(ex.toString());
                ex.printStackTrace(pw);

                NbProjectInfoModel ret = new NbProjectInfoModel();
                ret.setGradleException(sw.toString());

                Throwable cause = ex;
                while ((cause != null) && (cause.getCause() != cause)) {
                    if (cause instanceof GradleException) {
                        // unexpected exceptions at this level
                        ret.noteProblem((GradleException) cause, true);
                        break;
                    }
                    cause = cause.getCause();
                }
                return ret;
            }
        }
        
    }

}
