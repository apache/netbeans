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
package org.netbeans.modules.java.mx.project;

import java.util.logging.Level;
import org.openide.util.Exceptions;

public final class Compliance {

    final int max;
    final int min;
    
    private Compliance(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    public static Compliance exact(int version) {
        return new Compliance(version, version);
    }
    
    public static Compliance plus(int version) {
        return new Compliance(version, Integer.MAX_VALUE);
    }
    
    public static Compliance range(int min, int max) {
        return new Compliance(min, max);
    }
    
    public static Compliance parse(String spec) {
        if (spec == null) {
            return plus(8);
        }
        if (spec.startsWith("1.")) {
            spec = spec.substring(2);
        }
        boolean plus;
        if (spec.endsWith("+")) {
            plus = true;
            spec = spec.substring(0, spec.length() - 1);
        } else {
            plus = false;
            
            int dots = spec.indexOf("..");
            if (dots != -1) {
                int low = Integer.parseInt(spec.substring(0, dots));
                int high = Integer.parseInt(spec.substring(dots + 2));
                return range(low, high);
            }
        }
        int comma = spec.indexOf(",");
        if (comma != -1) {
            spec = spec.substring(0, comma);
        }
        if (spec.endsWith("-loom")) {
            spec = spec.substring(0, spec.length() - 5);
        }
        int version;
        try {
            version = Integer.parseInt(spec);
        } catch (NumberFormatException numberFormatException) {
            Exceptions.attachSeverity(numberFormatException, Level.INFO);
            Exceptions.printStackTrace(numberFormatException);
            return plus(8);
        }
        return plus ? Compliance.plus(version) : Compliance.exact(version);
    }

    public boolean includes(int version) {
        return min <= version && version <= max;
    }

    @Override
    public String toString() {
        if (max == Integer.MAX_VALUE) {
            return min + "+";
        } else {
            if (min == max) {
                return "" + min;
            } else {
                return min + ".." + max;
            }
        }
    }

    boolean matches(Compliance check) {
        if (min > check.min) {
            return false;
        }
        if (max < check.max) {
            return false;
        }
        return true;
    }

    String getSourceLevel() {
        if (min == 8) {
            return "1.8";
        }
        return "" + min;
    }

    
    public interface Provider {
        public Compliance getCompliance();
    }
}
