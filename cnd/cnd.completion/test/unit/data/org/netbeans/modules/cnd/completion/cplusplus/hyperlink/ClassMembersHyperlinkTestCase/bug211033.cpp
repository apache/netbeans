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

class bug211033_ClassOuter {
public:
    bug211033_ClassOuter();
    bug211033_ClassOuter(const bug211033_ClassOuter& orig);
    virtual ~bug211033_ClassOuter();

    class bug211033_Class {
    public:
        bug211033_Class();
        bug211033_Class(const bug211033_Class& orig);
        virtual ~bug211033_Class();
    private:
        class bug211033_StringRef;
        bug211033_StringRef* pNext;
    };
private:

};

class bug211033_Other {
public:
    bug211033_Other();
    bug211033_Other(const bug211033_Other& orig);
    virtual ~bug211033_Other();
private:
    class bug211033_StringRef* pNext;
};

class bug211033_StringRef {
public:
    void foo();
};

class bug211033_ClassOuter::bug211033_Class::bug211033_StringRef {
public:
    void boo();
};

bug211033_Other::bug211033_Other() {
    pNext = 0;
    pNext->foo();
}

bug211033_ClassOuter::bug211033_Class::bug211033_Class() {
    pNext = 0;
    pNext->boo(); // unresolved boo
}