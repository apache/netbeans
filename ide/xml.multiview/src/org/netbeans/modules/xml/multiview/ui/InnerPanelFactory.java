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
 * InnerPanelFactory.java
 *
 * Created on November 22, 2004, 6:45 PM
 */

package org.netbeans.modules.xml.multiview.ui;

/** InnerPanelFactory.java
 *  Factory for dynamic inner panels - bodies of section panels
 *
 * Created on November 22, 2004, 6:45 PM
 * @author mkuchtiak
 */
public interface InnerPanelFactory {
    /** Creates SectionInnerPanel object from the key, e.g. for bean obtained from DD-API
     */
    public SectionInnerPanel createInnerPanel(Object key);
}
