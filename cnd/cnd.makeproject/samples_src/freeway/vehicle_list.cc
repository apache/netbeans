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

#include "vehicle_list.h"


List* List::prev(Vehicle *veh) {
    List *i = this->find(veh);

    if (i->hasValue()) {
        return i->p;
    } else {
        return NULL;    // couldn't find vehicle
    }
}


List* List::next(Vehicle *veh) {
    List *i = this->find(veh);

    if (i->hasValue()) {
        return i->n;
    } else {
        return NULL;    // couldn't find vehicle
    }
}


List* List::find(Vehicle *veh) {
    for (List *i = this->first(); i->hasValue(); i = i->n) {
        if (i->v->name() == veh->name()) {
            return i;
        }
    }
    return NULL; // couldn't find vehicle
}

void List::remove(Vehicle *veh) {
    List *i = this->find(veh);

    if (i->hasValue()) {
        i->p->n = i->n;
        i->n->p = i->p;
    }
}

void List::append(Vehicle *veh) {
    List *i = new List();

    i->v = veh;
    i->n = this;
    i->p = p;
    p->n = i;
    p    = i;
}

void List::prepend(Vehicle *veh) {
    List *i = new List();

    i->v = veh;
    i->p = this;
    i->n = n;
    n->p = i;
    n    = i;
}

void List::insert(Vehicle *veh) {
    // Scan over list looking for element which is smaller than v and then insert v after it
    for (List *i = this->last(); i->hasValue(); i = i->p)  {
        if (i->v->pos() < veh->pos()) {
            i->insertAfter(veh);
            return;
        }
    }

    // If there is no element smaller than v, insert it at the beginning
    this->insertAfter(veh);
}

ostream& operator<<(ostream & o, List & l)
{ 
    o << "{ ";

    for (List *i = l.first(); i->hasValue(); i = i->next()) {
        o << i->value();
        if (i != l.last()){
            o << " , ";
        }
    }

    o << " }" << endl;

    return o;
}

