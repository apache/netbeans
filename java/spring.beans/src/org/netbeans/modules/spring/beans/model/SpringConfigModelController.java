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

package org.netbeans.modules.spring.beans.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.SpringConfigModelAccessor;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.LockedDocument;

/**
 * The implementation of the config model for a config file group. Manages
 * a model for each of the files. Provides access to the model.
 * This class is thread-safe.
 *
 * @author Andrei Badea
 */
public class SpringConfigModelController {

    // XXX probably make lazy. First runReadAccess() will be slower, but
    // at least we won't be eating up unnecessary memory.

    private final ConfigFileGroup configFileGroup;
    private final Map<File, SpringConfigFileModelController> file2Controller = new HashMap<File, SpringConfigFileModelController>();

    // Encapsulates the current read access to the model.
    private ConfigModelSpringBeans readAccess;
    // Encapsulates the current read access to the model.
    private boolean writeAccess;

    /**
     * @param  fileModelManager the manager of models for the individual config files.
     * @param  configFileGroup the config file group to create a model for.
     * @return a new instance; never null.
     */
    public SpringConfigModelController(SpringConfigFileModelManager fileModelManager, ConfigFileGroup configFileGroup) {
        this.configFileGroup = configFileGroup;
        for (File file : configFileGroup.getFiles()) {
            SpringConfigFileModelController controller = fileModelManager.getFileModelController(file);
            if (controller != null) {
                file2Controller.put(file, controller);
            }
        }
    }

    public ConfigFileGroup getConfigFileGroup() {
        return configFileGroup;
    }

    /**
     * Provides access to the model by running the passed
     * action under exclusive access.
     *
     * @param  action the action to run.
     */
    public void runReadAction(final Action<SpringBeans> action) throws IOException {
        try {
            ExclusiveAccess.getInstance().runSyncTask(new Callable<Void>() {
                public Void call() throws IOException {
                    runReadActionExclusively(action);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                IOException ioe = new IOException(e.getMessage());
                throw (IOException)ioe.initCause(e);
            }
        }
    }

    private void runReadActionExclusively(Action<SpringBeans> action) throws IOException {
        if (writeAccess) {
            throw new IllegalStateException("Already in write access.");
        }
        // Handle reentrant access.
        boolean firstEntry = (readAccess == null);
        try {
            if (firstEntry) {
                readAccess = new ConfigModelSpringBeans(computeSpringBeanSources(null));
            }
            action.run(readAccess);
        } finally {
            if (firstEntry) {
                readAccess = null;
            }
        }
    }

    /**
     * Provides access to the model and the document for each underlying configuration file.
     * The passed {@code action} will be invoked sequentially for each configuration
     * file. This is useful for actions which need to process all files in the
     * model while also accessing the document for each file (for example, refactoring).
     *
     * @param  action the action to run.
     */
    public void runDocumentAction(final Action<DocumentAccess> action) throws IOException {
        try {
            ExclusiveAccess.getInstance().runSyncTask(new Callable<Void>() {
                public Void call() throws IOException {
                    runDocumentActionExclusively(action);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                IOException ioe = new IOException(e.getMessage());
                throw (IOException)ioe.initCause(e);
            }
        }
    }

    private void runDocumentActionExclusively(Action<DocumentAccess> action) throws IOException {
        if (readAccess != null) {
            throw new IllegalStateException("Already in read access.");
        }
        if (writeAccess) {
            throw new IllegalStateException("Reentrant write access not supported");
        }
        writeAccess = true;
        try {
            for (Map.Entry<File, SpringConfigFileModelController> it : file2Controller.entrySet()) {
                File currentFile = it.getKey();

                Map<File, SpringBeanSource> beanSources = computeSpringBeanSources(currentFile);
                SpringConfigFileModelController controller = it.getValue();
                LockedDocument lockedDoc = controller.getLockedDocument();
                if (lockedDoc != null) {
                    lockedDoc.lock();
                    try {
                        beanSources.put(currentFile, lockedDoc.getBeanSource());
                        ConfigModelSpringBeans springBeans = new ConfigModelSpringBeans(beanSources);
                        DocumentAccess docAccess = SpringConfigModelAccessor.getDefault().createDocumentAccess(springBeans, currentFile, lockedDoc);
                        action.run(docAccess);
                    } finally {
                        lockedDoc.unlock();
                    }
                }
            }
        } finally {
            writeAccess = false;
        }
    }

    private Map<File, SpringBeanSource> computeSpringBeanSources(File skip) throws IOException {
        Map<File, SpringBeanSource> result = new HashMap<File, SpringBeanSource>();
        for (Map.Entry<File, SpringConfigFileModelController> entry : file2Controller.entrySet()) {
            File currentFile = entry.getKey();
            if (!currentFile.equals(skip)) {
                result.put(entry.getKey(), entry.getValue().getUpToDateBeanSource());
            }
        }
        return result;
    }
}
