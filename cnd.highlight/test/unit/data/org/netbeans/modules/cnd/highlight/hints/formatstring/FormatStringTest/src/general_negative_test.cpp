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

#include <stdio.h>

int main(int argc, char** argv) {
    short int si = -2;
    float f = 12.8f;
    int i = -1;
    
    // Errors
    printf("No args", f);
    printf("%f", f, f);
    printf("%f");
    printf("%.*f", f);
    printf("%.*f", f, f);
    printf("%*f", f);
    printf("%*f", f, f);
    
    printf("%#hd", si);
    printf("%hf", f);
    printf("%ho", f);
    printf("%'he", f);
    printf("%hc", i);
    
    printf("%#hhd", si);
    printf("%hhf", f);
    printf("%hho", f);
    printf("%'hhe", f);
    printf("%hhc", i);
    
    printf("%#ld", si);
    printf("%lo", f);
    printf("%lc", i);
    printf("%k", i);
    printf("%'#0c", i);
    
    printf("%#lld", si);
    printf("%llo", f);
    printf("%llc", i);
    
    printf("%#lld", si);
    printf("%llo", f);
    printf("%llc", i);
    
    printf("%#zd", si);
    printf("%zo", f);
    printf("%zc", i);
    
    printf("%#td", si);
    printf("%to", f);
    printf("%tc", i);
    
    printf("%d", (long)i);
    
    return 0;
}