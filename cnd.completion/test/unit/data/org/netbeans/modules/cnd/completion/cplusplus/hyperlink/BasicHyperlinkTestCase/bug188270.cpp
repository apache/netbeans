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

typedef struct {
    int a;
    char b;
} bug188270_inner;

typedef struct {
    bug188270_inner i;
} bug188270_outer;

int bug188270_main(void) {
    bug188270_outer x = {
      .i = {
          .a=5,  /* Unable to resolve identifier a */
          .b='c' /* Unable to resolve identifier b */
      }
    };

    return 0;
}