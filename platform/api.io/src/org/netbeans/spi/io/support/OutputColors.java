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
package org.netbeans.spi.io.support;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.io.OutputColor;
import org.netbeans.modules.io.OutputColorAccessor;

/**
 * Helper class for accessing information from {@link OutputColor} objects.
 *
 * @author jhavlin
 */
public final class OutputColors {

    private OutputColors() {
    }

    /**
     * Get type of a color.
     *
     * @param color The color to get type of.
     * @return Type of color.
     */
    @NonNull
    public static OutputColorType getType(@NonNull OutputColor color) {
        return OutputColorAccessor.getDefault().getType(color);
    }

    /**
     * Get RGB value for an {@link OutputColor} specified for a constant RGB
     * color (type {@link OutputColorType#RGB}).
     *
     * @param color The color to get RGB value for.
     *
     * @return RGB value of the color.
     * @throws IllegalArgumentException if the color is not of type
     * {@link OutputColorType#RGB}.
     */
    public static int getRGB(OutputColor color) {
        return OutputColorAccessor.getDefault().getRgb(color);
    }

}
