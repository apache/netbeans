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
package org.netbeans.modules.cpplite.project;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static String encode(List<List<String>> toSave) {
        return toSave.stream().map(c -> quote(c.stream().map(p -> quote(p)).collect(Collectors.joining(" ")))).collect(Collectors.joining(" "));
    }

    private static String quote(String s) {
        return s.replace("_", "_u_").replace(" ", "_s_");
    }

    public static List<List<String>> decode(String toDecode) {
        List<List<String>> result = new ArrayList<>();
        for (String commandDesc : toDecode.split(" ")) {
            List<String> command = new ArrayList<>();
            for (String paramDesc : unquote(commandDesc).split(" ")) {
                command.add(unquote(paramDesc));
            }
            result.add(command);
        }
        return result;
    }
    
    private static String unquote(String s) {
        return s.replace("_s_", " ").replace("_u_", "_");
    }
}
