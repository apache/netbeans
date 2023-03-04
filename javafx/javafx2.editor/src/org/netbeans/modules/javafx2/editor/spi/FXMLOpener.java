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
package org.netbeans.modules.javafx2.editor.spi;

import org.openide.util.Lookup;

/**
 * A friend SPI for providing custom FXML open action
 * Currently the only implementation is the SceneBuilder integraion module
 * @author Jaroslav Bachorik
 * 
 * @since 1.3.1
 */
public abstract class FXMLOpener {
    public abstract boolean isEnabled(Lookup context);
    public abstract boolean open(Lookup context);
}
