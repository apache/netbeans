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
package org.netbeans.modules.javascript2.model.spi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.model.ModelAccessor;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.types.spi.ParserResult;

/**
 *
 * @author Petr Pisl
 */
public final class ModelContainer {

    private static final Logger LOGGER = Logger.getLogger(ModelContainer.class.getName());

    private Model model;

    @NonNull
    public Model getModel(ParserResult info, boolean reload) {
        synchronized (this) {
            if (model == null || reload) {
                model = ModelAccessor.getDefault().createModel(info);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    model.writeModel((String str) -> {
                        LOGGER.log(Level.FINEST, str);
                    });
                }
            }
            return model;
        }
    }
}
