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


