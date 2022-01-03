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

void foo1(int i) {
    goto label;    
    label: i++;    
}

void foo2(int i) {
    goto label;    
    if(true) {
        label: i++;
    }    
}

void foo3(int i) {
    goto label;    
    if(true) {
        i++;
    } else {
        label: i++;
    }   
}

void foo4(int i) {
    goto label;    
    for(;;) {
        label: i++;
    }   
}

void foo5(int i) {
    goto label;    
    while(true) {
        label: i++;
    }   
}

void foo6(int i) {
    goto label;    
    do {
        label: i++;
    } while(true);
}

void foo7(int i) {
    goto label;    
    {
        label: i++;
    }  
}

void foo8(int i) {
    switch (i) {
        case 1:
            label: i++;
            break;
        case 2:
            goto label; 
            break;
    }
}

