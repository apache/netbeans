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

#if !defined NAME_LIST_H
#define NAME_LIST_H

enum tDiscount {tnone, tretail, trepeat};

class NameList // ?database? of names. Singleton class.
{
    private:
        static NameList* pList; //used to implement singleton design pattern
        
        int nameCount;
        int maxIndex;
        
        char** Name; //2D character array ... name strings ... dynamically allocated in constructor
        char** ID; //2D character array ... ID strings ... dynamically allocated in constructor
        int* Index;
        int* Discount;
        
        protected:
            NameList(); //constructor ... called indirectly through singleton pattern (static ListInstance)
            NameList(const NameList& obj); //copy constructor ... not called directly
            NameList& operator=(NameList& obj); //overload of assignment operator ... not called directly
            ~NameList(); //destructor ... not called directly
            public:
                static NameList* ListInstance(); //used to implement singleton design pattern (check plist)
                int FindCustomer(char* name); //returns customer index within namelist data structure
                char* GetName(int index);
                char* GetID(int index);
                int GetDiscount(int index);
                void DisplayList();
}
;//note that ";" is required at end of class definition header file

#endif //NAME_LIST_H
