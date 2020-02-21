/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

//Implementation of disk module class

#include "disk.h"

Disk::Disk(int type /*= SINGLE */, int size /*= T100 */, int units /*= 1 */) :
    Module("Disk storage", "generic", type, size, units) {
        ComputeSupportMetric();
}
    
/*
 * Heuristic for disk module complexity is based on number of disk sub-modules 
 * and architecture. Size of individual disks is not considered in heuristic
 */

void Disk::ComputeSupportMetric() { 
    int metric = 200 * GetUnits();
     
    if (GetTypeID() == RAID) {
        metric += 500;
    }
        
    SetSupportMetric(metric);
}
    
const char* Disk::GetType() const {
    switch (GetTypeID()) {
        case SINGLE: 
            return "Single disk";
        
        case RAID: 
            return "Raid";
         
        default: 
            return "Undefined";
    }
}
    
const char* Disk::GetCategory() const {
    switch (GetCategoryID()) {
        case T100: 
            return "100 Gb disk";
        
        case T200: 
            return "200 Gb disk";
        
        case T500: 
            return "500 Gb or more";
            
        default: 
            return "Undefined";
    }
}
    
// end disk.cc
