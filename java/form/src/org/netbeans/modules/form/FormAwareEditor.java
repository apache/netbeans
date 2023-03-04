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


package org.netbeans.modules.form;

/**
 * Interface implemented by property editors that need to know the context
 * where they are used (FormModel and the property they edit).
 * 
 * @author Tomas Pavek
 */
public interface FormAwareEditor {

    void setContext(FormModel formModel, FormProperty property);

    /**
     * Called when a value is written to a property which has this property
     * editor associated with. At this moment the property editor has a chance
     * to indicate which format version (NB release) the value requires to be
     * stored in the form file. Use FormModel.raiseVersionLevel method.
     */
    void updateFormVersionLevel();
}
