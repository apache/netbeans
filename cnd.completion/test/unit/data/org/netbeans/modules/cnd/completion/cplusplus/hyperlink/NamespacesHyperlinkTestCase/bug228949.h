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

/* 
 * File:   Project228949.h
 * Author: 
 *
 * Created on August 21, 2013, 1:57 PM
 */

#ifndef PROJECT228949_H
#define	PROJECT228949_H
 
namespace NNOther228949 {
    struct BBB228949 {
        string& str() { return 0; }
    };
}

namespace NN228949 {
    class AAA228949 : public NNOther228949::BBB228949 {
    private:
        vector<string> field;
    public:
        AAA228949() : BBB228949() {
            
        }
        vector<string>& get() {
            return field;
        }

        int size() {
            return field.size() + str.length();
        }   
        
        string& getStr() { 
            BBB228949 base;
            return base.str();
        }
        
        string& getSSS();

    private:
        string str;
    };
}

#endif	/* PROJECT228949_H */
