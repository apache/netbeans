/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.debugger.jpda.testapps;

import java.util.Random;

/**
 * An application for test of concurrent debugging.
 * <p>
 * Creates {@link ConcurrencyApp#NUM_THREADS} concurrent threads and a 3D
 * mesh of {@link ConcurrencyApp#GRID_LENGTH} size for each thread.
 * Threads operate on their local data and exchange results under a lock.
 *
 * @author Martin
 */
public class ConcurrencyApp extends Thread {

    private static final int NUM_THREADS = 4;
    private static final int GRID_LENGTH = 100;

    private double[][][] localData;
    private boolean dataReady = false;
    private int appNum;
    private ConcurrencyApp[] allApps;

    public ConcurrencyApp(int appNum, double[][][] localData, ConcurrencyApp[] allApps) {
        this.appNum = appNum;
        this.localData = localData;
        this.allApps = allApps;
        setName("Concurrency thread "+appNum);
    }

    @Override
    public void run() {
        // 0th breakpoint, which suspends every started thread at the beginning.
        double md = maxDiagonal();                               // LBREAKPOINT
        synchronized (this) {
            for (int i = 0; i < localData.length; i++) {
                // 1st conditional breakpoint
                localData[i][i][i] *= (md/localData.length);     // LBREAKPOINT
            }
        }
        for (int a = 0; a < allApps.length; a++) {
            if (a == appNum) continue;
            md = allApps[a].maxDiagonal();
            synchronized (this) {
                for (int i = 0; i < localData.length; i++) {
                    localData[i][i][i] *= (md/localData.length);
                }
            }
        }
        synchronized (this) {
            dataReady = true;
            notify();
        }
    }

    private synchronized double sumDiagonal(boolean b1, boolean b2, boolean b3) {
        double d = 0;
        // 2nd monitor test breakpoint
        int n = localData.length;                   // LBREAKPOINT
        for (int i = 0; i < localData.length; i++) {
            d += localData[b1 ? i : n - i - 1]
                          [b2 ? i : n - i - 1]
                          [b3 ? i : n - i - 1];
        }
        return d;
    }

    private synchronized double maxDiagonal() {
        return max(sumDiagonal(true, true, true),
                   sumDiagonal(true, true, false),
                   sumDiagonal(true, false, true),
                   sumDiagonal(true, false, false),
                   sumDiagonal(false, true, true),
                   sumDiagonal(false, true, false),
                   sumDiagonal(false, false, true),
                   sumDiagonal(false, false, false)
                   );
    }

    public synchronized double getResult() {
        if (!dataReady) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        double sum = 0;
        for (int i = 0; i < localData.length; i++) {
            for (int j = 0; j < localData[i].length; j++) {
                for (int k = 0; k < localData[i][j].length; k++) {
                    sum += localData[i][j][k];
                }
            }
        }
        return sum;
    }


    private static double max(double d1, double... d) {
        double m = d1;
        int i = 0;
        int j = d.length - 1;
        while (i <= j) {
            m = Math.max(m, Math.max(d[i], d[j]));
            i++;
            j--;
        }
        return m;
    }

    public static void main(String[] args) {
        SharedData data = new SharedData(NUM_THREADS);
        ConcurrencyApp[] apps = new ConcurrencyApp[NUM_THREADS];
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < NUM_THREADS; i++) {
                apps[i] = new ConcurrencyApp(i, data.createLocalData(i), apps);
            }
            for (int i = 0; i < NUM_THREADS; i++) {
                apps[i].start();
            }
            double sum = 0;
            for (int i = 0; i < NUM_THREADS; i++) {
                sum += apps[i].getResult();
            }
            data.correctData(sum);
        }
    }

    private static class SharedData {

        private double[][][][] data = new double[NUM_THREADS][GRID_LENGTH][GRID_LENGTH][GRID_LENGTH];

        public SharedData(int numThreads) {
            Random r = new Random(1000l);
            for (int t = 0; t < data.length; t++) {
                for (int i = 0; i < data[t].length; i++) {
                    for (int j = 0; j < data[t][i].length; j++) {
                        for (int k = 0; k < data[t][i][j].length; k++) {
                            data[t][i][j][k] = r.nextDouble();
                        }
                    }
                }
            }
        }

        public double[][][] createLocalData(int l) {
            return data[l];
        }

        public synchronized void correctData(double a) {
            for (int t = 0; t < data.length; t++) {
                for (int i = 0; i < data[t].length; i++) {
                    for (int j = 0; j < data[t][i].length; j++) {
                        for (int k = 0; k < data[t][i][j].length; k++) {
                            data[t][i][j][k] /= a;
                        }
                    }
                }
            }
        }
    }

}
