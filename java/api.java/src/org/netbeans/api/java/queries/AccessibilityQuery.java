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
package org.netbeans.api.java.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation2;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Indicates whether a Java package should be considered publicly accessible.
 * <div class="nonnormative">
 * <p>Suggested uses:</p>
 * <ol>
 * <li>Visually marking public and private packages as such.</li>
 * <li>Editor code completion could refuse to include private packages from
 * other compilation units.</li>
 * <li>Javadoc editing tools (the suggestions provider and/or AutoComment) could
 * treat missing or incomplete Javadoc in private packages as a minor error, or
 * not an error.</li>
 * </ol>
 * <p>If the Java Project module is enabled, you may register an implementation
 * to the lookup for a project rather than the default lookup.</p>
 * </div>
 * @see AccessibilityQueryImplementation
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public class AccessibilityQuery {

    private static final Lookup.Result<? extends AccessibilityQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(AccessibilityQueryImplementation.class);
    private static final Lookup.Result<? extends AccessibilityQueryImplementation2> implementations2 =
        Lookup.getDefault().lookupResult(AccessibilityQueryImplementation2.class);

    private AccessibilityQuery() {}

    /**
     * Check whether a given Java source package should be considered publicly
     * accessible for use by other compilation units.
     * If not, then even public classes in the package should be treated as
     * effectively private by the IDE (though the Java compiler will not forbid
     * you to access them).
     * @param pkg a Java source package (must have a corresponding
     *        {@link org.netbeans.api.java.classpath.ClassPath#SOURCE} root)
     * @return true if the package is definitely intended for public access from
     *         other compilation units, false if it is definitely not, or null if
     *         this information is not known
     */
    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    @CheckForNull
    public static Boolean isPubliclyAccessible(@NonNull FileObject pkg) {
        if (!pkg.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + pkg); // NOI18N
        }
        for (AccessibilityQueryImplementation2 aqi : implementations2.allInstances()) {
            final AccessibilityQueryImplementation2.Result res = aqi.isPubliclyAccessible(pkg);
            if (res != null) {
                return res.getAccessibility().toBoolean();
            }
        }
        for (AccessibilityQueryImplementation aqi : implementations.allInstances()) {
            Boolean b = aqi.isPubliclyAccessible(pkg);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    /**
     * Check whether a given Java source package should be considered publicly
     * accessible for use by other compilation units.
     * If not, then even public classes in the package should be treated as
     * effectively private by the IDE (though the Java compiler will not forbid
     * you to access them).
     * @param pkg a Java source package (must have a corresponding
     *        {@link org.netbeans.api.java.classpath.ClassPath#SOURCE} root)
     * @return the {@link Result} object encapsulating the accessibility of the Java package.
     * Results created for accessibility provided by the {@link AccessibilityQueryImplementation} do not support listening.
     * @since 1.64
     */
    @NonNull
    public static Result isPubliclyAccessible2(@NonNull final FileObject pkg) {
        Parameters.notNull("pkg", pkg); //NOI18N
        if (!pkg.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + pkg); // NOI18N
        }
        for (AccessibilityQueryImplementation2 aqi : implementations2.allInstances()) {
            final AccessibilityQueryImplementation2.Result res = aqi.isPubliclyAccessible(pkg);
            if (res != null) {
                return new Result(res);
            }
        }
        return new Result(new Adapter(pkg));
    }

    /**
     * The Java package accessibility.
     * @since 1.64
     */
    public static enum Accessibility {
        /**
         * The package is publicly accessible by other compilation units.
         */
        EXPORTED {
            @Override
            Boolean toBoolean() {
                return Boolean.TRUE;
            }
        },
        /**
         * The package is private to owning compilation units.
         */
        PRIVATE {
            @Override
            Boolean toBoolean() {
                return Boolean.FALSE;
            }
        },
        /**
         * The accessibility is unknown.
         */
        UNKNOWN {
            @Override
            Boolean toBoolean() {
                return null;
            }
        };

        abstract Boolean toBoolean();

        @NonNull
        static Accessibility fromBoolean(@NullAllowed final Boolean b) {
            return b == Boolean.TRUE ?
                    EXPORTED :
                    b == Boolean.FALSE ?
                    PRIVATE :
                    UNKNOWN;
        }
    }

    /**
     * Result of finding accessibility of a Java package, encapsulating the answer as well as the
     * ability to listen to it.
     * @since 1.64
     */
    public static final class Result {
        private final AccessibilityQueryImplementation2.Result delegate;
        private final ChangeSupport listeners;
        //@GuardedBy("this")
        private ChangeListener spiListener;

        private Result(@NonNull final AccessibilityQueryImplementation2.Result delegate) {
            this.delegate = delegate;
            this.listeners = new ChangeSupport(this);
        }

        /**
         * Returns the accessibility.
         * @return the {@link Accessibility}
         */
        @NonNull
        public Accessibility getAccessibility() {
            return delegate.getAccessibility();
        }

        /**
         * Adds a {@link ChangeListener}.
         * @param listener the listener to be added
         */
        public void addChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            synchronized (this) {
                if (spiListener == null) {
                    spiListener = (e) -> listeners.fireChange();
                    delegate.addChangeListener(WeakListeners.change(spiListener, delegate));
                }
            }
            listeners.addChangeListener(listener);
        }

        /**
         * Removes a {@link ChangeListener}.
         * @param listener the listener to be removed
         */
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.removeChangeListener(listener);
        }
    }

    private static final class Adapter implements AccessibilityQueryImplementation2.Result {
        private final FileObject pkg;

        Adapter(@NonNull final  FileObject pkg) {
            this.pkg = pkg;
        }

        @Override
        public Accessibility getAccessibility() {
            return Accessibility.fromBoolean(
                    AccessibilityQuery.isPubliclyAccessible(pkg));
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            //Unsupported
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            //Unsupported
        }
    }
}
