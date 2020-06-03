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

class bug200673_ParCompactionManager {
public:
    bug200673_ParCompactionManager();
    bug200673_ParCompactionManager(const bug200673_ParCompactionManager& orig);
    virtual ~bug200673_ParCompactionManager();
private:

};

class bug200673_Class {
public:
    bug200673_Class();
    bug200673_Class(const bug200673_Class& orig);
    virtual ~bug200673_Class();
private:

public:
  void follow_header(void);
  void follow_header(bug200673_ParCompactionManager* cm);
};

void bug200673_Class::follow_header(void) {

}

void bug200673_Class::follow_header(bug200673_ParCompactionManager* cm) {

}