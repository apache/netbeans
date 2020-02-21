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

typedef int myInt;
class ClassA {
public:
    virtual ~ClassA(); // in test testDestructors
    
public:
    ClassA(); // in test testConstructors

    void publicFoo(); // in test testPublicMethods
    void publicFoo(int a); // in test testPublicMethods
    void publicFoo(int a, double b); // in test testPublicMethods
    void publicFoo(ClassA a); // !!!FAILED!!!
    void publicFoo(const ClassA &a); // !!!FAILED!!!
    
    static void publicFooSt(); // in test testPublicMethods
    
protected:
    ClassA(int a); // in test testConstructors
    
    void protectedFoo(); // in test testProtectedMethods
    void protectedFoo(int a); // in test testProtectedMethods
    void protectedFoo(int a, double b); // in test testProtectedMethods
    void protectedFoo(const ClassA* const ar[]);    // !!!FAILED!!!
    
    static void protectedFooSt(); // in test testProtectedMethods
private:
    ClassA(int a, double b); // in test testConstructors
    void privateFoo(); // in test testPrivateMethods
    void privateFoo(int a); // in test testPrivateMethods
    void privateFoo(int a, double b); // in test testPrivateMethods
    void privateFoo(const ClassA *a); // in test testPrivateMethods
    
    static void privateFooSt(); // in test testPrivateMethods
// members
public:
    int publicMemberInt;
    double publicMemberDbl;
    static int publicMemberStInt;
    
protected:
    int protectedMemberInt;
    double protectedMemberDbl;
    static int protectedMemberStInt;
    
private:
    int privateMemberInt;
    double privateMemberDbl;
    static int privateMemberStInt;
    
//operators
public:
    ClassA& operator= (const ClassA& obj); // in test testOperators
protected:
    ClassA& operator+ (const ClassA& obj); // in test testOperators
private:
    ClassA& operator- (const ClassA& obj); // in test testOperators
    
private:
    ClassA* classMethodRetClassAPtr();
    const ClassA& classMethodRetClassARef();
    
    typedef int myInnerInt;

    myInt classMethodRetMyInt();
    
    myInnerInt classMethodRetMyInnerInt();
    
private:
    friend ostream& operator<< (ostream&, const ClassA&);

public:
    friend void friendFoo();
};


