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

public class WeatherCity {

    private final int id;
    private final String name;
    private final String country;
    private final int population;
    private final double longitude;
    private final double lat;
    private double temperature;

    public WeatherCity(int id, String name, String country, int population, double lat, double longitude, double temperature) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.longitude = longitude;
        this.lat = lat;
        this.temperature = temperature;
        this.population = population;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public int getPopulation() { return population; }
    public double getLatitude() { return lat; }
    public double getLongitude() { return longitude; }
    public double getTemperature() { return temperature; }

    public void updateTemperature(double newValue) {
        temperature = newValue;
    }
}
