/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
