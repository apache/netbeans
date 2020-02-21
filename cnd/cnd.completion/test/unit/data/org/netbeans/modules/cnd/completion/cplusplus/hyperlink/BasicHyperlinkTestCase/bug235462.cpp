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

namespace bug235462 {
    //#include <iostream>
    namespace std {
        static struct _cout {
            template <class T>
            _cout operator<<(T t);
        } cout;
        
        static const char* endl = "\n";
    }

    struct AAA {};
    struct BBB : AAA {};

    template <typename T1, typename T2>
    void roo(T1 t1, T2 t2) {
        std::cout << "roo 1" << std::endl;
    }

    template <typename T1, typename T2>
    void roo(T1 *t1, T2 *t2) {
        std::cout << "roo 2" << std::endl;
    }

    template <typename T1, typename T2>
    void roo(const T1 *t1, const T2 *t2) {
        std::cout << "roo 3" << std::endl;
    }

    template <typename T>
    void roo(T t1, T t2) {
        std::cout << "roo 4" << std::endl;
    }

    void roo(AAA t1, BBB t2) {
        std::cout << "roo 5" << std::endl;
    }

    void roo(int t1, int t2) {
        std::cout << "roo 6" << std::endl;
    }

    void roo(float t1, int t2) {
        std::cout << "roo 7" << std::endl;
    }

    void roo(double t1, int t2) {
        std::cout << "roo 8" << std::endl;
    }

    void roo(int t1, float t2) {
        std::cout << "roo 9" << std::endl;
    }

    void zoo(char t1, float t2) {
        std::cout << "zoo 1" << std::endl;
    }

    void zoo(int t1, double t2) {
        std::cout << "zoo 2" << std::endl;
    }

    void zoo(AAA a, BBB *b) {
        std::cout << "zoo 3" << std::endl;
    }

    template <typename T>
    void zoo(T a, BBB *b) {
        std::cout << "zoo 4" << std::endl;
    }

    template <typename T>
    void zoo(AAA a, T b) {
        std::cout << "zoo 5" << std::endl;
    }

    int main() {
        AAA a;
        AAA *pa;
        const AAA *pca;
        BBB b;
        BBB *pb;
        const BBB *pcb;    

        roo(a, pa);
        roo(pa, pb);
        roo(pca, pcb);
        roo(a, a);
        roo(a, b);

        int ip;
        float fp;
        double dp;
        char cp;
        unsigned char ucp;
        bool bp;

        roo(ip, ip);
        roo(fp, ip);
        roo(dp, ip);
        roo(ip, fp);

        zoo(cp, fp);
        zoo(cp, dp);
        zoo(a, pb);
        zoo(b, pb);
        zoo(b, b);
        
        roo((const AAA*)pa, (const BBB*)pb);

        return 0;
    } 
}