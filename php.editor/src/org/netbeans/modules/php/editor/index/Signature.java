/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.index;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Marek Fukala
 */
public final class Signature {
    public static final char ITEM_DELIMITER = ';';
    public static final String ITEM_DELIMITER_ALTERNATIVE = "**NB_SEMI**"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(Signature.class.getName());
    private static final Pattern DECODE_PATTERN = Pattern.compile("\\*\\*NB_SEMI\\*\\*"); //NOI18N
    private static final Pattern ENCODE_PATTERN = Pattern.compile(";"); //NOI18N
    //shared array for better performance,
    //access is supposed from one thread so perf
    //shouldn't degrade due to synchronization
    private static final int[] SHARED;
    static {
        SHARED = new int[50]; //hopefully noone will use more items in the signature
        SHARED[0] = 0;
    }
    private final String signature;
    private final int[] positions;

    public static Signature get(String signature) {
        return new Signature(signature);
    }

    private Signature(String signature) {
        this.signature = signature;
        this.positions = parseSignature(signature);
    }

    @NonNull
    public String string(int index) {
        assert index >= 0 && index < positions.length;
        String subString = signature.substring(positions[index], index == positions.length - 1 ? signature.length() : positions[index + 1] - 1);
        return decodeItem(subString);
    }

    public int integer(int index) {
        String item = string(index);
        if (item != null && item.length() > 0) {
            try {
                return Integer.parseInt(item);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Can't parse item ''{0}'' as integer.", item);
                return -1;
            }
        } else {
            return -1;
        }
    }

    private static int[] parseSignature(String signature) {
        synchronized (SHARED) {
            int count = 0;
            for (int i = 0; i < signature.length(); i++) {
                if (signature.charAt(i) == ITEM_DELIMITER) {
                    SHARED[++count] = i + 1;
                }
            }
            count++; //include the first zero
            int[] a = new int[count];
            System.arraycopy(SHARED, 0, a, 0, count);
            return a;
        }
    }

    public static String encodeItem(final String item) {
        String result = null;
        if (item != null) {
            Matcher matcher = ENCODE_PATTERN.matcher(item);
            result = matcher.replaceAll(ITEM_DELIMITER_ALTERNATIVE);
        }
        return result;
    }

    private static String decodeItem(final String item) {
        String result = null;
        if (item != null) {
            Matcher matcher = DECODE_PATTERN.matcher(item);
            result = matcher.replaceAll(String.valueOf(ITEM_DELIMITER));
        }
        return result;
    }

}

