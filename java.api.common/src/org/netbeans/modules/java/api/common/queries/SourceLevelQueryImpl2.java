/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 * @author Tomas Zezula
 */
class SourceLevelQueryImpl2 implements SourceLevelQueryImplementation2 {
    
    private static final String PLATFORM_ACTIVE = "platform.active";    //NOI18N
    private static final String JAVAC_SOURCE = "javac.source";  //NOI18N
    private static final String JAVAC_TARGET = "javac.target";  //NOI18N
    private static final String DEFAULT_SOURCE_LEVEL = "default.javac.source";  //NOI18N
    private static final String DEFAULT_TARGET_LEVEL = "default.javac.target";  //NOI18N
    private static final String JAVAC_PROFILE = "javac.profile";    //NOI18N
    private static final Pattern SUPPORTS_PROFILES = Pattern.compile("(1\\.)?8");    //NOI18N

    private final PropertyEvaluator eval;
    private final String platformType;
    private final Result result;

    SourceLevelQueryImpl2(
        @NonNull final PropertyEvaluator eval,
        @NonNull final String platformType) {
        assert eval != null;
        assert platformType != null;
        this.eval = eval;
        this.platformType = platformType;
        this.result = new R();
    }

    @Override
    public Result getSourceLevel(FileObject javaFile) {
        return this.result;
    }

    @CheckForNull
    static String findSourceLevel (
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        return findValue(eval, platformType, JAVAC_SOURCE, DEFAULT_SOURCE_LEVEL);
    }

    @CheckForNull
    private static String findTargetLevel(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        return findValue(eval, platformType, JAVAC_TARGET, DEFAULT_TARGET_LEVEL);
    }

    @CheckForNull
    private static String findValue(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType,
            @NonNull final String prop,
            @NonNull final String fallBack) {
        final String activePlatform = eval.getProperty(PLATFORM_ACTIVE);
        if (CommonProjectUtils.getActivePlatform(activePlatform, platformType) != null) {
            String sl = eval.getProperty(prop);
            if (sl != null && !sl.isEmpty()) {
                return sl;
            }
            return null;
        }
        final EditableProperties props = PropertyUtils.getGlobalProperties();
        String sl = props.get(fallBack);
        if (sl != null && !sl.isEmpty()) {
            return sl;
        }
        return null;
    }

    private static SourceLevelQuery.Profile findProfile(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        SourceLevelQuery.Profile res;
        if (supportsProfiles(findTargetLevel(eval, platformType))) {
            final String profile = eval.getProperty(JAVAC_PROFILE);
            res = SourceLevelQuery.Profile.forName(profile);
            if (res != null) {
                return res;
            }
        }
        res = SourceLevelQuery.Profile.DEFAULT;
        return res;
    }

    private static boolean supportsProfiles(
            @NullAllowed final String sl) {
        return sl != null &&
            SUPPORTS_PROFILES.matcher(sl).matches();
    }

    private class R implements Result2, PropertyChangeListener {
        
        private final ChangeSupport cs = new ChangeSupport(this);

        @SuppressWarnings("LeakingThisInConstructor")
        private R() {
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        }

        @Override
        public String getSourceLevel() {
            return findSourceLevel(eval, platformType);
        }

        @Override
        public SourceLevelQuery.Profile getProfile() {
            return findProfile(eval, platformType);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            this.cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            this.cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String name = evt.getPropertyName();
            if (name == null ||
                JAVAC_SOURCE.equals(name) ||
                JAVAC_PROFILE.equals(name) ||
                PLATFORM_ACTIVE.equals(name)) {
                this.cs.fireChange();
            }
        }

        @Override
        public String toString() {
            final String sl = getSourceLevel();
            return sl == null ? "" : sl.toString(); //NOI18M
        }

    }

}
