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

typedef float iz165976_SNGL_VECT[3];
typedef struct iz165976_photon_struct iz165976_PHOTON;
struct iz165976_photon_struct {
  iz165976_SNGL_VECT Loc;          /* location */
};
typedef iz165976_PHOTON *iz165976_PHOTON_BLOCK;
struct iz165976_photon_map_struct {
  /* these 3 are render-thread safe - NOT pre-process thread safe */
  iz165976_PHOTON_BLOCK *head;   /* the photon map - array of blocks of photons */
};
int iz165976_main() {
    typedef struct iz165976_photon_map_struct PHOTON_MAP;
    PHOTON_MAP *map;
    int j = 0;
    (map->head [( j )>> 14 ][( j ) & ( (16384) -1) ]).Loc;
    return 0;
}