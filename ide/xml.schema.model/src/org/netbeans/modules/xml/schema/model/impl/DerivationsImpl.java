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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation;

/**
 *
 * @author nn136682
 */
public class DerivationsImpl implements Derivation {

    public static class DerivationSet<E> extends HashSet<E> {
        public static final long serialVersionUID = 1L;
        public String toString() {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (E e : this) {
                if (! first) {
                    sb.append(Util.SEP);
                } else {
                    first = false;
                }
                sb.append(e.toString());
            }
            return sb.toString();
        }
    }
}
