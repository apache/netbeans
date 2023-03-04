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

package org.netbeans.modules.csl.api;

import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;


/**
 * A CancellableTask responsible for computing the set of highlights matching
 * occurrences of the same program element as the one under the caret.
 *
 * @todo This should not be a stateful interface; there should be a single
 *   compute-occurrences-for-position method.  See
 *   http://www.netbeans.org/issues/show_bug.cgi?id=144160
 *   for the kinds of bugs that can result from this.
 * 
 * @author Tor Norbye
 */
public abstract class OccurrencesFinder<T extends Parser.Result> extends ParserResultTask<T> {
    /**
     * Set the caret position for which the current program element should
     * be computed. This method will be called before {@link #run} is called
     * to produce the set of highlights.
     */
    public abstract void setCaretPosition(int position);
    
    /**
     * Return a set of occurrence highlights computed by the last call to
     * {@link #run}.
     */
    public abstract @NonNull Map<OffsetRange, ColoringAttributes> getOccurrences();
}
