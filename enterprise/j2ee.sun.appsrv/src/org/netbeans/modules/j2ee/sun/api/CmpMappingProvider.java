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
package org.netbeans.modules.j2ee.sun.api;

import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.openide.filesystems.FileObject;

/**
 *
 * @author raccah
 */
public interface CmpMappingProvider {
    public void mapCmpBeans(FileObject sunCmpDDFO, OriginalCMPMapping[] mapping, SunCmpMappings existingMapping);
    public boolean removeMappingForCmp(SunCmpMappings sunCmpMappings, String beanName);
    public boolean renameMappingForCmp(SunCmpMappings sunCmpMappings, String oldBeanName, String newBeanName);
    public boolean removeMappingForCmpField(SunCmpMappings sunCmpMappings, String beanName, String fieldName);
    public boolean renameMappingForCmpField(SunCmpMappings sunCmpMappings, String beanName, String oldFieldName, String newFieldName);
}
