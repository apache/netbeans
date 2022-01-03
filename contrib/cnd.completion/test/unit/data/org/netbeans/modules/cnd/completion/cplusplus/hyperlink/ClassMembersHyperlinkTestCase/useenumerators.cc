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

enum { maxMediaChunks = 2 };

struct MediaSample {
    int chunk[maxMediaChunks]; // 
};

enum Style {
    feminineStyle,
    masculineStyle,
    brashStyle,
    nStyles
};

class Fad {
public:
    static const char* s_styleNames[ nStyles ];
};

const char* Fad::s_styleNames[ nStyles ] = { // 
    "feminine",
    "masculine",
    "brash"
};

typedef struct ClassOfUnnamedTypedef {
    const char *field;
};

template <int> class AEnum { typedef int Type; };

class BVV {
    enum { VV = 1 };
    AEnum<VV> a;
};

void fooWW() {
    enum { WW = 2 };
    AEnum<WW> b;
    ClassOfUnnamedTypedef obj;
    obj.field;
}

enum auto_event { EVENT_BUFADD };
typedef enum auto_event event_T;
static struct event_name  {
    char *name;
    event_T event;
} event_names[] = { {"BufAdd", EVENT_BUFADD} };

class TypeTraits {
    enum {
        isFloat = 1
    };

    typedef AEnum<isFloat> ParameterType;
    typedef AEnum<isFloat>::Type Type;
};
