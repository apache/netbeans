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

package org.netbeans.modules.maven.model;

import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * A single model modifying operation unit. Used by the {@link org.netbeans.modules.maven.model.Utilities}
 * class to perform one or many operation on top of the model.
 * 
 * Please note that ModelOperation should not be used to GET values out of the model, all such values will be unresolved.
 * The primary objective of the ModelOperation and AbstractDocumentModel subclasses is to modify a single file.
 *
 * @author mkleint
 */
public interface ModelOperation<T extends AbstractDocumentModel<? extends DocumentComponent<?>>> {

    /**
     * perform modifications on the model passed as parameter.
     * @param model
     */
    void performOperation(T model);
}
