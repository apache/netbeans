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
package org.netbeans.modules.cloud.oracle.developer;

import com.oracle.bmc.Region;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Petrovic
 */
public class MetricsItem extends OCIItem implements URLProvider {
    
    private static final String DEFAULT_AGGREGATION_FUNCTION = "mean()";
    private static final String DEFAULT_INTERVAL = "1m";
    private static final String BASE_URL = "https://cloud.oracle.com/monitoring/explore";
    private static final String DEFAULT_ADVANCED_MODE = "false";
    private static final String DEFAULT_AGGREGATE_METRICS = "false";
    private static final String UTF_8 = "UTF-8";
    
    private String namespace;
    private String region;
    
    public MetricsItem() {
        super();
    }

    public MetricsItem(String compartmentId, String name, String namespace, Region region, String tenancyId) {
        super(OCID.of("MetricsNamespace/Metrics"), compartmentId, name, tenancyId, region.getRegionCode());
        this.namespace = namespace;
        this.region = region.getRegionId();
    }
    
    public String getNamespace() {
        return namespace;
    }

    @Override
    public URL getURL() {        
        try {
            String url = addQueryTo(BASE_URL);
            return URI.create(url).toURL();
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public String addQueryTo(String baseUrl) throws UnsupportedEncodingException {
        Map<String, String> params = getQueryParams();
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(URLEncoder.encode(entry.getKey(), UTF_8))
              .append("=")
              .append(URLEncoder.encode(entry.getValue(), UTF_8))
              .append("&");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private Map<String, String> getQueryParams() {
        return new HashMap<>() {{
            put("panelConfigs[0][advanced]", DEFAULT_ADVANCED_MODE);
            put("panelConfigs[0][content][aggregate]", DEFAULT_AGGREGATE_METRICS);
            put("panelConfigs[0][content][compartmentId]", getCompartmentId());
            put("panelConfigs[0][content][metricName]", getName());
            put("panelConfigs[0][content][namespace]", getNamespace());
            put("panelConfigs[0][content][interval]", DEFAULT_INTERVAL);
            put("panelConfigs[0][content][statistic]", DEFAULT_AGGREGATION_FUNCTION);
            put("region", region);
        }};
    }
}
