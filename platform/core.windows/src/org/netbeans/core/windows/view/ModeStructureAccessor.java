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


package org.netbeans.core.windows.view;





/**
 * Class which is used as an access point to data whih View is responsible
 * to process.
 *
 * @author  Peter Zavadsky
 */
interface ModeStructureAccessor {

    /** Gets root element accessor of split structure. */
    public ElementAccessor getSplitRootAccessor();

    /** Gets set of separate mode accessors. */
    public ModeAccessor[] getSeparateModeAccessors();

    /** Gets set of sliding mode accessors. */
    public SlidingAccessor[] getSlidingModeAccessors();

}

