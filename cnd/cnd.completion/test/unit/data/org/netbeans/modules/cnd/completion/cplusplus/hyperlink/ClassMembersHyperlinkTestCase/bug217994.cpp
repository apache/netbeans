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

class bug217994_String {
public:
    void c_str() {
    }
};

class bug217994_StringClass
{
    public:
        void GetString( char* str , unsigned int size, const char* format_str = "FORMAT" ) const;
        bug217994_String GetString( const char* format_str = "FORMAT" ) const;
};

int bug217994_main()
{
    bug217994_StringClass stringClass;
    stringClass.GetString().c_str();
    return 0;
}