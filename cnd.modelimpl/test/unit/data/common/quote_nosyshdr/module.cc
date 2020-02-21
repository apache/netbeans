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
//Implementation of base module class

//#include <iostream>
//#include <cstdlib>
#include "module.h"

Module::Module() {
    SetDescription("undefined");
    SetVendor("undefined");
}

Module::Module(const char* _description, const char* _vendor, int _type, int _category, int _units) {
    SetDescription(_description);
    SetVendor(_vendor);
    SetType(_type);
    SetCategory(_category);
    SetUnits(_units);
}

//copy constructor
Module::Module(const Module& obj) {
    SetDescription(obj.GetDescription());
    SetVendor(obj.GetVendor());
    SetType(obj.type);
    SetUnits(obj.units);
    SetSupportMetric(obj.GetSupportMetric());
}

Module& Module::operator= (const Module& obj) {
    SetDescription(obj.GetDescription());
    SetVendor(obj.GetVendor());
    SetType(obj.type);
    SetUnits(obj.units);
    SetSupportMetric(obj.GetSupportMetric());
    
    return *this;
}

Module::~Module() {
}

void Module::SetDescription(const char* new_description) {
    description = new_description;
}

const char* Module::GetDescription() const {
    return description.c_str();
}

void Module::SetVendor(const char* new_vendor) {
    vendor = new_vendor;
}

const char* Module::GetVendor() const {
    return vendor.c_str();
}

void Module::SetType(int new_type) {
    type = new_type;
}

int Module::GetTypeID() const {
    return type;
}

void Module::SetCategory(int new_category) {
    category = new_category;
}

int Module::GetCategoryID() const {
    return category;
}

void Module::SetUnits(int new_units) {
    units = new_units;
}

int Module::GetUnits() const {
    return units;
}

void Module::SetSupportMetric(int new_metric) {
    supportMetric = new_metric;
}

int Module::GetSupportMetric() const {
    return supportMetric;
}

ostream& operator <<(ostream& output, const Module& module) {
    
    output << "** " << module.GetDescription() << " module data **" << endl;
    output << "Module type: " << module.GetType() << endl;
    output << "Module category: " << module.GetCategory() << endl;
    output << "Number of sub-modules: " << module.GetUnits() << endl;
    output << "Module support metric: " << module.GetSupportMetric() << endl;
    
    return output;
}
 
// end module.cc

