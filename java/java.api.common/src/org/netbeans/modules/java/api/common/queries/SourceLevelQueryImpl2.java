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
