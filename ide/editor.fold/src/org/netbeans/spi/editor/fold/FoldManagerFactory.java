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

package org.netbeans.spi.editor.fold;

import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * This factory interface allows to produce {@link FoldManager}
 * instance for the given fold.
 * <br>
 * It is intended for xml layer registration
 * into the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/FoldManager
 * </pre>
 * For example java fold manager factories should be registered in
 * <pre>
 *     Editors/text/x-java/FoldManager
 * </pre>
 *
 * <p>
 * The factories present in the folder can be sorted by using standard
 * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
 * Layer Ordering</a>.
 * <br>
 * The fold manager of factory A registered prior factory B produces
 * folds with higher priority than those from fold manager of factory B.
 * <br>
 * If two folds would overlap the one with higher priority
 * will be visible - see {@link FoldManager} for more details.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

@MimeLocation(subfolderName="FoldManager")
public interface FoldManagerFactory {
    
    /**
     * Create fold manager instance.
     */
    public FoldManager createFoldManager();
    
}
