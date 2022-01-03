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

import org.netbeans.modules.cnd.asm.core.assistance.RegisterUsageAccesor;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterUsageAccesor.PredefinedStatuses;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterUsageAccesor.RegisterStatus;
import org.netbeans.modules.cnd.asm.model.lang.Register;

class MockRegisterInfoAccessor implements RegisterUsageAccesor {
    
   private final RegisterStatus []stat;
   private final Register []regs;
   
   public MockRegisterInfoAccessor(Register []regs) {       
       this.regs = regs;
       
       stat = new RegisterStatus[regs.length];
   }
   
   public void setRegisterStatus(Register reg, RegisterStatus status) {
        for (int i = 0; i < regs.length; i++) {
            if (regs[i] == reg) {
                stat[i] = status;
            }
        }
   }
   
   public void setRegisterStatus(Collection<Register> regs, RegisterStatus status) {
       for (Register reg : regs) {
           setRegisterStatus(reg, status);
       }
   }
    
   public void clearStatuses() {
       for (int i = 0; i < regs.length; i++) {
           stat[i] = PredefinedStatuses.STATUS_DEF;
       }                     
   }
   
   public RegisterStatus getFor(Register reg) {
       for (int i = 0; i < regs.length; i++) {
           if (regs[i] == reg) {
               return stat[i];
           }
       }
       
       return null;
   }
      
}
