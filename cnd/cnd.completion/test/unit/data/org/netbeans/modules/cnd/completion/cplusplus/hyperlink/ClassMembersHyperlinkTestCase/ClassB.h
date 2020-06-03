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

#ifndef __B__H__
#define __B__H__
typedef int ostream;
#include "ClassA.h"

class ClassB : public ClassA {
public:
    enum type { MEDIUM,  HIGH };

    ClassB() {
    }

    ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
    }

    ClassB(int type1, int type2 = HIGH);

    void method(int a);

    void method(const char*);

    void method(char*, double);

    void method(char*, char*);
private:
    int myType1;
    int myType2;

public:
    void* myPtr;
    int myVal;

public:
    void setDescription(const char* description);

    void setDescription(const char* description, const char* vendor, int type, int category, int units);

    void setDescription(const ClassB& obj);

};

#endif 
