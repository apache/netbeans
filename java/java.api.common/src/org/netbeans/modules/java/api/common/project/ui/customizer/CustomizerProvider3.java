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
package org.netbeans.modules.java.api.common.project.ui.customizer;

import org.netbeans.spi.project.ui.CustomizerProvider2;

/**
 * CustomizerProvider enhanced with ability to explicitly close a customizer
 * that may be currently opened. The close operation is equivalent to
 * pressing Cancel in customizer dialog.
 * 
 * @author Petr Somol
 * @since 1.52
 */
public interface CustomizerProvider3 extends CustomizerProvider2 {
    
    /**
     * Close customizer if it is currently opened as if it was cancelled
     */
    void cancelCustomizer();
    
}
