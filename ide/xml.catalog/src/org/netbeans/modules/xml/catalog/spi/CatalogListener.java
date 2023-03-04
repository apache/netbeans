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
package org.netbeans.modules.xml.catalog.spi;

import java.util.EventListener;

/**
 * A callback interface notifying catalog content changes.
 * <p>
 * It is <b>a callback interface</b> so invokers must be aware of consequences.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface CatalogListener extends EventListener {

    /** Given public ID has changed - created. */
    public void notifyNew(String publicID);

    /** Given public ID has changed - disappeared. */
    public void notifyRemoved(String publicID);

    /** Given public ID has changed. */
    public void notifyUpdate(String publicID);
    
    /** All entries are invalidated. */
    public void notifyInvalidate();
    
}
