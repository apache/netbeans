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
// Freeway traffic-simulation control functions
//

class Vehicle;
class FreewayWindow;

extern void traffic_init(int, char **);

extern void traffic_start();
extern void traffic_stop();
extern void traffic_reset();
extern void traffic_clear();
extern void traffic_popup_done();
extern void traffic_popup_display(int);
extern void traffic_popup(Vehicle *);
extern void traffic_gap(gdouble);
extern void traffic_time(gdouble);
extern void traffic_state(int);
extern void traffic_class(int);
extern void traffic_randomize(int);
extern void traffic_position(char *);
extern void traffic_velocity(int);
extern void traffic_max_speed(int);
extern void traffic_step();
extern void traffic_remove();
extern void traffic_speed(int zone, int limit);
extern void traffic_force_popup_done();
extern void traffic_set_popup_values(Vehicle *);
extern void traffic_popup_scroll(Vehicle *);
extern void traffic_cancel();
extern void traffic_do_load(char *);
extern void traffic_do_save(char *);
extern void traffic_file_close();

extern char *traffic_current_file;

