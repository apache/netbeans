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

#ifndef V_MANIAC_H
#define V_MANIAC_H

//
// Header file for maniac class for "Freeway"
//

#include "vehicle.h"
#include "sports.h"

#define CLASS_MANIAC 3

class Maniac : public Sports_car {
protected:

public:
    Maniac(int = 0, int = 0, double = 0.0, double = 0.0);

    virtual char   *classname()       { return (char *)"Maniac"; }
    virtual int     classnum()        { return CLASS_MANIAC; }

    virtual void    check_lane_change(Vehicle *in_front, void *neighbors);
    virtual double  optimal_dist(Vehicle *in_front);
    virtual int     limit_speed(int limit);
};

#endif
