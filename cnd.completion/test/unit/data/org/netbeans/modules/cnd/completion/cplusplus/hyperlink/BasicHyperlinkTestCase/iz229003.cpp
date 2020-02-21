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

typedef __builtin_va_list __gnuc_va_list;
typedef __gnuc_va_list va_list;
typedef int A229003;
int(b229003); 
A229003(c229003);
    
int foo229003(...) {
    va_list(arglist229003);
    int(a229003);
    foo229003(arglist229003);
    return arglist229003 != 0 && a229003 == c229003 + b229003;
}
  
int boo229003(void (fun1)(int param), void (*fun2)(void* param), void (fun3)(double param)) { 
    fun1(0); 
    fun2(0);
    fun3(1);
}

