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
/*
 * Valued.java
 *
 * Created on October 10, 2002, 1:08 AM
 */

package org.netbeans.performance.spi;

/** Interface defining objects that represent a value.  Used to allow
 * for writing Comparators and ElementFilters that don't need to know
 * the details of the object, since they compare only one attribute -
 * this makes it possible to write them in a generic way.
 *
 * @author  Tim Boudreau
 */
public interface Valued {
    public Object getValue();
}
