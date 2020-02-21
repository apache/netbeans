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

int x = 10;//#include <iostream>
int main(int argc, char**argv) {
    std::cout << "Welcome ..." << std::endl;

    // Prints arguments...
    if (argc > 1) {
        std::cout << std::endl << "Arguments:" << std::endl;
        for (int i = 1; i < argc; i++) {
            std::cout << i << ": " << argv[i] << std::endl;
        }
    }

    int i = 5;
    if (argc == 1) return argc + argv;
    if (argc == 2) return main(argc, argv);

    int a,c,d,e = 5;

    int b = 5;

    main(a,
        b,
        c);

    int x = 5;
    // comment
    int y = 5;

    if (argc == 1) { return argv; }

    x = ::x;

    int mas[5];
    int z[] = {5,2};
    for(int i = 2; i < 7; i++){
        mas[-2+i+z[i-i]-5] = 0;
        int g=0;
        mas[-2+i+z[i-i]-5+foo()] = 0;
    }

    return 0;
}
