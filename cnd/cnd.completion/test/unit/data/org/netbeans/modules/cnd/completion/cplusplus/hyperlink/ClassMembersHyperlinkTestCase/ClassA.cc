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

#include "ClassA.h" // in test
// members
/*static*/ int ClassA::publicMemberStInt = 1;
/*static*/ int ClassA::protectedMemberStInt = 2;
/*static*/ int ClassA::privateMemberStInt = 3;
    
ClassA::ClassA() : privateMemberInt(1) { // in test testConstructors
    
}

ClassA::ClassA(int a) { // in test testConstructors
    
}

ClassA::ClassA(int a, double b) { // in test testConstructors
    
}

ClassA::~ClassA() { // in test testDestructors
    
}

void ClassA::publicFoo() { // in test testPublicMethods
    
}
void ClassA::publicFoo(int a) { // in test testPublicMethods
}

void ClassA::publicFoo(int a, double b) { // in test testPublicMethods
}

void ClassA::publicFoo(ClassA a) { // !!!FAILED!!!
}

void ClassA::publicFoo(const ClassA &a) { // !!!FAILED!!!
}

/*static*/ void ClassA::publicFooSt() {  // in test testPublicMethods
}

void ClassA::protectedFoo() {  // in test testProtectedMethods    
}

void ClassA::protectedFoo(int a) {      // in test testProtectedMethods
}

void ClassA::protectedFoo(int a, double b) {  // in test testProtectedMethods    
}

void ClassA::protectedFoo(const ClassA* const ar[]) {    // !!!FAILED!!!  
}

/*static*/ void ClassA::protectedFooSt() {  // in test testProtectedMethods     
}

void ClassA::privateFoo() {      // in test testPrivateMethods
}

void ClassA::privateFoo(int a) {      // in test testPrivateMethods
}

void ClassA::privateFoo(int a, double b) {      // in test testPrivateMethods
}

void ClassA::privateFoo(const ClassA *a) {      // in test testPrivateMethods
}

/*static*/ void ClassA::privateFooSt() {      // in test testPrivateMethods
}

////////////
// operators
ClassA& ClassA::operator= (const ClassA& obj) { // in test testOperators
    return *this;
}

ClassA& ClassA::operator+ (const ClassA& obj) { // in test testOperators
    return *this;
}

ClassA& ClassA::operator- (const ClassA& obj) { // in test testOperators
    return *this;
}

ClassA* ClassA::classMethodRetClassAPtr() {
    return this;
}

const ClassA& ClassA::classMethodRetClassARef() {
    return this;
}

myInt ClassA::classMethodRetMyInt() {
    return 0;
}

myInnerInt ClassA::classMethodRetMyInnerInt() {
    return 0;
}

ostream& operator <<(ostream& output, const ClassA& item) {
    output << item.privateMemberInt << customer.privateMemberDbl;
    return output;
}

void friendFoo() {
    int i = ClassA::publicMemberStInt;
}
