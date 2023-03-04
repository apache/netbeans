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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cordova.platforms.spi.SDK;

/**
 *
 * @author Jan Becicka
 */
public class Target implements SDK {

    private String name;
    private HashMap<String, String> props;
    private int id;
    private static final Logger LOG = Logger.getLogger(Target.class.getName());

    private Target() {
        this.props = new HashMap<>();
    }
    
    public static Collection<SDK> parse(String output) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));
        
        Pattern pattern = Pattern.compile("id: ([\\d]*) or \"([^\"]+)\" *"); //NOI18N
        
        ArrayList<SDK> result = new ArrayList<SDK>();
        //ignore first 2 lines
        r.readLine();
        r.readLine();
        
        Target current = new Target();
        String lastProp = null;
        String line = r.readLine();
        while (line != null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                current.id = Integer.parseInt(m.group(1));
                current.name = m.group(2);
            } else {
                if (line.contains("---------")) { //NOI18N
                    result.add(current);
                    current = new Target();
                } else {
                    //current.props.put(lastProp, current.props.get(lastProp) + line);
                }
            }
            line = r.readLine();
            if (line == null) {
                result.add(current);
            }
        }
        if (result.isEmpty()) {
            LOG.warning("no targets found");
            LOG.warning("output:" + output);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Target{" + "id=" + id + ", name=" + name + '}'; //NOI18N
    }

    @Override
    public String getIdentifier() {
        return Integer.toString(id);
    }
}
