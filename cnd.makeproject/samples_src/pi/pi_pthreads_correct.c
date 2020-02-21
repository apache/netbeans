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

