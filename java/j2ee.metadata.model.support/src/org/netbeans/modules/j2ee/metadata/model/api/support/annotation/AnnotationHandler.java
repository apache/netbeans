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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * A callback interface for annotated elements. Implementations of this
 * interface are used by annotation scanners such as
 * {@link AnnotationScanner} to pass annotations back to the caller.
 *
 * @author Andrei Badea, Tomas Mysik
 */
public interface AnnotationHandler {

    /**
     * This method allows implementors to process annotated elements. Typically
     * this method will be called once for each element annotated with the
     * specified annotation.
     *
     * @param type        the type in which annotated element can be found. Never <code>null</code>.
     * @param element     the element annotated with the annotation specified
     *                    by the <code>annotation</code> parameter. Never <code>null</code>.
     *                    The same as <code>type</code> param for class.
     * @param annotation  an annotation mirror.
     */
    void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation);
}
