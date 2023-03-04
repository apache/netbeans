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
package org.netbeans.modules.php.editor.model;

import java.util.Collection;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.PhpElement;

/**
 * @author Radek Matous
 */
public interface Occurence {

    public static enum Accuracy {
        NO,
        UNIQUE,
        EXACT,
        EXACT_TYPE,
        MORE_TYPES,
        MORE_MEMBERS,
        MORE,
    }

    /**
     * makes sense just for type members for calls with unknown types on left side (else EXACT).
     */
    Accuracy degreeOfAccuracy();
    /**mostly the same as getDeclaration. In case of __constructor are different*/
    PhpElementKind getKind();
    Collection<? extends PhpElement> gotoDeclarations();
    Collection<? extends PhpElement> getAllDeclarations();
    Collection<Occurence> getAllOccurences();
    OffsetRange getOccurenceRange();

}
