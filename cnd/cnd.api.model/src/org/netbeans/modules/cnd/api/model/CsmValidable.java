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

package org.netbeans.modules.cnd.api.model;

/**
 * This interface provides a way to obtain validity of the instance of CSM object.
 * Returns true if the given object is valid, otherwise false.
 * It's always false upon project closing or reparsing of the file that contains the object.
 * If you store object, say, in a field, then 
 * you have to check isValid() prior to calling any other method;
 * otherwise results are inpredictable.
 */

public interface CsmValidable {

    public boolean isValid();
}
