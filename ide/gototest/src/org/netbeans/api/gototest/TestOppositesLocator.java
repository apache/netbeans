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
package org.netbeans.api.gototest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.spi.gototest.TestLocator.LocationListener;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Find one or multiple test files for a source file,
 * and one or multiple source files for a test file.
 *
 * @since 1.57
 */
public final class TestOppositesLocator {

    private static final RequestProcessor WORKER = new RequestProcessor(TestOppositesLocator.class.getName(), 1, false, false);

    /**
     * The default instance of TestOppositesLocator.
     *
     * @return the default instance of TestOppositesLocator
     */
    public static TestOppositesLocator getDefault() {
        return new TestOppositesLocator();
    }

    private TestOppositesLocator() {}

    /**
     * Given the file and position in the file, if the:
     * <ul>
     *     <li> given file is a source file, find corresponding test file or test files, if exist.</li>
     *     <li> given file is a test file, find corresponding source file or source files, if exist.</li>
     * </ul>
     *
     * @param fo the file for which the opposites should be found
     * @param caretOffset position in the file, or {@code -1} if unknown
     * @return a result describing either an error, or a possibly empty list of locations found;
     *         note one of {@code errorMessage} and {@code locations} is always {@code null},
     *         and one always non-{@code null}.
     */
    @NbBundle.Messages("No_Test_Or_Tested_Class_Found=No Test or Tested class found")
    public CompletableFuture<LocatorResult> findOpposites(FileObject fo, int caretOffset) {
        if (!isSupportedFileType(fo)) {
            CompletableFuture<LocatorResult> result = new CompletableFuture<>();

            result.complete(new LocatorResult(Bundle.No_Test_Or_Tested_Class_Found(), null, null));
            return result;
        }
        else {
            return populateLocationResults(fo, caretOffset)
                    .thenApply(locations -> new LocatorResult(null,
                                                              locations.stream()
                                                                       .filter(l -> l.getErrorMessage() != null)
                                                                       .map(l -> l.getErrorMessage())
                                                                       .collect(Collectors.toList()),
                                                              locations.stream()
                                                                       .filter(l -> l.getFileObject()!= null)
                                                                       .map(l -> new Location(l.getFileObject(), l.getOffset()))
                                                                       .collect(Collectors.toList())));
        }
    }

    private CompletableFuture<? extends List<LocationResult>> populateLocationResults(FileObject fo, int caretOffset) {
        Collection<? extends TestLocator> locators = Lookup.getDefault()
                                                           .lookupAll(TestLocator.class)
                                                           .stream()
                                                           .filter(tl -> tl.appliesTo(fo))
                                                           .collect(Collectors.toList());
        CompletableFuture<ArrayList<LocationResult>> result = new CompletableFuture<>();

        result.complete(new ArrayList<>());

        for (TestLocator locator : locators) {
            if (locator.appliesTo(fo)) {
                CompletableFuture<List<LocationResult>> currentFuture = new CompletableFuture<>();

                if (locator.asynchronous()) {
                    locator.findOpposite(fo, caretOffset, new LocationListener() {
                        @Override
                        public void foundLocation(FileObject fo, LocationResult location) {
                            List<LocationResult> resultList =
                                location != null ? Collections.singletonList(location)
                                                 : Collections.emptyList();

                            currentFuture.complete(resultList);
                        }
                    });
                } else {
                    WORKER.post(() -> {
                        try {
                            LocationResult opposite = locator.findOpposite(fo, caretOffset);
                            List<LocationResult> resultList =
                                opposite != null ? Collections.singletonList(opposite)
                                                 : Collections.emptyList();

                            currentFuture.complete(resultList);
                        }catch(ThreadDeath td){
                            throw td;
                        }catch (Throwable t) {
                            currentFuture.completeExceptionally(t);
                        }
                    });
                }

                result = result.thenCombine(currentFuture, (accumulator, currentList) -> {
                    accumulator.addAll(currentList);
                    return accumulator;
                });
            }
        }

        return result;
    }

    private TestLocator getLocatorFor(FileObject fo) {
        Collection<? extends TestLocator> locators = Lookup.getDefault().lookupAll(TestLocator.class);
        for (TestLocator locator : locators) {
            if (locator.appliesTo(fo)) {
                return locator;
            }
        }

        return null;
    }

    private boolean isSupportedFileType(FileObject fo) {
        TestLocator locator = fo != null ? getLocatorFor(fo) : null;
        if (locator != null) {
            return locator.getFileType(fo) != TestLocator.FileType.NEITHER;
        }

        return false;
    }

    /**
     * A description of the found opposite files. Exactly one of {@code errorMessage}
     * {@code locations} will be non-null;
     */
    public static final class LocatorResult {
        private final String errorMessage;
        private final Collection<? extends String> providerErrors;
        private final Collection<? extends Location> locations;

        private LocatorResult(String errorMessage,
                              List<? extends String> providerErrors,
                              List<? extends Location> locations) {
            if (errorMessage == null && locations == null) {
                throw new IllegalArgumentException("Both errorMessage and locations is null!");
            }
            if (errorMessage != null && locations != null) {
                throw new IllegalArgumentException("Both errorMessage and locations is non-null!");
            }
            if (providerErrors == null ^ locations == null) {
                throw new IllegalArgumentException("Both providerErrors and locations must either be null or non-null");
            }
            this.errorMessage = errorMessage;
            this.providerErrors = providerErrors != null ? Collections.unmodifiableList(providerErrors) : null;
            this.locations = locations != null ? Collections.unmodifiableList(locations) : null;
        }

        /**
         * Get the error message if present.
         *
         * @return error message
         */
        public @CheckForNull String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Get error messages provided by the providers.
         *
         * @return the errors from the providers.
         */
        public @CheckForNull Collection<? extends String> getProviderErrors() {
            return providerErrors;
        }

        /**
         * Get the locations if present.
         *
         * @return the found locations.
         */
        public @CheckForNull Collection<? extends Location> getLocations() {
            return locations;
        }

    }

    /**
     * A description of a target location.
     */
    public static final class Location {
        private final FileObject file;
        private final int offset;

        /**
         * Construct a Location from a given file and offset.
         * @param file The FileObject of the opposite file.
         * @param offset The offset in the file, or -1 if the offset
         *   is unknown.
         */
        public Location(FileObject file, int offset) {
            this.file = file;
            this.offset = offset;
        }

        /**
         * Get the FileObject associated with this location
         * @return The FileObject for this location, or null if
         *   this is an invalid location. In that case, consult
         *   {@link #getErrorMessage} for more information.
         */
        public FileObject getFileObject() {
            return file;
        }

        /**
         * Get the offset associated with this location, if any.
         * @return The offset for this location, or -1 if the offset
         *   is not known.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * Get the proper display name for this location.
         *
         * @return the display name for this location
         */
        @Messages("DN_Error=Error")
        public String getDisplayName() {
            return file != null ? file.getName() : Bundle.DN_Error();
        }
    }

}