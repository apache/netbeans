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
package org.netbeans.modules.jakarta.web.beans.xml;

import java.util.List;


/**
 * 
 * This model represents beans.xml OM.
 * It is based on schema file last changed at 2009-12-02
 * http://fisheye.jboss.org/browse/~raw,r=5197/weld/api/trunk/cdi/src/main/resources/beans.xsd.   
 * 
 * @author ads
 *
 */
public interface Beans extends WebBeansComponent {
    
    String BEANS_ELEMENT = "beans-element";         // NOI18N
    
    String BEANS = "beans";                         // NOI18N

    List<BeansElement> getElements();
    void addElement( BeansElement element );
    void removeElement( BeansElement element );
    void addElement( int index , BeansElement element );
}
