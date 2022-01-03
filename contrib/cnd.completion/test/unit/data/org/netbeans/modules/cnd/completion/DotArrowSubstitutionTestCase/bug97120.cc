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

namespace ns97120{
    class Class97120;
    typedef Class97120* PClass97120;
    typedef PClass97120 PPClass97120;
    typedef PPClass97120 PPPClass97120;
    
    class Class97120 {
    public:
        Class97120(int i);
        int foo();
        Class97120* ptr();
        PClass97120 pPtr();
        PPClass97120 ppPtr();
        PPPClass97120 pppPtr();
    private:

    };


    
    /*
     * 
     */
    int main97120(int argc, char** argv) {
            Class97120 c(10), d(1);
            PClass97120 pA = new Class97120(10);
            PPClass97120 ppA = new Class97120(1);
            PPPClass97120 pppA = new Class97120(3);
            
            /* insert text above this line */
            
            
    }
}
