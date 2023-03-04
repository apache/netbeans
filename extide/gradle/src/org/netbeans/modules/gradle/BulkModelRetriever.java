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
package org.netbeans.modules.gradle;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.gradle.api.Action;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.Model;

/**
 *
 * @author lkishalmi
 */
public class BulkModelRetriever {

    private static final AtomicInteger TASK_SEQUENCE = new AtomicInteger();

    <T extends Model, P> Future<T> fetchModel(Class<T> modelType, Class<P> parameterType, Action<? super P> parameterInitializer) {
        return null;
    }

    public static class BulkActionResult {}
    public static class ModelResult implements Serializable {
        String id;
        Model result;
        Throwable exception;

        public ModelResult(String id, Model result) {
            this.id = id;
            this.result = result;
        }

        public ModelResult(String id, Throwable exception) {
            this.id = id;
            this.exception = exception;
        }

        public String getId() {
            return id;
        }

        public Model getResult() {
            return result;
        }

        public Throwable getException() {
            return exception;
        }

    }

    public static class ModelTask<T extends Model, P> implements Serializable {

        String id;
        Class<T> modelType;
        Class<P> parameterType;
        Action<? super P> parameterInitializer;

        public ModelTask(Class<T> modelType, Class<P> parameterType, Action<? super P> parameterInitializer) {
            id = modelType.getName() + "-" + TASK_SEQUENCE.getAndIncrement();
            this.modelType = modelType;
            this.parameterType = parameterType;
            this.parameterInitializer = parameterInitializer;
        }

        public String getId() {
            return id;
        }

        public Class<T> getModelType() {
            return modelType;
        }

        public Class<P> getParameterType() {
            return parameterType;
        }

        public Action<? super P> getParameterInitializer() {
            return parameterInitializer;
        }
        
    }

    private static class BulkModelAction implements BuildAction<List<ModelResult>> {

        List<ModelTask> modelTasks = new LinkedList<>();

        public void addTask(Class modelType, Class parameterType, Action parameterInitializer) {
            ModelTask task = new ModelTask(modelType, parameterType, parameterInitializer);
            modelTasks.add(task);
        }

        @Override
        public List<ModelResult> execute(BuildController bc) {
            List<ModelResult> results = new LinkedList<>();
            for (ModelTask modelTask : modelTasks) {
                if (modelTask.parameterType != null) {
                    try {
                        Model m = (Model)bc.getModel(modelTask.modelType, modelTask.parameterType, modelTask.getParameterInitializer());
                        results.add(new ModelResult(modelTask.getId(), m));
                    } catch (Throwable th) {
                        results.add(new ModelResult(modelTask.getId(), th));
                    }
                }
            }
            return results;
        }

    }
}
