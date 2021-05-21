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
package org.netbeans.modules.cloud.amazon;

/**
 *
 * @author peedeeboy
 */
public enum AmazonRegion {

    US_EAST_2("US East (Ohio) Region", "elasticbeanstalk.us-east-2.amazonaws.com", "us-east-2"),
    US_EAST_1("US East (Northern Virginia) Region", "elasticbeanstalk.us-east-1.amazonaws.com", "us-east-1"),
    US_WEST_1("US West (Northern California) Region", "elasticbeanstalk.us-west-1.amazonaws.com", "us-west-1"),
    US_WEST_2("US West (Oregon) Region", "elasticbeanstalk.us-west-2.amazonaws.com", "	us-west-2"),
    EU_WEST_1("EU (Ireland) Region", "elasticbeanstalk.eu-west-1.amazonaws.com", "eu-west-1"),
    EU_CENTRAL_1("EU (Frankfurt) Region", "elasticbeanstalk.eu-central-1.amazonaws.com", "eu-central-1"),
    EU_WEST_2("EU (London) Region", "elasticbeanstalk.eu-west-2.amazonaws.com", "eu-west-2"),
    EU_WEST_3("EU (Paris) Region", "elasticbeanstalk.eu-west-3.amazonaws.com", "eu-west-3"),
    EU_NORTH_1("EU (Stockholm) Region", "elasticbeanstalk.eu-north-1.amazonaws.com", "eu-north-1"),
    AP_EAST_1("Asia Pacific (Hong Kong) Region", "elasticbeanstalk.ap-east-1.amazonaws.com", "ap-east-1"),
    AP_SOUTH_1("Asia Pacific (Mumbai) Region", "elasticbeanstalk.ap-south-1.amazonaws.com", "ap-south-1"),
    AP_NORTHEAST_3("Asia Pacific (Osaka) Region", "elasticbeanstalk.ap-northeast-3.amazonaws.com", "ap-northeast-3"),
    AP_NORTHEAST_1("Asia Pacific (Tokyo) Region", "elasticbeanstalk.ap-northeast-1.amazonaws.com", "ap-northeast-1"),
    AP_NORTHEAST_2("Asia Pacific (Seoul) Region", "elasticbeanstalk.ap-northeast-2.amazonaws.com", "ap-northeast-2"),
    AP_SOUTHEAST_1("Asia Pacific (Singapore) Region", "elasticbeanstalk.ap-southeast-1.amazonaws.com", "ap-southeast-1"),
    AP_SOUTHEAST_2("Asia Pacific (Sydney) Region", "elasticbeanstalk.ap-southeast-2.amazonaws.com", "ap-southeast-2"),
    SA_EAST_1("South America (Sao Paulo) Region", "elasticbeanstalk.sa-east-1.amazonaws.com", "sa-east-1"),
    CA_CENTRAL_1("Canada (Central) Region", "elasticbeanstalk.ca-central-1.amazonaws.com", "ca-central-1"),
    CN_NORTH_1("China (Beijing) Region", "elasticbeanstalk.cn-north-1.amazonaws.com.cn", "cn-north-1"),
    CN_NORTHWEST_1("China (Ningxia) Region", "elasticbeanstalk.cn-northwest-1.amazonaws.com.cn", "cn-northwest-1");
    
    private String name;
    private String url;
    private String code;

    private AmazonRegion(String name, String url, String code) {
            this.name = name;
            this.url = url;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
        
        public String getCode() {
            return code;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public static Object findRegion(String regionURL) {
        for (AmazonRegion r : values()) {
            if (r.getUrl().equals(regionURL)) {
                return r;
            }
        }
        return null;
    }
}
