/**
*Created by Miguel Guevara and Ekaterina Gumnova
*02/25/16
*/

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

int sharedVariable = 0;
pthread_mutex_t mymutex;
pthread_barrier_t barr;

void *SimpleThread(void *id){
	int num, val;
	int threadid = (int)id;
	//start of critical space
	#ifdef PTHREAD_SYNC
		pthread_mutex_lock(&mymutex);
	#endif
	for(num = 0 ; num < 20 ; num++){
		if(random() > RAND_MAX/2)
			usleep(10);
		val = sharedVariable;
		printf("***thread %d sees value %d\n",threadid,val);
		sharedVariable= val + 1;
	}
	
	pthread_mutex_unlock(&mymutex);
	//end critical space
	#ifdef PTHREAD_SYNC
	int rc  = pthread_barrier_wait(&barr);
	if (rc != 0 && rc != PTHREAD_BARRIER_SERIAL_THREAD){
		printf("Could not wait for barrier\n");
		exit(-1);
	}
	#endif
	val = sharedVariable;
	printf("Thread %d sees final value %d\n",threadid, val);
	pthread_exit(NULL);
}

int main(int argc, char *argv[]){
	//validate the input
	if (argc != 2) {
		printf("No arguments to show\n");
		return -1;
	}
	if (atoi(argv[1]) == 0) {
		printf("Not a valid input\n");
		return -1;
	}
	
	int number_of_threads = atoi(argv[1]);
	pthread_t threads[number_of_threads];
	void *status;
	int rc;
	long t;
	
	if(pthread_barrier_init(&barr, NULL, number_of_threads)){
		printf("Could not create a barrier\n");
		return -1;
	}
	pthread_mutex_init(&mymutex, NULL);
	//create the thread to perform worlk

	for(t = 0 ; t < number_of_threads ; t++){
		printf("IN MAIN: creating thread %ld '%s'\n", t, *argv);
		rc = pthread_create(&threads[t], NULL, &SimpleThread,(void*)t);
		if(rc){
			printf("ERROR: return code from pthread_create() is %d\n", rc);
			exit(-1);
		}
	}
	printf("%d\n", syscall(342));
	//wait for the threads to come back
	for(t = 0 ; t < number_of_threads ; t++){
		pthread_join(threads[t], &status);
	}
	pthread_mutex_destroy(&mymutex);
	pthread_exit(NULL);
}
