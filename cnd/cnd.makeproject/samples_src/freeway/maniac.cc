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
// Implementation of maniac driver class for "Freeway".
//

#include <math.h>
#include "vehicle_list.h"
#include "maniac.h"


const double DELTA_T    = 0.0000555; // 1/5 sec expressed in hours
const double OPT_DT     = 0.0001;    // optimal buffer (in hrs) in front
const double CAR_LEN    = 0.0025;    // car length (in miles) (roughly 16 ft)
const double BRAKE_DV   = 6.0;       // 30 mph / sec for 1/5 sec
const int    CAR_LENGTH = 5;	  
const int    CAR_WIDTH  = 3;	  


Maniac::Maniac(int i, int l, double p, double v) 
{
    classID           = CLASS_MANIAC;
    name_int          = i;
    lane_num          = l;
    position          = p;
    velocity          = v;
    state             = VSTATE_MAINTAIN;
    max_speed         = 150;
    xlocation         = 0;
    ylocation         = 0;
    change_state      = 0;
    restrict_change   = 0;
    absent_mindedness = 0;
}


void
Maniac::check_lane_change(Vehicle *in_front, void *neighbors) {
    //
    // Scan across list of neighbors.  If anyone is too close, just forget it.
    // If someone is somewhat close behind, and going faster,  just forget it.
    // If someone is somewhat close ahead,  and going slower,  just forget it.
    // Otherwise, go for a lane change.
    //
    // Note: The argument is passed as a void pointer to appease header files.  
    //       It is really a list pointer.
    //
#define BIG_NUM 999

    Vehicle *n_in_front = NULL;
    Vehicle *n_in_back  = NULL;
    double   d_in_front = BIG_NUM;
    double   d_in_back  = BIG_NUM;

    // Find closest neighbor in front and back
    for (List *l = ((List *) neighbors)->first(); l->hasValue(); l = l->next()) { 
	Vehicle *neighbor = l->value();
	double   dist     = neighbor->pos() - position;

	if (dist < 0) {	// neighbor is in back
	    dist = this->rear_pos() - neighbor->pos();
	    if (d_in_back > dist) {
		d_in_back = dist;
		n_in_back = neighbor;
	    }
	} else {			// neighbor is in front
	    dist = neighbor->rear_pos() - position;
	    if (d_in_front > dist) {
		d_in_front = dist;
		n_in_front = neighbor;
	    }
	}
    }

    // If there is nobody ahead of me in this lane, no reason to change.
    if (!in_front) {
        return;
    }

    // If the car in front of me is changing, forget it.
    if (in_front->vstate() == VSTATE_CHANGE_LANE) {
        return;
    }

    // If the neighbor in front of me is closer than car in front, bail.
    if (n_in_front && n_in_front->pos() < in_front->pos()) {
        return;
    }

    //
    // Find optimal following distance for me to follow car in front and car in 
    // back to follow me.
    //
    double opt_in_front, opt_in_back;
    if (n_in_front) {
        opt_in_front = this->optimal_dist(n_in_front);
    }
    if (n_in_back)  {
        opt_in_back  = n_in_back->optimal_dist(this);
    }

    // If there is not enough room in front, bail.
    if (n_in_front && d_in_front < opt_in_front / 3) {
        return;
    }

    // If there is not enough room in back, bail.
    if (n_in_back && d_in_back < opt_in_back / 3) {
        return;
    }

    // If there is not a lot of room in front, and guy in front is slower, bail.
    if (n_in_front && d_in_front < opt_in_front && n_in_front->vel() < vel()) {
        return;
    }

    // If there is not a lot of room in back, and guy in back is faster, bail.
    if (n_in_back && (d_in_back < opt_in_back / 2) && n_in_back->vel() > vel()) {
        return;
    }

    state           = VSTATE_CHANGE_LANE;
    change_state    = 0;		// just beginning the change
    restrict_change = 0;		// don't restrict lane changes
}


double
Maniac::optimal_dist(Vehicle *in_front)
{
    // Calculate optimal following distance based on my velocity and the 
    // difference in velocity from the car in front.
    double dv = in_front->vel() - velocity;

    return(OPT_DT * velocity + (0.5 * dv * dv * DELTA_T / BRAKE_DV));
}


int
Maniac::limit_speed(int)
{
    return(max_speed);
}


