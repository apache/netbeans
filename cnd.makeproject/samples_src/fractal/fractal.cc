/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <complex>
#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <sys/time.h>

using namespace std;

#define WI 1000 //1280            // consider (wi,hi) as the R-axis and I-axis on a complex plane
#define HI 800 //1024
#define ITER 100
#define MAXISIZE 500000    // maximum iterated elements allowed to written per file

long double currLeft = -2, currRight = 2, currTop = 1.6, currBottom = -1.6, inc = .001;

template <class T> class Matrix {
private:
    unsigned wi, hi;
    T* mtrx;
public:

    Matrix(unsigned w, unsigned h) : wi(w), hi(h), mtrx(new T[w*h]) {
    };

    Matrix(const Matrix& M) : wi(M.wi), hi(M.hi), mtrx(new T[M.wi*M.hi]) {
        memcpy(mtrx, M.mtrx, wi * hi * sizeof (T));
    };

    ~Matrix() {
        delete [] mtrx;
    };

    void zero() {
        memset(mtrx, 0, sizeof (T) * wi * hi);
    }

    unsigned width() {
        return wi;
    }

    unsigned height() {
        return hi;
    }

    T * operator [] (unsigned i) {
        return mtrx + (i * hi);
    }
};

/* Note: Distinction between a Mandelbrot set and Buddhabrot set,
 * Buddhabrot is still a Mandelbrot set, but display the number of
 * iterations (i) at position z, instead of c.
 * Thus visually Buddhabrot does not sweep through the plane systematically.
 * Depends on inputs, increments, and locality, it might paint the complex
 * plane radially, with certain rotation.
 */
void Mandelbrot(const size_t wi, const size_t hi, const size_t it) // Basic Mandelbrot calculation
{
    Matrix <int> hits(wi, hi), max(wi, hi);
    Matrix <complex<long double> > Buf(wi, hi);

    hits.zero();
    max.zero();
    Buf.zero();

    long double wide = wi, high = hi, iter = it;
    int i = 0, x = 0, y = 0, fcnt = 0, icnt = 0;
    long double a = currLeft, b = currBottom;

    long double inc = (currRight - currLeft) / wide;
    long double yinc = (currTop - currBottom) / high;
    char filename[80];
    strcpy(filename, "output_pns_0.dat");
    FILE* fh = fopen(filename, (char *) &"a+b");
    if (fh == NULL) {
        printf("Cannot create output files\n");
        printf("Usage: Execute native/seq_demo2 from .../sampledir/C++/Fractal/Java\n");
        exit(0);
    }

    // Mandelbrot fractals: f(z)=z^2+c, where z, c are complex
    // Note: the exponents for z could be varied to achieve other types of fractals
    // Varying const. c by sweeping across the complex plane
    for (a = currLeft; a < currRight; a += inc) {
        complex<long double> c(a, 0);

        for (b = currBottom; b < currTop; b += yinc) {
            i = 0;
            complex<long double> z(0, 0);
            c = complex<long double>(c.real(), b);

            // divergence test
            // (note 1) computation complexity, or resolution finess, is determined by pre-defined iteration limit, iter
            // (note 2) a graphic interface would take (n, z.real, z.img) and display the result
            while (abs(z) < 2 && i <= iter) {

                z = (z * z) + c; // heart-and-soul of the Mandelbrot fractal

                // check if z is inbound
                if (z.real() > currLeft && z.real() < currRight && z.imag() < currTop && z.imag() > currBottom && i != 0) {

                    x = (z.real() - currLeft) / inc;
                    y = (z.imag() - currBottom) / yinc;
                    hits[x][y]++;

                    if (hits[x][y] > max[x][y]) {
                        max[x][y] = hits[x][y];
                        Buf[x][y] = z;
                        icnt++;
                    }

                    if ((icnt / MAXISIZE) > 0) {
                        icnt = 0;
                        if (fcnt > 0) {
                            fclose(fh);
                            remove(filename);

                            fh = fopen(filename, (char *) &"ab");
                        }

                        // write to n-th output files
                        for (int ii = 0; ii < wi; ii++) {
                            for (int jj = 0; jj < hi; jj++) {
                                if (max[ii][jj] > 0)
                                    fprintf(fh, "%d %lf %lf\n", max[ii][jj], (float) Buf[ii][jj].real(), (float) Buf[ii][jj].imag());
                            }
                        }
                        fcnt++;
                    }
                }
                i++;
            }
        }
    }
    fprintf(fh, "%c %d \n", 'e', fcnt); // indicates end of all outputs to java
    fclose(fh);
}

#define USEC_TO_SEC 1.0e-6

double wallTime() {
    double seconds;
    struct timeval tv;

    gettimeofday(&tv, NULL);

    seconds = (double) tv.tv_sec; // seconds since Jan. 1, 1970
    seconds += (double) tv.tv_usec * USEC_TO_SEC; // and microseconds
    return seconds;
}

int main(int argc, char* argv[]) {
    printf("Calculating. Please wait....\n");
    // tracking  wall time
    double startwtime = 0.0;
    double endwtime;
    startwtime = wallTime();

    // call Mandelbrot routine
    Mandelbrot(WI, HI, ITER);

    // calculate wall time
    endwtime = wallTime();
    double total = (double) endwtime - startwtime;
    printf("Wall clock time = %lf seconds\n", total);
    return 0;
}
