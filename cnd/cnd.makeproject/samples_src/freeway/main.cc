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
// main.cc - Create and initialize GUI objects and simulation.
// 		start even loop
//

#include "FreewayWindow.h"
#include "traffic.h"

FreewayWindow window;

int main(int argc, char **argv)
{

    gtk_init(&argc, &argv);

    // Initialize user interface components.
    window.objects_initialize(argc, argv);

    // Initialize traffic simulation);
    traffic_init(argc, argv);

    // Turn control over to the event loop
    gtk_widget_show(GTK_WIDGET(window.getShell()));
    gtk_main();

    return 0;
}

