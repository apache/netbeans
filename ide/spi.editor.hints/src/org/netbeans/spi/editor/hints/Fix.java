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

package org.netbeans.spi.editor.hints;

import org.netbeans.modules.editor.hints.HintsControllerImpl;

/**
 * Allows to perform a change when the user selects the hint.
 * @author Jan Lahoda
 */
public interface Fix {

    /**
     * The text displayed to the user as the fix description.
     * @return
     */
    public abstract String getText();

    /**
     * Correct the source, doing whatever the hint says it will do.
     * @return A ChangeInfo instance if invoking the hint caused changes
     *  that should change the editor selection/caret position, or null
     *  if no such change was made, or proper caret positioning cannot be
     *  determined.
     */
    public abstract ChangeInfo implement() throws Exception;
    
    default Iterable<? extends Fix> getSubfixes() {
        return HintsControllerImpl.getSubfixes(this);
    }
}
