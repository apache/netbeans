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

#if !defined MODULE_H
#define MODULE_H

#include <iostream>

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
