/** *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************** */
package org.jetbrains.kotlin.filesystem;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.Set;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = VirtualSourceProvider.class)
public class KotlinVirtualSourceProvider implements VirtualSourceProvider {

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("kt");
    }

    @Override
    public boolean index() {
        return false;
    }

    @Override
    public void translate(Iterable<File> files, File sourceRoot, Result result) {
        VirtualSourceUtilsKt.translate(files, result);
    }
    
}
