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

#ifndef VEHICLE_LIST_H
#define VEHICLE_LIST_H

#include <string.h>

#include "vehicle.h"

class List {
protected:
    List *n, *p;			// next and previous
    Vehicle *v;			// this vehicle

public:
    List()              { n = p = this; v = 0; }
    Vehicle *value()    { return v; }
    int      hasValue() { return (this && v); }
    int      isEmpty()  { return (n == this); }
    List *   first()    { return n; }
    List *   last()     { return p; }
    List *   next()     { return n; }
    List *   prev()     { return p; }
    List *   next   (Vehicle*);
    List *   prev   (Vehicle*);
    List *   find   (Vehicle*);
    void     remove (Vehicle*);
    void     append (Vehicle*);
    void     prepend(Vehicle*);
    void     insert (Vehicle*);
};

#define insertAfter  prepend
#define insertBefore append

ostream & operator<< (ostream &, List &);

#endif
