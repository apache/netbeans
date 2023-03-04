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
package org.netbeans.modules.maven.htmlui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.openide.util.NbBundle;

@Model(className="Device", builder="assign", properties = {
    @Property(name = "name", type = String.class),
    @Property(name = "id", type = String.class),
    @Property(name = "type", type = DeviceType.class),
    @Property(name = "info", type = String.class)
})
final class MacUtilities {
    private static final String ID_UNKNOWN = "0000-0000-0000";

    private MacUtilities() {
    }
    
    @NbBundle.Messages({
        "# {0} - the error",
        "ERR_CannotExecute=Cannot execute `instruments -s devices`: {0}",
        "# {0} - the error",
        "ERR_CannotParse=Unrecognized device: {0}",
        "MSG_Loading=Loading devices..."
    })
    static void listDevices(List<Device> collectTo) {
        collectTo.clear();
        Device loading = new Device().assignId(ID_UNKNOWN).
                assignName(Bundle.MSG_Loading()).
                assignInfo(Bundle.MSG_Loading());
        collectTo.add(loading);

        ProcessBuilder pb = ProcessBuilder.getLocal();
        pb.setExecutable("instruments");
        pb.setArguments(Arrays.asList("-s", "devices"));
        pb.setRedirectErrorStream(true);
        Process p;
        try {
            p = pb.call();
        } catch (IOException ex) {
            collectTo.clear();
            collectTo.add(
                new Device().assignId(ID_UNKNOWN).
                assignName("unknown").
                assignInfo(Bundle.ERR_CannotExecute(ex.getLocalizedMessage()))
            );
            return;
        }

        
        final InputStream is = p.getInputStream();
        parseDevices(collectTo, is);
        p.destroy();
        collectTo.remove(loading);
    }

    static List<Device> parseDevices(List<Device> collectTo, final InputStream is) {
        Pattern pattern = Pattern.compile("\\[([0-9A-Fa-f\\-]+)\\]");

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        for (int at = collectTo.size();;) {
            String l;
            try {
                l = r.readLine();
            } catch (IOException ex) {
                collectTo.add(new Device().assignId(ID_UNKNOWN).
                        assignName("unknown").
                        assignInfo(Bundle.ERR_CannotExecute(ex.getLocalizedMessage()))
                );
                break;
            }
            if (l == null) {
                break;
            }
            if (l.indexOf('[') == -1 || l.indexOf(']') == -1) {
                continue;
            }
            Matcher m = pattern.matcher(l);
            if (m.find()) {
                String id = m.group(1);
                String nameVersion = l.substring(0, m.start()).trim();
                String rest = l.substring(m.end());
                Device d = new Device().
                        assignName(nameVersion).
                        assignId(id);
                if (rest.contains("imulator")) {
                    d.assignType(DeviceType.SIMULATOR);
                } else {
                    d.assignType(DeviceType.DEVICE);
                }
                collectTo.add(at++, d);
            } else {
                Device notRecognized = new Device().
                        assignId(ID_UNKNOWN).
                        assignName("broken").
                        assignInfo(Bundle.ERR_CannotParse(l));
                collectTo.add(collectTo.size(), notRecognized);
            }
        }
        return collectTo;
    }
}
