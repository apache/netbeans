/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.palette;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register a palette item.
 *
 * This annotation may be used at two level:
 * <ul>
 * <li><b>at package level</b> body <b>MUST</b> be set;<br><br> 
 * Usage example in a package-info.java file, inspired by Netbeans Code Snippet Module Tutorial.<br>
 * {@code @PaletteItemRegistration(paletteid = "HTMLPalette", category = "HTML", itemid = "BR",
    icon32 = "org/netbeans/modules/newsnippets1/BR32.png",
    icon16 = "org/netbeans/modules/newsnippets1/BR16.png",
    body = "<br>",
    name = "New Line",
    tooltip = "<br>")}</li>
 * <li><b>at class level</b> on class implementing {@link org.openide.text.ActiveEditorDrop}.<br><br>
 * {@code @PaletteItemRegistration(
    paletteid = "HTMLPalette",
    category = "HTML",
    itemid = "BR",
    icon32 = "org/netbeans/modules/newsnippets1/BR32.png",
    icon16 = "org/netbeans/modules/newsnippets1/BR16.png",
    body = "&lt;br&gt;",
    name = "New Line",
    tooltip = "&lt;br&gt;")} <br>
  * {@code public class MyPaletteItem implements org.openide.text.ActiveEditorDrop ...}
 * </li>
 * </ul>
 * 
 * @author Eric Barboni &lt;skygo@netbeans.org&gt;
 * @since 1.40
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface PaletteItemRegistration {

    /**
     * Palette root name. 
     * 
     * This is part of palette location in the netbeans layer:<br>
     * <b>paletteid</b>/category/itemid
     */
    String paletteid();

    /**
     * Category for palette item.
     * 
     * This is part of palette location in the netbeans layer:<br>
     * paletteid/<b>category</b>/itemid
     */
    String category();

    /**
     * Id for palette item.
     * 
     * This is part of palette location in the netbeans layer:<br>
     * paletteid/category/<b>itemid</b>
     */
     String itemid();

    /**
     * body of palette item.
     */
    String body() default "";

    /**
     * Path to a 16x16 image file for palette item.
     * <br>
     * Image must be available in classpath.
     */
    String icon16();

    /**
     * Path to a 32x32 image file for palette item.
     * <br>
     * Image must be available in the classpath
     */
    String icon32();

    /**
     * Display name for palette item.
     */
    String name();

    /**
     * Tooltip text for palette item.
     *
     */
    String tooltip();
}
