/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
 * <li><b>at class level</b> on class implementing {@link ActiveEditorDrop}.<br><br>
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
 * @author Eric Barboni <skygo@netbeans.org>
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
