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
package org.openide.util.svg;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.ParsedDocument;
import com.github.weisj.jsvg.parser.ResourceLoader;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.awt.NullPlatformSupport;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.CachedHiDPIIcon;
import org.openide.util.Parameters;

/**
 * An icon loaded from an SVG file resource. Renders in high resolution on HiDPI displays.
 * Thread-safe.
 */
final class SVGIcon extends CachedHiDPIIcon {
    private static final Logger LOG = Logger.getLogger(SVGIcon.class.getName());
    /* A limit of 8192 pixels on each side means the resulting maximum image buffer would take 268
    megabytes. It's also twice twice as long as the longest side of a 4K display. This is small
    enough to avoid an OutOfMemoryError but large enough to cater for most SVG loading scenarios.
    Photoshop had 10000 pixels as a maximum limit for many years. */
    private static final int MAX_DIMENSION_PIXELS = 8192;
    /* XML document factories are expensive to initialize, so do it once per thread only. This
    optimization was originally done for the Batik SVG library, but I suspect it might be beneficial
    for JSVG as well. The SVGLoader constructor does initialize some XML parser stuff. */
    private static final ThreadLocal<SVGLoader> SVG_LOADER =
            new ThreadLocal<SVGLoader>()
    {
        @Override
        protected SVGLoader initialValue() {
            return new SVGLoader();
        }
    };

    private final URL url;
    /**
     * Cache of the parsed SVG document. Just painting the SVGDocument is probably faster than also
     * re-parsing the underlying SVG file, yet we want to avoid keeping potentially complex object
     * trees in memory for the lifetime of the Icon instance. Thus we allow the SVGDocument to be
     * garbage collected after the first paint. The rasterized bitmap will be cached separately by
     * the superclass.
     */
    private WeakReference<SVGDocument> svgDocumentWeakRef;
    /**
     * A strong reference version of {@link #svgDocumentWeakRef}, which can be set to ensure that
     * the latter is not yet garbage collected. Used to ensure that the initially loaded SVGDocument
     * is cached at least until the first time the icon is painted. May be null.
     */
    private SVGDocument svgDocumentStrongRef;

    private SVGIcon(URL url, SVGDocument initialSVGDocument, int width, int height) {
        super(width, height);
        Parameters.notNull("url", url);
        Parameters.notNull("initialSVGDocument", initialSVGDocument);
        this.url = url;
        this.svgDocumentStrongRef = initialSVGDocument;
        this.svgDocumentWeakRef = new WeakReference<SVGDocument>(initialSVGDocument);
    }

    public static Icon load(URL url) throws IOException {
        Parameters.notNull("url", url);
        Dimension size = new Dimension();
        SVGDocument initialSVGDocument = loadSVGDocument(url, size);
        return new SVGIcon(url, initialSVGDocument, size.width, size.height);
    }

    /**
     * Get the {@code SVGDocument}, re-loading it from the original resource if a cached instance
     * is no longer available. Once this method has been called at least once, garbage collection
     * may cause the cache to be cleared.
     */
    private synchronized SVGDocument getSVGDocument() throws IOException {
        SVGDocument ret = svgDocumentWeakRef.get();
        if (ret != null) {
            // Allow the SVGDocument to be garbage collected after the initial paint.
            svgDocumentStrongRef = null;
            return ret;
        }
        ret = loadSVGDocument(url, null);
        svgDocumentWeakRef = new WeakReference<SVGDocument>(ret);
        return ret;
    }

    /**
     * Load the original SVG resource.
     *
     * @param toSize if not null, will be set to the image's size
     */
    private static SVGDocument loadSVGDocument(URL url, Dimension toSize) throws IOException {
        Parameters.notNull("url", url);

        final SVGDocument svgDocument;
        FloatSize documentSize;
        try (InputStream is = url.openStream()) {
            // Explicitly deny loading of external URLs.

            /* Handle e.g. <image href="https://example.com/image.png"> elements. Tested in
            testLoadImageWithExternalImageHref. */
            List<IOException> externalResourceExceptions = new ArrayList<>();
            ResourceLoader resourceLoader = (ParsedDocument nnpd, URI nnuri) -> {
              IOException e = new IOException("External resource loading from SVG file not permitted ("+
                  nnuri + " from " + url + ")");
              externalResourceExceptions.add(e);
              throw e;
            };
            /* Handle e.g. <use xlink:href="http://foobar/not/exists#text"> elements. Tested in
            testLoadImageWithExternalUseXlinkHref. */
            DenyingElementLoader elementLoader = new DenyingElementLoader();

            svgDocument = SVG_LOADER.get().load(is, null, LoaderContext.builder()
                .resourceLoader(resourceLoader)
                .elementLoader(elementLoader)
                .build());
            if (!elementLoader.getAttemptedExternalURLsLoaded().isEmpty()) {
              throw new IOException("SVG loading failed; external document loading prohibited (" +
                  elementLoader.getAttemptedExternalURLsLoaded() + ")");
            }
            if (!externalResourceExceptions.isEmpty()) {
              IOException e = new IOException("SVG loading failed due to disallowed external resources");
              for (IOException e2 : externalResourceExceptions) {
                  e.addSuppressed(e2);
              }
              throw e;
            }
            if (svgDocument == null) {
                throw new IOException(
                        "SVG loading failed for " + url + " (SVGLoader.load returned null)");
            }
            documentSize = svgDocument.size();
        } catch (RuntimeException e) {
            /* Rethrow any uncaught exceptions that could be thrown when parsing invalid SVG files. */
            throw new IOException("Error parsing SVG file", e);
        }
        if (toSize != null) {
            int width = (int) Math.ceil(documentSize.getWidth());
            int height = (int) Math.ceil(documentSize.getHeight());
            final int widthLimited = Math.min(MAX_DIMENSION_PIXELS, width);
            final int heightLimited = Math.min(MAX_DIMENSION_PIXELS, height);
            if (width != widthLimited || height != heightLimited) {
                LOG.log(Level.WARNING,
                        "SVG image {0} too large (dimensions were {1}x{2}, each side can be at most {3}px)",
                        new Object[]{url, width, height, MAX_DIMENSION_PIXELS});
            } else if (width <= 1 && height <= 1) {
                LOG.log(Level.WARNING,
                        "SVG image {0} did not specify a width/height, or is incorrectly sized", url);
            }
            toSize.width = widthLimited;
            toSize.height = heightLimited;
        }
        return svgDocument;
    }

    private static RenderingHints createHints() {
        Map<RenderingHints.Key, Object> hints = new LinkedHashMap<RenderingHints.Key, Object>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        /* Ensure that outlined strokes (strokes converted to solid shapes) appear the same as
        regular strokes, as they do during editing in Adobe Illustrator. This hint is also
        specifically recommended by JSVG's README ( https://github.com/weisJ/jsvg ). */
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return new RenderingHints(hints);
    }

    @Override
    protected Image createAndPaintImage(
            Component c, ColorModel colorModel, int deviceWidth, int deviceHeight, double scale)
    {
        BufferedImage img = createBufferedImage(colorModel, deviceWidth, deviceHeight);
        final Graphics2D g = img.createGraphics();
        try {
            g.scale(scale, scale);
            try {
                SVGDocument svgDocument = getSVGDocument();
                g.addRenderingHints(createHints());
                svgDocument.renderWithPlatform(NullPlatformSupport.INSTANCE, g, null);
            } catch (IOException e) {
                LOG.log(Level.WARNING,
                        "Unexpected exception while re-loading an SVG file that previously loaded successfully", e);
            }
        } finally {
            g.dispose();
        }
        return img;
    }
}
