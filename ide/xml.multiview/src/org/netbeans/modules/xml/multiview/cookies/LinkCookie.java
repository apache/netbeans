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

package org.netbeans.modules.xml.multiview.cookies;

/**
 * This interface should be implemented by classes that need
 * to respond to pressing of links. See related class 
 * {@link org.netbeans.modules.xml.multiview.ui.LinkButton}.
 * 
 * Created on November 19, 2004, 8:52 AM
 * @author mkuchtiak
 */
public interface LinkCookie {
    
    /**
     * Invoked when a button representing a link is pressed.
     * @param ddBean the model that is affected by the link.
     * @param ddProperty the property of the given <code>ddBean</code>
     * that is affected by the link.
     */
    public void linkButtonPressed(Object ddBean, String ddProperty);
}
