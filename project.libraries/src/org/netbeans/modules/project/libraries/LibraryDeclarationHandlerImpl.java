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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.project.libraries;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.BaseUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Read content of library declaration XML document.
 *
 * @author Petr Kuzel
 */
public class LibraryDeclarationHandlerImpl implements LibraryDeclarationHandler {


    private LibraryImplementation library;
    private String libraryType;
    private String libraryDescription;
    private String libraryName;
    private String localizingBundle;
    private String displayName;
    private final Map<String,List<URL>> contentTypes = new HashMap<String,List<URL>>();
    private final Map<String,String> properties = new HashMap<String, String>();

    // last volume
    private List<URL> cpEntries;
    //last volume type
    private String contentType;
    //parsing volume?
    private State state = State.LIB;
    //Used flag preventing from being reused
    private final AtomicBoolean used = new AtomicBoolean();
    
    //Propery name - valid in State.PROPERTY
    private String propName;
    //Propery value - valid in State.PROPERTY
    private String propValue;

    @Override
    public void startDocument() {
        if (used.getAndSet(true)) {
            throw new IllegalStateException("The LibraryDeclarationHandlerImpl was already used, create a new instance");   //NOI18N
        }
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void start_volume(final Attributes meta) throws SAXException {
        cpEntries = new ArrayList<URL>();
        this.state = State.VOLUME;
    }

    @Override
    public void end_volume() throws SAXException {
        contentTypes.put (contentType, cpEntries);
        this.state = State.LIB;
        this.contentType = null;
    }

    @Override
    public void handle_type(final String data, final Attributes meta) throws SAXException {
		if (data == null || data.length () == 0) {
			throw new SAXException ("Empty value of type element");	//NOI18N
		}
        if (this.state == State.VOLUME) {
            this.contentType = data;
        }
        else {
            this.libraryType = data;
        }        
    }

    @Override
    public String start_library(final String nameSpace, final Attributes meta) throws SAXException {
        final String version = meta.getValue("version");
        if (LibraryDeclarationParser.VER_1.equals(version)) {
            return "";  //NOI18N
        } else if (LibraryDeclarationParser.VER_2.equals(version)) {
            return LibraryDeclarationParser.LIBRARY_NS2;
        } else if (LibraryDeclarationParser.VER_3.equals(version)) {
            return LibraryDeclarationParser.LIBRARY_NS3;
        } else {
            throw new SAXException("Invalid librray descriptor version"); // NOI18N
        }
    }

    @Override
    public void end_library() throws SAXException {
        boolean update;
        if (this.library != null) {
            if (this.libraryType == null || !this.libraryType.equals(this.library.getType())) {
                throw new SAXParseException("Changing library type of library: "+this.libraryName+" from: "+
                        library.getType()+" to: " + libraryType, null); //NOI18N
            }
            update = true;
        } else {
            if (this.libraryType == null) {
                throw new SAXParseException("Unspecified library type for: "+this.libraryName, null); //NOI18N
            }
            LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider(this.libraryType);
            if (provider == null) {
                throw new UnknownLibraryTypeException(libraryName, libraryType);
            }
            this.library = provider.createLibrary();
            update = false;
            LibrariesStorage.LOG.log(Level.FINE, "LibraryDeclarationHandlerImpl library {0} type {1} found", new Object[] { this.libraryName, this.libraryType });
        }
        if (!update || !BaseUtilities.compareObjects(this.library.getLocalizingBundle(), localizingBundle)) {
            this.library.setLocalizingBundle (this.localizingBundle);
        }
        if (!update || !BaseUtilities.compareObjects(this.library.getName(), libraryName)) {
            this.library.setName (this.libraryName);
        }
        if (!update || !BaseUtilities.compareObjects(this.library.getDescription(), libraryDescription)) {
            this.library.setDescription (this.libraryDescription);
        }
        LibrariesSupport.setDisplayName(this.library,displayName);
        LibrariesSupport.setProperties(this.library, properties);
        for (Map.Entry<String,List<URL>> entry : contentTypes.entrySet()) {
            String contentType = entry.getKey();
            List<URL> cp = entry.getValue();
            try {
                if (!update || !urlsEqual(this.library.getContent(contentType),cp)) {
                    this.library.setContent(contentType, cp);
                }
            } catch (IllegalArgumentException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void handle_resource(URL data, final Attributes meta) throws SAXException {
        if (data != null) {
            cpEntries.add(data);
        }
    }

    @Override
    public void handle_name(final String data, final Attributes meta) throws SAXException {
        if (state == State.PROPERTY) {
            this.propName = data;
        } else {
            this.libraryName = data;
        }
    }

    @Override
    public void handle_description (final String data, final Attributes meta) throws SAXException {
        libraryDescription = data;
    }

    @Override
    public void handle_localizingBundle (final String data, final Attributes meta) throws SAXException {
        this.localizingBundle = data;
    }

    @Override
    public void handle_displayName (String data, Attributes meta) throws SAXException {
        this.displayName = data;
    }

    public void setLibrary (LibraryImplementation library) {
        this.library = library;
    }

    public LibraryImplementation getLibrary () {
        return this.library;
    }

    @Override
    public void start_properties(Attributes meta) throws SAXException {
        state = State.PROPERTIES;
        properties.clear();
    }

    @Override
    public void end_properties() throws SAXException {
        state = State.LIB;
    }

    @Override
    public void start_property(Attributes meta) throws SAXException {
        this.propName = null;
        this.propValue = null;
        state = State.PROPERTY;
    }

    @Override
    public void end_property() throws SAXException {
        state = State.PROPERTIES;
        assert propName != null;
        properties.put(propName, propValue);
    }

    @Override
    public void handle_value(String data, Attributes meta) throws SAXException {
        this.propValue = data;
    }

    public static class UnknownLibraryTypeException extends SAXException {
        public final String type;
        private UnknownLibraryTypeException(
            final String libraryName,
            final String libraryType) {
            super ("Cannot create library: "+libraryName+" of unknown type: " +libraryType,null);
            this.type = libraryType;
        }
    }
    
    private static enum State {LIB, VOLUME, PROPERTIES, PROPERTY};


    private static boolean urlsEqual (final Collection<? extends URL> first, final Collection<? extends URL> second) {
        assert first != null;
        assert second != null;
        if (first.size() != second.size()) {
            return false;
        }
        for (Iterator<? extends URL> fit = first.iterator(), sit = second.iterator(); fit.hasNext();) {
            final URL furl = fit.next();
            final URL surl = sit.next();
            if (!furl.toExternalForm().equals(surl.toExternalForm())) {
                return false;
            }
        }
        return true;
    }

}
