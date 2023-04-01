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
package org.netbeans.swing.laf.flatlaf;

import java.awt.image.RGBImageFilter;

public final class OklabDarkIconFilter extends RGBImageFilter {
  private final float fnConst;
  private final float fnLimit;
  private final float[] res = new float[3];

  public OklabDarkIconFilter(float fnConst, float fnLimit) {
    this.fnConst = fnConst;
    this.fnLimit  = fnLimit;
  }

  public OklabDarkIconFilter() {
    // These parameters found by trial and error.
    this.fnConst = 0.4f;
    this.fnLimit = 0.8f;
  }

  @Override
  public int filterRGB(int x, int y, int rgba) {
    int   alphaInt = ( rgba >> 24) & 0xFF;
    float r        = ((rgba >> 16) & 0xFF) / 255f;
    float g        = ((rgba >> 8 ) & 0xFF) / 255f;
    float b        = ((rgba      ) & 0xFF) / 255f;

    float rLinear = toLinear(r);
    float gLinear = toLinear(g);
    float bLinear = toLinear(b);

    linear_srgb_to_oklab(rLinear, gLinear, bLinear, res);
    float okL = res[0];
    float okA = res[1];
    float okB = res[2];

    // This formula deduced by trial and error.
    okL = Math.max(0f, Math.min(1.0f,
        Math.max(
          Math.min((1.0f - okL) + fnConst, fnLimit),
                   (1.0f - okL)
        )));

    oklab_to_linear_srgb(okL, okA, okB, res);
    rLinear = res[0];
    gLinear = res[1];
    bLinear = res[2];

    r = fromLinear(rLinear);
    g = fromLinear(gLinear);
    b = fromLinear(bLinear);

    return (alphaInt      << 24) |
           (floatToInt(r) << 16) |
           (floatToInt(g) << 8 ) |
           (floatToInt(b)      );
  }

  private int floatToInt(float x) {
    int ret = (int) (x * 255f + 0.5f);
    return ret < 0 ? 0 : (ret > 255 ? 255 : ret);
  }

  private float fromLinear(float x) {
    // Formula from https://bottosson.github.io/posts/colorwrong
    return x >= 0.0031308f
        ? (1.055f * (float) Math.pow(x, 1.0 / 2.4) - 0.055f)
        : (12.92f * x);
  }

  private float toLinear(float x) {
    // Formula from https://bottosson.github.io/posts/colorwrong
    return x >= 0.04045f
        ? (float) Math.pow((x + 0.055) / (1 + 0.055), 2.4)
        : (x / 12.92f);
  }

  // From https://bottosson.github.io/posts/oklab/ (declared in public domain)
  private void linear_srgb_to_oklab(float r, float g, float b, float[] ret) {
    float l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
    float m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
    float s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

    float l_ = (float) Math.cbrt(l);
    float m_ = (float) Math.cbrt(m);
    float s_ = (float) Math.cbrt(s);

    ret[0] = 0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_;
    ret[1] = 1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_;
    ret[2] = 0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_;
  }

  // From https://bottosson.github.io/posts/oklab/ (declared in public domain)
  private void oklab_to_linear_srgb(float L, float a, float b, float[] ret) {
      float l_ = L + 0.3963377774f * a + 0.2158037573f * b;
      float m_ = L - 0.1055613458f * a - 0.0638541728f * b;
      float s_ = L - 0.0894841775f * a - 1.2914855480f * b;

      float l = l_ * l_ * l_;
      float m = m_ * m_ * m_;
      float s = s_ * s_ * s_;

      ret[0] = +4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
      ret[1] = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
      ret[2] = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;
  }
}
