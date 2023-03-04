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
package org.netbeans.modules.javafx2.editor.completion.impl;

import javax.lang.model.element.TypeElement;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Converts a TypeElement into a CC suggestion item. Handles different variations
 * of the TypeElement, as each situation requires its own processing and CompletionItem
 * results.
 * 
 * @author sdedic
 */
@MimeLocation(subfolderName="completion")
public interface ClassItemFactory {
    public CompletionItem   convert(TypeElement elem, CompletionContext ctx, int priorityHint);
}
