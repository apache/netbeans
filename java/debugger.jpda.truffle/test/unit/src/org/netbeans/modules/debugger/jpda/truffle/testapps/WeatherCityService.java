/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.debugger.jpda.truffle.testapps;

import java.util.Arrays;

public class WeatherCityService {

    // A test sample
    private static WeatherCity[] cities = new WeatherCity[] {
        new WeatherCity(0, "San Roque", "N. Mariana Islands", 1097, 15.25, 145.77, 13.2346746509429),
        new WeatherCity(1, "La Sabana", "Honduras", 1572, 15.37, -87.93, 29.5364631644916),
        new WeatherCity(2, "Videbaek", "Denmark", 4076, 56.08, 8.63, 24.2545498488471),
        new WeatherCity(3, "Nasirabad", "Pakistan", 28506, 27.38, 67.91, 25.4554680001456),
        new WeatherCity(4, "Okazaki", "Japan", 355573, 34.96, 137.16, 7.89123377134092),
        new WeatherCity(5, "Huddersfield", "UK", 149607, 53.66, -1.8, 26.9585336048622),
        new WeatherCity(6, "Cocentaina", "Spain", 11223, 38.75, -0.44, 16.3208602035884),
        new WeatherCity(7, "Ankathia", "Greece", 1300, 40.56, 22.47, 9.76689549605362),
        new WeatherCity(8, "Mediesu Aurit", "Romania", 7062, 47.78, 23.15, 10.8357614693232),
        new WeatherCity(9, "Juan Lopez", "Dominican Republic", 1547, 19.43, -70.52, 14.5722625446506),
        new WeatherCity(10, "Birnin Kebbi", "Nigeria", 111883, 12.46, 4.19, 23.3168364593294),
        new WeatherCity(11, "Chistopol", "Russia", 62020, 55.36, 50.64, 3.51541253109463),
        new WeatherCity(12, "Periyialion", "Greece", 2120, 37.95, 22.84, 16.3009215018246),
        new WeatherCity(13, "Togo", "Japan", 42643, 35.1, 137.04, 8.89008317119442),
        new WeatherCity(14, "Shiyan", "China", 413581, 32.57, 110.78, 20.1505158017389),
        new WeatherCity(15, "Iira", "Estonia", 138, 58.99, 24.72, 12.9968547783792),
        new WeatherCity(16, "Ilha Soltera", "Brazil", 25305, -20.38, -51.34, 14.7320180023089),
        new WeatherCity(17, "Ikorodu", "Nigeria", 321809, 6.61, 3.51, 22.5338149268646),
        new WeatherCity(18, "Kocani", "Macedonia", 34448, 41.93, 22.4, 2.02855428215116),
        new WeatherCity(19, "Siatista", "Greece", 5603, 40.26, 21.54, 22.2741849503946),
        new WeatherCity(20, "Pella", "Greece", 2482, 40.76, 22.52, 13.1903853449039),
    };

    public int getTotalCount() { return cities.length; }
    public WeatherCity[] getAll() { return cities; }
    public WeatherCity[] getAllPaged(int skip, int pageSize) {
        return Arrays.stream(cities).skip(skip).limit(pageSize).toArray(n -> new WeatherCity[n]);
    }

    public WeatherCity findByName(String name) {
        return Arrays.stream(cities).filter(x -> x.getName().equals(name)).findFirst().orElse(null);
    }

    public void updateTemperature(int id, double temperature) {
        cities[id].updateTemperature(temperature);
    }
    public Object getNull() {
        return null;
    }

}
