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
package org.netbeans.spi.java.hints.unused;

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * SPI to mark an arbitrary {@link Element} as used. Allows to suppress the standard NetBeans
 * unused element detection and to prevent the "unused" hint being displayed on given elements.
 * Can be used by various framework libraries that sometimes honor annotations
 * (i.e. injections or bindings) even on private methods.
 *
 * @since 1.56
 */
public interface UsedDetector {

    /**
     * Checks whether given element should be marked as "used".
     * @param el element to check
     * @param path path to the element to check
     * @return true if the given element should be marked as "used"
     * @since 1.56
     */
    boolean isUsed(Element el, TreePath path);

    /**
     * Factory to create {@link UsedDetector} instances.
     * @since 1.56
     */
    public interface Factory {
        /**
         * Creates {@link UsedDetector} instance for the given {@link CompilationInfo}.
         * @param info
         * @return {@link UsedDetector} instance or null.
         * @since 1.56
         */
        UsedDetector create(CompilationInfo info);
    }
}
