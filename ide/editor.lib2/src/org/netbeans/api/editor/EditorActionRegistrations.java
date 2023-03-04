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

package org.netbeans.api.editor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to annotate one action class by multiple
 * {@link EditorActionRegistration} annotations.
 * <br>
 * Example:
 * <pre>
 * @EditorActionRegistrations({ @EditorActionRegistration(name = "name1", ...),
 *                              @EditorActionRegistration(name = "name2", ...)
 *                            })
 * public class MultiAction extends BaseAction {
 *
 *     public void actionPerformed(ActionEvent evt) {
 *         if ("name1".equals(getValue(Action.NAME))) {
 *             // behavior for "name1"
 *         } else {
 *             // behavior for "name2"
 *         }
 *     }
 *
 * ]
 * </pre>
 *
 * @since 1.10
 * @author Miloslav Metelka
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface EditorActionRegistrations {

    EditorActionRegistration[] value();

}
