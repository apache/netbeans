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
package org.netbeans.api.io;

import org.netbeans.spi.io.support.OutputColorType;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.io.OutputColorAccessor;

/**
 * A color specified for part of text in output pane. It can be a predefined
 * color for some type of text (success, debug, warning, failure), or arbitrary
 * RGB color.
 * <p>
 * Although using wide range of custom RGB colors may be tempting, predefined
 * colors are recommended, as they can be configured to respect GUI theme in
 * use.
 * </p>
 *
 * @author jhavlin
 */
public abstract class OutputColor {


    private final OutputColorType type;
    private static final OutputColor CLR_WARNING = new TypeColor(OutputColorType.WARNING);
    private static final OutputColor CLR_FAILURE = new TypeColor(OutputColorType.FAILURE);
    private static final OutputColor CLR_DEBUG = new TypeColor(OutputColorType.DEBUG);
    private static final OutputColor CLR_SUCCESS = new TypeColor(OutputColorType.SUCCESS);

    static {
        OutputColorAccessor.setDefault(new OutputColorAccessorImpl());
    }

    private OutputColor(OutputColorType type) {
        this.type = type;
    }

    OutputColorType getType() {
        return type;
    }

    /**
     * Warning text color.
     *
     * @return Predefined color for text of type "warning".
     */
    @NonNull
    public static OutputColor warning() {
        return CLR_WARNING;
    }

    /**
     * Failure text color.
     *
     * @return Predefined color for text of type "failure".
     */
    @NonNull
    public static OutputColor failure() {
        return CLR_FAILURE;
    }

    /**
     * Debug text color.
     *
     * @return Predefined color for text of type "debug".
     */
    @NonNull
    public static OutputColor debug() {
        return CLR_DEBUG;
    }

    /**
     * Success text color.
     *
     * @return Predefined color for text of type "success".
     */
    @NonNull
    public static OutputColor success() {
        return CLR_SUCCESS;
    }

    /**
     * Arbitrary constant RGB color.
     *
     * <p>
     * Please note that it is recommended to use colors for predefined text
     * types, which can respect color theme used by the GUI.
     * </p>
     *
     * @param r The red component, in the range (0 - 255).
     * @param g The green component, in the range (0 - 255).
     * @param b The blue component, in the range (0 - 255).
     *
     * @return Color specified for a constant RGB value.
     * @throws IllegalArgumentException If some of color components is out of
     * range.
     */
    @NonNull
    public static OutputColor rgb(int r, int g, int b) {
        checkColorComponentRange("r", r);
        checkColorComponentRange("g", g);
        checkColorComponentRange("b", b);
        int value = ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | ((b & 0xFF));
        return rgb(value);
    }

    /**
     * Arbitrary constant RGB color. Creates an opaque sRGB color with the
     * specified combined RGB value consisting of the red component in bits
     * 16-23, the green component in bits 8-15, and the blue component in bits
     * 0-7.
     *
     * <p>
     * Please note that it is recommended to use colors for predefined text
     * types, which can respect color theme used by the GUI.
     * </p>
     *
     * @param rgbValue The combined RGB components.
     *
     * @return Color specified for a constant RGB value.
     */
    @NonNull
    public static OutputColor rgb(int rgbValue) {
        return new RgbColor(rgbValue);
    }

    private static void checkColorComponentRange(String name,
            int colorComponent) {

        if (colorComponent < 0 || colorComponent > 255) {
            throw new IllegalArgumentException("Color component " + name//NOI18N
                    + " is out of range (0 - 255): " + colorComponent); //NOI18N
        }
    }

    private static class TypeColor extends OutputColor {

        public TypeColor(OutputColorType type) {
            super(type);
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static class RgbColor extends OutputColor {

        private final int value;

        public RgbColor(int value) {
            super(OutputColorType.RGB);
            this.value = value;
        }

        public int getRGB() {
            return value;
        }
    }
}
