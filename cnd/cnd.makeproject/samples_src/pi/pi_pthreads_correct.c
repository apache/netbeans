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

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

#define THREADS 2


/*
 * 
 */

#define num_steps 2000000
double pi = 0;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;


void *work(void *arg)
{
  int start;
  int end;
  int i;
    
    start = (num_steps/THREADS) * ((int )arg) ;
    end = start + num_steps/THREADS;

    for (i = start; i < end; i++) {
        pthread_mutex_lock(&mutex);
        pi += 1.0/(i*4.0 + 1.0);
        pi -= 1.0/(i*4.0 + 3.0);
        pthread_mutex_unlock(&mutex);

    }

    return NULL;
}

int
main(int argc, char** argv) {
    
    
    int i;
    pthread_t tids[THREADS-1];
    
    for (i = 0; i < THREADS - 1 ; i++) {
         pthread_create(&tids[i], NULL, work, (void *)i);
    }

    i = THREADS-1;
    work((void *)i);

    for (i = 0; i < THREADS - 1 ; i++) {
        pthread_join(tids[i], NULL);

    }
    
    pi = pi * 4.0;
    printf("pi done - %f \n", pi);    
    
    return (EXIT_SUCCESS);
}

