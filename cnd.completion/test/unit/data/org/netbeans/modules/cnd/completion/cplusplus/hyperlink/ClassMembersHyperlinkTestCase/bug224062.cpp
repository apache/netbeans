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

#define bug224062_Q_DISABLE_COPY(Class) \
Class(const Class &); \
Class &operator=(const Class &); 


template <typename A, typename B>
class bug224062_QMap {
    
}; 

class bug224062_ObjectTypeDescriptor {
public:
    
  bug224062_ObjectTypeDescriptor() {}
  
  bug224062_Q_DISABLE_COPY(bug224062_ObjectTypeDescriptor)
  
private:
    
    const bug224062_ObjectTypeDescriptor *superclass_descriptor_; 

    static bug224062_QMap<int, bug224062_ObjectTypeDescriptor> class_map;
    
    int fooo() {
        bug224062_ObjectTypeDescriptor *var = new bug224062_ObjectTypeDescriptor();
    }
     
};
