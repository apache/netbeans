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
package beans;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Bean {

    public List<String> any() {
        return Arrays.asList(
                "Jeden",
                "Dva");
    }

    public String getProperty() {
        return "property";
    }

    public String[] getMyArray() {
        return new String[0];
    }

    public Iterable<String> getMyIterable() {
        return Collections.<String>emptyList();
    }

    public List<String> getMyList() {
        return Collections.<String>emptyList();
    }

    public String getMyString() {
        return "string";
    }

    public String getMyStringWithParam(String string) {
        return string;
    }

    public Map<String, String> getMyMap() {
        return Collections.<String, String>emptyMap();
    }

    public Cypris getMyCypris() {
        return new Cypris();
    }

    public void updateGameList(ActionEvent event) {
    }

}
