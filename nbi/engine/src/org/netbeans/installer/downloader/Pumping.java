/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.downloader;

import java.io.File;
import java.net.URL;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */

public interface Pumping {
    /**
     * It's runtime property of pumping. It means that it's not persistence 
     * property. So if downloader client maintain it's state persistance - it 
     * mustn't base on pumpings ids.
     */
    String getId();

    /**
     * @return declared pumping url.
     */
    URL declaredURL();

    /**
     * @return real pumping url. It is url which was obtain at runtime. It's may be 
     * the same as declared url if no redirect may occur.
     */
    URL realURL();

    /**
     * @return file corresponding to this pumping.
     */
    File outputFile();

    File folder();

    long length();

    /**
     * @return mode in which downloader process it. So if Single thread mode - it's
     * means that only one thread process pumping(so one section invoked). If multi
     * thread mode - it's means that downloader allowed to process pumping in more
     * then one thread concurrently. But it's not means that downloader do it.
     * The issue process or not in multy thread deal with some external issues:
     * for example domain police and server side speed reducing for client who try 
     * to obtain more then one connection at time. Base implementation in any case
     * download in one thread.
     */
    DownloadMode mode();

    State state();

    /**
     * one section  - one thread. Section - data structure for representation and 
     * manage downloading unit
     */
    Section[] getSections();

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static interface Section {
        /**
         * range of bytes this section responsible for.
         */
        Pair<Long, Long> getRange();

        /**
         * absolute offset. Means if range: 12345 - 23456. initially offset equals 
         * 12345 when section downloaded it's equals 23456.
         */
        long offset();
    }

    public enum State {
        NOT_PROCESSED, 
        CONNECTING, 
        PUMPING, 
        WAITING, 
        INTERRUPTED, 
        FAILED, 
        FINISHED, 
        DELETED
    }
}
