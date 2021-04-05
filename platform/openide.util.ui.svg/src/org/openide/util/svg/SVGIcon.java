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
import java.awt.geom.Dimension2D;
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
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.openide.util.CachedHiDPIIcon;
import org.openide.util.Parameters;
import org.w3c.dom.Document;

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
    // XML document factories are expensive to initialize, so do it once per thread only.
    private static final ThreadLocal<SAXSVGDocumentFactory> DOCUMENT_FACTORY =
            new ThreadLocal<SAXSVGDocumentFactory>()
    {
        @Override
        protected SAXSVGDocumentFactory initialValue() {
            return new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        }
    };

    private final URL url;
    /**
     * Cache of the parsed SVG document. Just painting the GraphicsNode is much faster than also
     * re-parsing the underlying SVG file, yet we want to avoid keeping potentially complex object
     * trees in memory for the lifetime of the Icon instance. Thus we allow the GraphicsNode to be
     * garbage collected after the first paint. The rasterized bitmap will be cached separately by
     * the superclass.
     */
    private WeakReference<GraphicsNode> graphicsNodeWeakRef;
    /**
     * A strong reference version of {@link #graphicsNodeWeakRef}, which can be set to ensure that
     * the latter is not yet garbage collected. Used to ensure that the initially loaded
     * GraphicsNode is cached at least until the first time the icon is painted. May be null.
     */
    private GraphicsNode graphicsNodeStrongRef;

    private SVGIcon(URL url, GraphicsNode initialGraphicsNode, int width, int height) {
        super(width, height);
        Parameters.notNull("url", url);
        Parameters.notNull("initialGraphicsNode", initialGraphicsNode);
        this.url = url;
        this.graphicsNodeStrongRef = initialGraphicsNode;
        this.graphicsNodeWeakRef = new WeakReference<GraphicsNode>(initialGraphicsNode);
    }

    public static Icon load(URL url) throws IOException {
        Parameters.notNull("url", url);
        Dimension size = new Dimension();
        GraphicsNode initialGraphicsNode = loadGraphicsNode(url, size);
        return new SVGIcon(url, initialGraphicsNode, size.width, size.height);
    }

    /**
     * Get the {@code GraphicsNode}, re-loading it from the original resource if a cached instance
     * is no longer available. Once this method has been called at least once, garbage collection
     * may cause the cache to be cleared.
     */
    private synchronized GraphicsNode getGraphicsNode() throws IOException {
        GraphicsNode ret = graphicsNodeWeakRef.get();
        if (ret != null) {
            // Allow the GraphicsNode to be garbage collected after the initial paint.
            graphicsNodeStrongRef = null;
            return ret;
        }
        ret = loadGraphicsNode(url, null);
        graphicsNodeWeakRef = new WeakReference<GraphicsNode>(ret);
        return ret;
    }

    /**
     * Load the original SVG resource.
     *
     * @param toSize if not null, will be set to the image's size
     */
    private static GraphicsNode loadGraphicsNode(URL url, Dimension toSize)
            throws IOException
    {
        Parameters.notNull("url", url);
        final GraphicsNode graphicsNode;
        final Dimension2D documentSize;
        final Document doc;
        InputStream is = url.openStream();
        try {
            // See http://batik.2283329.n4.nabble.com/rendering-directly-to-java-awt-Graphics2D-td3716202.html
            SAXSVGDocumentFactory factory = DOCUMENT_FACTORY.get();
            /* Don't provide an URI here; we shouldn't commit to supporting relative links from
            loaded SVG documents. */
            doc = factory.createDocument(null, is);
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext bctx = new BridgeContext(userAgent, loader);
            try {
                bctx.setDynamicState(BridgeContext.STATIC);
                graphicsNode = new GVTBuilder().build(bctx, doc);
                documentSize = bctx.getDocumentSize();
            } finally {
                bctx.dispose();
            }
        } catch (RuntimeException e) {
            /* Rethrow the many different exceptions that can occur when parsing invalid SVG files;
            DOMException, BridgeException etc. */
            throw new IOException("Error parsing SVG file", e);
        } finally {
            is.close();
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
        return graphicsNode;
    }

    private static RenderingHints createHints() {
        Map<RenderingHints.Key, Object> hints = new LinkedHashMap<RenderingHints.Key, Object>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        /* Ensure that outlined strokes (strokes converted to solid shapes) appear the same as
        regular strokes, as they do during editing in Adobe Illustrator. */
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return new RenderingHints(hints);
    }

    @Override
    protected Image createAndPaintImage(
            Component c, ColorModel colorModel, int deviceWidth, int deviceHeight, double scale)
    {
        BufferedImage img = createBufferedImage(colorModel, deviceWidth, deviceHeight);
        /* Use Batik's createGraphics method to improve performance and avoid the
        "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" warning. */
        final Graphics2D g = GraphicsUtil.createGraphics(img);
        try {
            g.scale(scale, scale);
            try {
                GraphicsNode graphicsNode = getGraphicsNode();
                g.addRenderingHints(createHints());
                graphicsNode.paint(g);
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
