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

/*
 * "MyFontMetrics"
 * MyFontMetrics.java 1.5 01/07/10
 */
package org.netbeans.lib.terminalemulator;

import java.awt.*;
import java.util.AbstractMap;
import java.util.HashMap;

class MyFontMetrics {

    /**
     * WidthCache contains a byte array that maps a character to it's cell width.
     * It also keeps track of whether we've discovered that we're dealing with a
     * font that has wide characters. This information is kept with WidthCache
     * because the caches are shared between MyFontMetrics and we test for
     * multi-cellnes only on a cache miss. So we got into a situation where one
     * Term got a wide character, missed the cache, figured that it's multi-cell,
     * then another Term got the same character didn't miss the cache and didn't
     * set it's own multi-cell bit.
     *
     * The reference counting stuff is explained with CacheFactory.
     */
    static class WidthCache {

        byte[] cache = new byte[Character.MAX_VALUE + 1];
        int reference_count = 1;

        public void up() {
            reference_count++;
            if (reference_count == 1) {
                cache = new byte[Character.MAX_VALUE + 1];
            }
        }

        public void down() {
            if (reference_count == 0) {
                return;
            }
            reference_count--;
            if (reference_count == 0) {
                cache = null;
            }
        }

        public boolean isMultiCell() {
            return multiCell;
        }

        public void setMultiCell(boolean multiCell) {
            this.multiCell = multiCell;
        }
        private boolean multiCell = false;
    }

    /**
     *
     * CacheFactory doles out WidthCaches.
     *
     * These caches are 64Kb (Character.MAX_VALUE+1) and we don't really want 
     * Each interp to have it's own. So we share them in a map using FontMetrics
     * as a key. Unfortunately stuff will accumulate in the map. A WeakHashMap
     * is no good because the keys (FontMetrics) are usually alive. For all I
     * know Jave might be cacheing them in turn. I actually tried using a
     * WeakHashMap and wouldn't see things going away, even after System.gc().
     * <p>
     * So we get this slightly more involved manager.
     * <br>
     * A WidthCache holds on to the actual WidthCache and reference counts it.
     * When the count goes to 0 the actual cache array is "free"d be nulling
     * it's reference. To make the reference count go down CacheFactory.disposeBy()
     * is used. And that is called from MyFontMetrics.finalize().
     *
     * NOTE: The actual WidthCache's instances _will_ accumulate, but they are small and 
     * there are only so many font variations an app can go through. As I
     * mentioned above using a WeakHashMap doesn't help much because WidthCache's
     * are keyed by relatively persistent FontMetrics.
     */
    private static final class CacheFactory {
        private CacheFactory() {
        }

        static synchronized WidthCache cacheForFontMetrics(FontMetrics fm) {
            WidthCache entry = map.get(fm);
            if (entry == null) {
                entry = new WidthCache();
                map.put(fm, entry);
            } else {
                entry.up();
            }
            return entry;
        }

        static synchronized void disposeBy(FontMetrics fm) {
            WidthCache entry = map.get(fm);
            if (entry != null) {
                entry.down();
            }
        }
        private static final AbstractMap<FontMetrics, WidthCache> map = new HashMap<>();

    }

    public MyFontMetrics(Component component, Font font) {
        fm = component.getFontMetrics(font);
        width = fm.charWidth('m');
        height = fm.getHeight();
        ascent = fm.getAscent();
        leading = fm.getLeading();

        // HACK
        // From all I can tell both xterm and DtTerm ignore the leading.
        // Maybe X font's don't have a leading and Java sets it to one
        // artificially? Because unless I do the below I get one extra pixel
        // between my lines.
        //
        // Some code takes the leading into account and some code doesn't.
        // the following makes things match up, but if we ever undo this
        // we'll have to go and adjust how everything is drawn (cursor,
        // reverse-video attribute, underscore, bg stripe, selection etc.

        height -= leading;
        leading = 0;

        cwidth_cache = CacheFactory.cacheForFontMetrics(fm);
    }

    @Override
    protected void finalize() {
        CacheFactory.disposeBy(fm);
    }
    public int width;
    public int height;
    public int ascent;
    public int leading;
    public FontMetrics fm;
    private WidthCache cwidth_cache;

    public boolean isMultiCell() {
        return cwidth_cache.isMultiCell();
    }

    /*
     * Called 'wcwidth' for historical reasons. (see wcwidth(3) on unix.)
     * Return how many cells this character occupies.
     */
    public int wcwidth(char c) {
        int cell_width = cwidth_cache.cache[c];	// how many cells wide

        if (cell_width == 0) {
            // width not cached yet so figure it out
            int pixel_width = fm.charWidth(c);

            if (pixel_width == width) {
                cell_width = 1;

            } else if (pixel_width == 0) {
                cell_width = 1;

            } else {
                // round pixel width to multiple of cell size
                // then distill into a width in terms of cells.
                int mod = pixel_width % width;
                int rounded_width = pixel_width;
                if (mod > (width / 2)) {
                    rounded_width = pixel_width + (width - mod);
                } else {
                    rounded_width = pixel_width - mod;
                }
                cell_width = rounded_width / width;
                if (cell_width == 0) {
                    cell_width = 1;
                } else if (cell_width > 1) {
                    cwidth_cache.setMultiCell(true);
                }
            }

            cwidth_cache.cache[c] = (byte) cell_width;
        }
        return cell_width;
    }

    /*
     * Shift to the multi-cell character regime as soon as we spot one.
     * The actual work is done in wcwidth() itself.
     */
    void checkForMultiCell(char c) {
        wcwidth(c);
    }
}
