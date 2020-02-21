/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

//Implementation of base module class

#include <iostream>
#include <cstdlib>
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

