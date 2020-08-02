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

