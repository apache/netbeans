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

//
// Implementation of police driver class for "Freeway".
//

#include <math.h>
#include "vehicle_list.h"
#include "police.h"

const double DELTA_T = 0.0000555; // 1/5 sec expressed in hours
const double OPT_DT = 0.0001; // optimal buffer (in hrs) in front
const double CAR_LEN = 0.004; // car length (in miles) (roughly 16 ft)
const double BRAKE_DV = 6.0; // 30 mph / sec for 1/5 sec
const int CAR_LENGTH = 8;
const int CAR_WIDTH = 4;

Police::Police(int i, int l, double p, double v) {
    classID = CLASS_POLICE;
    name_int = i;
    lane_num = l;
    position = p;
    velocity = v;
    state = VSTATE_MAINTAIN;
    max_speed = 150;
    xlocation = 0;
    ylocation = 0;
    change_state = 0;
    restrict_change = 0;
    absent_mindedness = 0;
    flash_state = 0;
}

double
Police::vehicle_length() {
    return CAR_LEN;
}

void
Police::recalc_pos() {
    // Update position based on velocity
    position += velocity * DELTA_T;

    // Update state of flashing lights
    flash_state = 1 - flash_state;
}

void
Police::draw(GdkDrawable *pix, GdkGC *gc, int x, int y,
        int direction_right, int scale, int xorg, int yorg, int selected) {
    extern GdkColor *color_red, *color_blue;

    this->xloc(x);
    this->yloc(y);

    // If I am heading to the right, then I need to draw brick to the left of 
    // front of car.  If I am heading left, draw brick to the right.
    if (direction_right) {
        x -= (CAR_LENGTH - 1);
    }

    int l = x * scale + xorg;
    int t = y * scale + yorg;
    int w = CAR_LENGTH * scale;
    int h = CAR_WIDTH * scale;
    int w2 = w / 2;
    int h2 = h / 2;

    // Draw brick.
    if (flash_state) {
        gdk_gc_set_foreground(gc, color_red);
    } else {
        gdk_gc_set_foreground(gc, color_blue);
    }
    gdk_draw_rectangle(pix, gc, TRUE, l, t, w, h);

    // Draw flashing lights on top and bottom
    if (flash_state) {
        gdk_gc_set_foreground(gc, color_blue);
    } else {
        gdk_gc_set_foreground(gc, color_red);
    }
    gdk_draw_rectangle(pix, gc, TRUE, l, t, w2, h2);
    gdk_draw_rectangle(pix, gc, TRUE, l + w2, t + h2, w2, h2);

    // Put red box around "current vehicle"
    if (selected) {
        draw_selection(pix, gc, l, t, w, h, scale);
    }
}


