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

#if !defined MODULE_H
#define MODULE_H

//#include <iostream>

using namespace std;

// Base class

class Module {
public:
    Module();
    Module(const char* description, const char* vendor, int type, int category, int units);
    virtual ~Module(); //destructor is virtual since derived classes may have distinct destructor

    Module(const Module& obj); //copy constructor
    Module& operator= (const Module& obj); //overload of assignment operator "="

    void SetDescription(const char* description);
    const char* GetDescription() const;

    void SetVendor(const char* v);
    const char* GetVendor() const;

    void SetType(int type);
    int GetTypeID() const;
    virtual const char* GetType() const = 0;

    void SetCategory(int category);
    int GetCategoryID() const;
    virtual const char* GetCategory() const = 0;

    void SetUnits(int u);
    int GetUnits() const;

    void SetSupportMetric(int m);
    int GetSupportMetric() const;

protected:    
    virtual void ComputeSupportMetric() = 0; //metric is defined in derived classes

 private:
    string  description;
    string  vendor; //this anticipates future functionality
    int     type;
    int     category;
    int     units;
    int     supportMetric; //default value

friend ostream& operator<< (ostream&, const Module&);
};

#endif // MODULE_H
