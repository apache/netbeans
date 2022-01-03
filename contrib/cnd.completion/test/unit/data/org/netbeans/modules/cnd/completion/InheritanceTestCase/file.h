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
public:
    int aPub;
    void aPubFun();
protected:
    int aProt;
    void aProtFun() {}
private:
    int aPriv;
    void aPrivFun() {}
};
 
class ClassB : private ClassA {
public:
    int bPub;  
    void bPubFun() {}
protected:
    int bProt;
    void bProtFun();
private:
    int bPriv;
    void bPrivFun() {}
};

class ClassC {
public:
    int cPub;   
    void cPubFun() {}
protected:
    int cProt;
    void cProtFun() {}
private:
    int cPriv;
    void cPrivFun();
};
 
class ClassD : public ClassB, protected ClassC {
public:
    int dPub;
    void dPubFun();
protected:
    int dProt;
    void dProtFun() {}
private:
    int dPriv;
    void dPrivFun() {}
};

class ClassE : protected ClassC {
public:
    int ePub;
    void ePubFun();
protected:
    int eProt;
    void eProtFun() {}
private:
    int ePriv;
    void ePrivFun() {}
};

#endif

