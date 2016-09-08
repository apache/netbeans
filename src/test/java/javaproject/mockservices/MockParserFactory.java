/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package javaproject.mockservices;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;

/**
 *
 * @author Alexander.Baratynski
 */
@MimeRegistration(mimeType = "text/x-java", service = ParserFactory.class)
public class MockParserFactory extends ParserFactory {

    @Override
    public Parser createParser(Collection<Snapshot> snapshots) {
        return new JavacParserFactory().createParser(snapshots);
    }
    
}
