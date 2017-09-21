/**
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

/* this is stub code written based on Apple EAWT package javadoc published at
 * http://developer.apple.com.  It makes compiling code which uses Apple EAWT
 * on non-Mac platforms possible.  The compiled stub classes should never be
 * included in the final product.
 */

package com.apple.eawt;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.awt.Window;

public abstract class AppEvent extends EventObject {
    AppEvent() {
        super(null);
    }

    public abstract static class FilesEvent extends AppEvent {
        public List<File> getFiles() { return null; }
    }

    public static class OpenFilesEvent extends FilesEvent {
        public String getSearchTerm() { return null;}
    }

    public static class PrintFilesEvent extends FilesEvent {}

    public static class OpenURIEvent extends AppEvent {
        public URI getURI() { return null;}
    }

    public static class AboutEvent extends AppEvent { }
 
    public static class PreferencesEvent extends AppEvent { }

    public static class QuitEvent extends AppEvent { }

    public static class AppReOpenedEvent extends AppEvent { }

    public static class AppForegroundEvent extends AppEvent {  }

    public static class AppHiddenEvent extends AppEvent {  }

    public static class UserSessionEvent extends AppEvent {  }

    public static class ScreenSleepEvent extends AppEvent {  }

    public static class SystemSleepEvent extends AppEvent {  }

    public static class FullScreenEvent extends AppEvent {
        public Window getWindow() { return null;}
    }
}
