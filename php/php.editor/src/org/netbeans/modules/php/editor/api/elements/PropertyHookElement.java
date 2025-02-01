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
package org.netbeans.modules.php.editor.api.elements;

import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Property hook element.
 *
 * @see @since 2.46.0
 */
public interface PropertyHookElement extends PhpElement {

    /**
     * Check whether a property hook is reference.
     *
     * e.g. {@code &get{}}
     *
     * @return {@code true} if it's reference, {@code false} otherwise
     * @since 2.46.0
     */
    boolean isReference();

    /**
     * Check whether a property hook has a body(`{}` part).
     *
     * Interface property and abstract properties don't have a body. (e.g.
     * {@code get; set;})
     *
     * @return {@code true} if it has a body, {@code false} otherwise
     * @since 2.46.0
     */
    boolean hasBody();

    /**
     * Check whether a property hook has attributes. (e.g.
     * {@code #[Attr] get{}})
     *
     * @return {@code true} if a property hook has attributes, {@code false}
     * otherwise
     * @since 2.46.0
     */
    boolean isAttributed();

    /**
     * Get parameters of a property hook.
     *
     * e.g. {@code set(#[Attr] string $value){}}
     *
     * @return parameters
     * @since 2.46.0
     */
    List<? extends ParameterElement> getParameters();

    /**
     * Get the offset range.
     *
     * @return the offset range
     * @since 2.46.0
     */
    OffsetRange getOffsetRange();
    // TODO add List<? extends AttributeElemnt> getAttributes();
}
