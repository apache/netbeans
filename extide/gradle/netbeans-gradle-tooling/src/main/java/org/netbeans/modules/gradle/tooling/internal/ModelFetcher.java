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
package org.netbeans.modules.gradle.tooling.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.gradle.api.Action;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 *
 * @author lkishalmi
 */
public final class ModelFetcher {
    private static final AtomicInteger REQUEST_SEQUENCER = new AtomicInteger();

    final ExecutorService executor;
    final transient CountDownLatch lock = new CountDownLatch(1);
    final MultiModelAction action = new MultiModelAction();
    final Map<Integer, ModelResult> modelResults = new HashMap<>();

    public ModelFetcher() {
        executor = Executors.newSingleThreadExecutor();
    }

    public ModelFetcher(ExecutorService executor) {
        this.executor = executor;
    }

    public boolean isAcceptingRequests() {
        return !executor.isShutdown();
    }

    public <T> Future<T> requestModel(Class<T> modelType) throws RejectedExecutionException {
        return requestModel(null, modelType, null, null);
    }

    public <T> Future<T> requestModel(String target, Class<T> modelType) throws RejectedExecutionException {
        return requestModel(target, modelType, null, null);
    }

    public <T,P> Future<T> requestModel(Class<T> modelType, Class<P> parameterType, Action<? super P> parameterInitializer) throws RejectedExecutionException {
        return requestModel(null, modelType, parameterType, parameterInitializer);
    }

    @SuppressWarnings("unchecked")
    public <T,P> Future<T> requestModel(String target, Class<T> modelType, Class<P> parameterType, Action<? super P> parameterInitializer) throws RejectedExecutionException {
        ModelRequest req = new ModelRequest(target, modelType, parameterType, parameterInitializer);
        Future ret = executor.submit(() -> {
            lock.await();
            ModelResult result = modelResults.get(req.sequenceId);
            if (result != null) {
                if (result.problem == null) {
                    return (T) result.model;
                } else {
                    throw new Exception(result.problem);
                }
            } else {
                throw new Exception("Model not found");
            }
        });
        action.modelRequests.add(req);
        return ret;
    }

    public <T,P> void modelAction(String target, Class<T> modelType, Action<T> action) throws RejectedExecutionException {
        modelAction(target, modelType, null, null, action, null);
    }

    public <T,P> void modelAction(String target, Class<T> modelType, Action<T> action, Action<Exception> error) throws RejectedExecutionException {
        modelAction(target, modelType, null, null, action, error);
    }

    @SuppressWarnings("unchecked")
    public <T,P> void modelAction(String target, Class<T> modelType, Class<P> parameterType, Action<? super P> parameterInitializer, Action<T> action, Action<Exception> error) throws RejectedExecutionException {
        ModelRequest req = new ModelRequest(target, modelType, parameterType, parameterInitializer);
        executor.submit(() -> {
            lock.await();
            ModelResult result = modelResults.get(req.sequenceId);
            if (result != null) {
                if (result.problem == null) {
                    if (action != null) action.execute((T) result.model);
                } else {
                    if (error != null) error.execute(new Exception(result.problem));
                }
            } else {
                throw new Exception("Model not found");
            }
            return null;
        });
        this.action.modelRequests.add(req);
    }

    public void fetchModels(ProjectConnection pconn, Action<? super BuildActionExecuter> config) {
        executor.shutdown();
        try {
            if (!action.modelRequests.isEmpty()) {
                BuildActionExecuter<Map<Integer, ModelResult>> exec = pconn.action(action);
                if (config != null) {
                    config.execute(exec);
                }
                modelResults.putAll(exec.run());
            }
        } finally {
            lock.countDown();
        }
    }

    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(time, unit);
    }
    
    static class MultiModelAction implements BuildAction<Map<Integer, ModelResult>> {

        final List<ModelRequest> modelRequests = new LinkedList<>();

        @Override
        @SuppressWarnings("unchecked")
        public Map<Integer, ModelResult> execute(BuildController bc) {
            Map<Integer,ModelResult> ret = new HashMap<>();
            List<ModelRequest> reqs = new LinkedList<>(modelRequests);
            Iterator<ModelRequest> it = reqs.iterator();
            while (it.hasNext()) {
                ModelRequest req = it.next();
                if (req.targetProjectId == null) {
                    it.remove();
                    if (req.parameterType != null) {
                        try {
                            ret.put(req.sequenceId, new ModelResult(bc.getModel(req.modelType, req.parameterType, req.parameterInitializer)));
                        } catch (Throwable ex) {
                            ret.put(req.sequenceId, new ModelResult(ex));
                        }
                    } else {
                        try {
                            ret.put(req.sequenceId, new ModelResult(bc.getModel(req.modelType)));
                        } catch (Throwable ex) {
                            ret.put(req.sequenceId, new ModelResult(ex));
                        }
                    }
                }
            }
            if (!reqs.isEmpty()) {
                GradleBuild build = bc.getBuildModel();
                Map<String, BasicGradleProject> projects = new HashMap<>();
                for (BasicGradleProject prj : build.getProjects()) {
                    projects.put(prj.getPath(), prj);
                }
                for (ModelRequest req : reqs) {
                    BasicGradleProject target = projects.get(req.targetProjectId);
                    if (target != null) {
                        if (req.parameterType != null) {
                            try {
                                ret.put(req.sequenceId, new ModelResult(bc.getModel(target, req.modelType, req.parameterType, req.parameterInitializer)));
                            } catch (Throwable ex) {
                                ret.put(req.sequenceId, new ModelResult(ex));
                            }
                        } else {
                            try {
                                ret.put(req.sequenceId, new ModelResult(bc.getModel(target, req.modelType)));
                            } catch (Throwable ex) {
                                ret.put(req.sequenceId, new ModelResult(ex));
                            }
                        }
                    } else {
                        ret.put(req.sequenceId, new ModelResult(new NullPointerException(req.targetProjectId)));
                    }
                }
            }
            return ret;
        }
    }
    
    static class ModelRequest<T,P> implements Serializable {
        final int sequenceId = REQUEST_SEQUENCER.getAndIncrement();
        String targetProjectId;
        Class<T> modelType;
        Class<P> parameterType;
        Action<? super P> parameterInitializer;

        public ModelRequest(String targetProjectId, Class<T> modelType, Class<P> parameterType, Action<? super P> parameterInitializer) {
            this.targetProjectId = targetProjectId;
            this.modelType = modelType;
            this.parameterType = parameterType;
            this.parameterInitializer = parameterInitializer;
        }
    }

    static class ModelResult<T> implements Serializable {
        T model;
        Throwable problem;

        public ModelResult(T model) {
            this.model = model;
        }

        public ModelResult(Throwable problem) {
            this.problem = problem;
        }
    }
}
