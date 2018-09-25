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
package org.netbeans.modules.htmlui;

import net.java.html.json.Model;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import org.netbeans.html.geo.spi.GLProvider;
import org.netbeans.html.geo.spi.GLProvider.Query;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = GLProvider.class)
@Model(className = "FreeGeoIp", properties = {
    @Property(name = "id", type = String.class),
    @Property(name = "country_code", type = String.class),
    @Property(name = "country_name", type = String.class),
    @Property(name = "region_code", type = String.class),
    @Property(name = "region_name", type = String.class),
    @Property(name = "city", type = String.class),
    @Property(name = "zip_code", type = String.class),
    @Property(name = "time_zone", type = String.class),
    @Property(name = "latitude", type = double.class),
    @Property(name = "longitude", type = double.class),
    @Property(name = "metro_code", type = int.class),
})
public final class FreeGeoProvider extends GLProvider<FreeGeoIp,Query> {
    private static FreeGeoProvider INSTANCE;
    private Query current;

    public FreeGeoProvider() {
        INSTANCE = this;
    }

    @Override
    protected Query start(Query query) {
        current = query;
        new FreeGeoIp().findLocation(query);
        return query;
    }

    @Override
    protected void stop(Query watch) {
    }

    @Override
    protected double latitude(FreeGeoIp coords) {
        return coords.getLatitude();
    }

    @Override
    protected double longitude(FreeGeoIp coords) {
        return coords.getLongitude();
    }

    @Override
    protected double accuracy(FreeGeoIp coords) {
        return 0;
    }

    @Override
    protected Double altitude(FreeGeoIp coords) {
        return null;
    }

    @Override
    protected Double altitudeAccuracy(FreeGeoIp coords) {
        return null;
    }

    @Override
    protected Double heading(FreeGeoIp coords) {
        return null;
    }

    @Override
    protected Double speed(FreeGeoIp coords) {
        return null;
    }

    @OnReceive(url = "https://freegeoip.net/json/", onError = "noLocation")
    static void findLocation(FreeGeoIp model, FreeGeoIp data, Query q) {
        INSTANCE.callback(q, System.currentTimeMillis(), data, null);
    }

    static void noLocation(FreeGeoIp model, Exception ex) {
        INSTANCE.callback(INSTANCE.current, System.currentTimeMillis(), null, ex);
    }
}
