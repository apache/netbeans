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

package clienteditor;

import org.jdesktop.beansbinding.Converter;

/**
 * Converter between marital status code and its human-readable representation.
 *
 * @author Jan Stola
 */
public class MaritalStatusConverter extends Converter<Integer, String> {

    public String convertForward(Integer arg) {
        String value = null;
        switch (arg) {
            case 0: value="Single"; break;
            case 1: value="Married"; break;
            case 2: value="Separated"; break;
            case 3: value="Divorced"; break;
        }
        return value;
    }

    public Integer convertReverse(String arg) {
        int value = 0;
        if ("Single".equals(arg)) {
            value = 0;
        } else if ("Married".equals(arg)) {
            value = 1;
        } else if ("Separated".equals(arg)) {
            value = 2;
        } else if ("Divorced".equals(arg)) {
            value = 3;
        }
        return value;
    }

}
