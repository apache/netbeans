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

#ifndef V_POLICE_H
#define V_POLICE_H

//
// Header file for police class for "Freeway"
//

#include "maniac.h"

#define CLASS_POLICE 4

class Police : public Maniac {
protected:
    int flash_state;		// state of flashing lights

public:
    Police(int = 0, int = 0, double = 0.0, double = 0.0);

    virtual char   *classname()       { return (char *)"Police"; }
    virtual int     classnum()        { return CLASS_POLICE; }
    virtual double  vehicle_length();
    virtual void    recalc_pos();
    virtual void    draw(GdkDrawable *pix, GdkGC *gc, 
                        int x, int y, int direction_right, int scale, 
                        int xorg, int yorg, int selected);
};

#endif
