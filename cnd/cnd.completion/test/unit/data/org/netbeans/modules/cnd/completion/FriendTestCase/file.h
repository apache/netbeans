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

#ifndef _FILE_H_
#define _FILE_H_

class ClassC;

class ClassA {
    friend class ClassC;
    public:
    int aPub;
    int static aPubSt;
    void aPubFun();
    void static aPubFunSt() {};
    protected:
    int aProt;
    int static aProtSt;
    void aProtFun() {}
    void static aProtFunSt() {};
    private:
    int aPriv;
    int static aPrivSt;
    void aPrivFun() {}
    void static aPrivFunSt() {};
};
 
class ClassB : private ClassA {
    friend void friendOfB();
    public:
    int bPub;  
    int static bPubSt;
    void bPubFun() {}
    void static bPubFunSt();
    protected:
    int bProt;
    int static bProtSt;
    void bProtFun() {};
    void static bProtFunSt() {};
    private:
    int bPriv;
    int static bPrivSt;
    void bPrivFun() {}
    void static bPrivFunSt();
};

class ClassC {
    public:
    void cPubFun();
    protected:
    void cProtFun();    
};

class ClassD : public ClassB {
    public:
    int dPub;  
    int static dPubSt;
    void dPubFun();
    void static dPubFunSt() {};
    protected:
    int dProt;
    int static dProtSt;
    void dProtFun() {};
    void static dProtFunSt() {};
    private:
    int dPriv;
    int static dPrivSt;
    void dPrivFun() {}
    void static dPrivFunSt() {};
};

class ClassE : public ClassA {
    public:
    int dEub;  
    int static dEubSt;
    void dEubFun() {};
    void static dEubFunSt() {};
    protected:
    int dErot;
    int static dErotSt;
    void dErotFun() {};
    void static dErotFunSt() {};
    private:
    int dEriv;
    int static dErivSt;
    void dErivFun() {}
    void static dErivFunSt() {};
};
#endif
