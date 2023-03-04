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

package org.netbeans.lib.uihandler;


/** A callback interface of a decorated representation of LogRecord.
 * Should be passed into {@link LogRecords#decorate} and will receive
 * appropriate call-backs.
 *
 * @since 1.13
 */
public interface Decorable {
    
    /**
     * Set the name.
     * @param n the name of the log record
     */
    public void setName(String n);

    /**
     * Set the display name.
     * @param n the display name of the log record
     */
    public void setDisplayName(String n);

    /**
     * Set the icon base.
     * @param base the icon base, including the extension, of the log record
     */
    public void setIconBaseWithExtension(String base);

    /**
     * Set the short description.
     * @param shortDescription the short description of the log record
     */
    public void setShortDescription(String shortDescription);
}
