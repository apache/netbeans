/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
