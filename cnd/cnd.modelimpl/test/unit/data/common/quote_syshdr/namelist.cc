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

#include "namelist.h"
#include <cstdlib>
#include <iostream>
#include <cstring>
using namespace std;


NameList* NameList::ListInstance() {
    if (pList) //Check to see if NameList already exists (plist not null)
    {
        return pList;
    }
    else {
        pList=new NameList; //Create NameList. Creator function will be called.
        return pList;
    }
}

NameList::NameList() {
    maxIndex=4;
    const int sz=80;
    
    Name=new char* [maxIndex];
    ID=new char* [maxIndex];
    
    for(int i=0;i<=maxIndex;i++) {
        *(Name+i)=new char[sz];
        *(ID+i)=new char[sz];
    }
    
    Index=new int[maxIndex];
    Discount=new int[maxIndex];
    
    for (int i=0; i<=maxIndex; i++) {
        switch(i) {
            case 0: Name[0]=(char*)"XYZ";ID[0]=(char*)"111";Index[0]=0;Discount[0]=trepeat;break;
            case 1: Name[1]=(char*)"RSG";ID[1]=(char*)"112";Index[1]=1;Discount[1]=trepeat;break;
            case 2: Name[2]=(char*)"AEC";ID[2]=(char*)"113";Index[2]=2;Discount[2]=trepeat;break;
            case 3: Name[3]=(char*)"John";ID[3]=(char*)"0";Index[3]=3;Discount[3]=tretail;break;
            case 4: Name[4]=(char*)"Mary";ID[4]=(char*)"0";Index[4]=4;Discount[4]=tretail;break;
            default:	;
        }
    }
    
}

NameList::NameList(const NameList& obj) //copy constructor is not supported for this class
{
}

NameList& NameList::operator=(NameList& obj) //overload of assignment not supported for this class
{
    return *this;
}

NameList::~NameList() {
    delete [] Name;
    delete [] ID;
    delete [] Index;
    delete [] Discount;
    
}

int NameList::FindCustomer(char* name) {
    for (int i=0; i<=maxIndex; i++) {
        if (strcmp(Name[i], name)==0)
            return Index[i];
    }
    return -1;//not found
}

char* NameList::GetName(int index) {
    return Name[index];
}

char* NameList::GetID(int index) {
    return ID[index];
}

int NameList::GetDiscount(int index) {
    return Discount[index];
}

void NameList::DisplayList() {
    cout<<"**Namelist content**"<<endl;
    for (int i=0; i<=maxIndex; i++)
        cout<<"Name: "<<Name[i]<<" Discount code: "<<Discount[i]<< " ID: "<<ID[i]<<endl;
    cout<<endl;
}

//end namelist.cc
