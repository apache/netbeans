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

#define GOTO_MACRO(statement) \
if (!statement) { \
    goto done; \
} \

int myFunction() {
    
    return 1;
}
/*
 * 
 */
int main(int argc, char** argv) {

    /* If we click on the function to go to definition, it works correctly */
    myFunction();
    
    /* If we click on the function to go the definition, it will go to done 
     * Indeed, if we position the mouse over the function call, ww'll see the
     * tool tip pointing to "label done" instead of to "int myFunction()", as
     * occurs in the previous call.
     */
    GOTO_MACRO(myFunction());
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 done:
    
    return 0;
}
