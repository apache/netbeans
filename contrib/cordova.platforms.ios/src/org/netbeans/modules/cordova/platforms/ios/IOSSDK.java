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
package org.netbeans.modules.cordova.platforms.ios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cordova.platforms.spi.SDK;

/**
 *
 * @author Jan Becicka
 */
public class IOSSDK implements SDK {

    public static final String IOS_BUILD_SDK_PROP = "ios.build.sdk"; // NOI18N

    private String name;
    private final String identifier;

    public static Collection<SDK> parse(String output) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));
        
        Pattern pattern = Pattern.compile("(.*)-sdk(.*)"); //NOI18N
        
        ArrayList<SDK> result = new ArrayList<SDK>();
        //ignore first line
        
        String line = null;
        do {
            line = r.readLine();
            if (line==null) {
                //no ios sdks
                return result;
            }
        } while (!line.startsWith("iOS Simulator SDKs")); //NOI18N
        
        line = r.readLine();
      
        while (line !=null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                IOSSDK sdk = new IOSSDK(m.group(1).trim(), m.group(2).trim());
                result.add(sdk);
            } else {
                return result;
            }
            line = r.readLine();
        }
        return result;
    }

    IOSSDK(String name, String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "SDK{" + "name=" + name + ", identifier=" + identifier + '}'; //NOI18N
    }
}
