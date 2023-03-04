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

package org.netbeans.modules.j2ee.api.ejbjar;

import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;

/**
 *
 * @author Martin Adamek
 */
public class EnterpriseReferenceSupport {

    private EnterpriseReferenceSupport() {
    }

    public static void populate(MessageDestinationReference reference, String newName, MessageDestinationRef ref) {
        ref.setMessageDestinationRefName(newName != null ? newName : reference.getMessageDestinationRefName());
        ref.setMessageDestinationType(reference.getMessageDestinationType());
        ref.setMessageDestinationUsage(reference.getMessageDestinationUsage());
        ref.setMessageDestinationLink(reference.getMessageDestinationLink());
    }
    
    public static void populate(ResourceReference reference, String newName, ResourceRef ref) {
        ref.setResRefName(newName != null ? newName : reference.getResRefName());
        ref.setResType(reference.getResType());
        ref.setResAuth(reference.getResAuth());
        ref.setResSharingScope(reference.getResSharingScope());
        ref.setDescription(reference.getDefaultDescription());
    }
    
}
