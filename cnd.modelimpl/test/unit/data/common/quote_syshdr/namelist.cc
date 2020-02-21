/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
