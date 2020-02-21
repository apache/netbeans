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


package org.netbeans.modules.cnd.asm.core.assistance;

import java.util.Collection;

import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.openide.util.NbBundle;

public interface RegisterUsageAccesor {
        
   void setRegisterStatus(Register reg, RegisterStatus status);
   void setRegisterStatus(Collection<Register> regs, RegisterStatus status);
   void clearStatuses();
          
   public interface RegisterStatus {
        String getStatusName();
   }       

   public enum PredefinedStatuses implements RegisterStatus {
       STATUS_DEF(""),
       STATUS_USED(NbBundle.getMessage(PredefinedStatuses.class, "REG_USAGE_USED")),
       STATUS_ARG(NbBundle.getMessage(PredefinedStatuses.class, "REG_USAGE_ARGUMENT")),
       STATUS_RET(NbBundle.getMessage(PredefinedStatuses.class, "REG_USAGE_RETURNED")),
       STATUS_READ(NbBundle.getMessage(PredefinedStatuses.class, "REG_USAGE_READ")),
       STATUS_WRITE(NbBundle.getMessage(PredefinedStatuses.class, "REG_USAGE_WRITE"));

       private final String name;

       private PredefinedStatuses(String name) {
           this.name = name;
       }

       public String getStatusName() {
           return name;
       }
       
        @Override
        public String toString() {
           return getStatusName();
       }
    }
}
