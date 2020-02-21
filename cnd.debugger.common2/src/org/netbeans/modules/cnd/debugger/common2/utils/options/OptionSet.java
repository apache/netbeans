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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.util.ArrayList;

public interface OptionSet {

    /**
     * Given the name of the option, find its instance
     */

    public OptionValue byName(String name);


    public OptionValue byType(Option type);

    public ArrayList<OptionValue> values();


    /**
     * Assign option values from that into this
     */

    public void assign(OptionSet that);


    /**
     * Assign non-client option values from 'that' into 'this'.
     */

    public void assignNonClient(OptionSet that);


    /**
     * Make a copy.
     */
    public OptionSet makeCopy();


    public void applyTo(OptionClient client);

    // OLD public OptionSet asLayers();


    /**
     * Mark options in 'this' set as dirty if they differ from the
     * corresponding options in 'that'.
     */

    public void deltaWithRespectTo(OptionSet that);


    /*
     * Mark all values as having been applied.
     */
    public void doneApplying();


    /**
     * Mark all options which are not the same as the defaults
     * as "dirty" (such that an apply will set them).
     */
    public void markChanges();

    public boolean isDirty();

    public void clearDirty();


    public void save();
    public void open();

    /*
     * Tag used in XML encoding.
     */

    public String tag();

    public String description();
}
