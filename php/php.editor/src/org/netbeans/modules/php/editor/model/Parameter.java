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

package org.netbeans.modules.php.editor.model;

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;

/**
 *
 * @author Radek Matous
 */
public interface Parameter {

    String getName();
    String getDefaultValue();
    @CheckForNull
    String getDeclaredType();
    @CheckForNull
    String getPhpdocType();
    @CheckForNull
    /**
     * @return false if the type information is taken from PHPDoc
     */
    boolean hasRawType();
    boolean isMandatory();
    boolean isReference();
    boolean isVariadic();
    boolean isUnionType();
    boolean isIntersectionType();
    int getModifier();
    /*mixed types can be documented in php doc*/
    List<QualifiedName> getTypes();
    OffsetRange getOffsetRange();
    String getIndexSignature();

}
