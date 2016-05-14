/**
*Created by Miguel Guevara and Ekaterina Gumnova
*02/25/16
*/

#include <semaphore.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

sem_t ready_to_ask;
pthread_mutex_t mymutex, reporter_count;
pthread_cond_t cond, capacity_cond;
int question_asked;
int reporter_id, capacity, reporters_in, number_of_reporters;

void EnterConferenceRoom(int reporter_id){
pthread_mutex_lock(&mymutex);
while (reporters_in >= capacity){
pthread_cond_wait(&capacity_cond, &mymutex);
}
reporters_in++;
number_of_reporters--;
printf("Reporter %d enters the conference room\n", reporter_id);
pthread_mutex_unlock(&mymutex);
}
void QuestionStart(){
printf("Reporter %d asks a question\n", reporter_id);
}
void QuestionDone(){
printf("Reporter %d is satisfied\n", reporter_id);
}
void LeaveConferenceRoom(int reporter_id){
pthread_mutex_lock(&mymutex);
reporters_in--;
printf("Reporter %d leaves the conference room\n", reporter_id);
pthread_cond_signal(&capacity_cond);
pthread_mutex_unlock(&mymutex);
}

void AnswerStart(){
printf("Speaker starts to answer question for reporter %d\n", reporter_id);
}
void AnswerDone(){
printf("Speaker is done with answer for reporter %d\n", reporter_id);
}

void *Speaker (){
while (reporters_in > 0){
pthread_mutex_lock(&mymutex);
while (question_asked == 0){
pthread_cond_wait(&cond,&mymutex);
}
AnswerStart();
AnswerDone();
question_asked = 0;
pthread_mutex_unlock(&mymutex);
usleep(10);
}
pthread_exit(0);
}

void *Reporter (void *id){
	EnterConferenceRoom((int)id);
	usleep(rand()%10);
	int questions_to_ask;
	questions_to_ask = ((int)id % 4) + 2;
		while (questions_to_ask){
		sem_wait(&ready_to_ask);
		if (question_asked == 0){
		reporter_id = (int)id;
		pthread_mutex_lock(&mymutex);
		question_asked = 1;
		QuestionStart();
		pthread_cond_signal(&cond);
		pthread_mutex_unlock(&mymutex);
		usleep(rand()%10);
		QuestionDone();
		questions_to_ask--;
		if (questions_to_ask == 0)
		LeaveConferenceRoom((int)id);
		}
		sem_post(&ready_to_ask);
		usleep(10);
		}
	
		pthread_exit(0);
}
int main (int argc, char *argv[]){
	if (argc !=3){
		printf("Not enough arguments\n");
		return -1;
	}
	
	if ( (atoi(argv[1]) == 0) || (atoi(argv[2]) == 0) ){
		printf("Not a valid input\n");
		return -1;
	}
	
	number_of_reporters = atoi(argv[1]);
	capacity = atoi(argv[2]);
	int i;
	question_asked = 0;
	reporters_in = 0;
	pthread_mutex_init(&mymutex, NULL);
	pthread_mutex_init(&reporter_count, NULL);
	pthread_cond_init(&cond, NULL);
	pthread_cond_init(&capacity_cond, NULL);
	
	pthread_t threads[number_of_reporters+1];
	if (sem_init(&ready_to_ask, 0, 1)){
		printf("Could not initialize a semaphore\n");
		return -1;
	}
	//creating a thread for the speaker
	if (pthread_create(&threads[0], NULL, &Speaker, NULL)){
	printf("Could not create speaker thread\n");
	return -1;
	}
	for (i = 0; i < number_of_reporters; i++){
	if (pthread_create(&threads[i+1], NULL, &Reporter, (void*)(i+1))){
		printf("Could not create thread %d\n", i+1);
		return -1;
	}
	printf("Thread %d created\n", i+1);
	}
	for (i = 0; i < number_of_reporters+1; i++){
	
	if (pthread_join(threads[i], NULL)){
		printf("Could not join thread %d\n", i);
		return -1;
		}
	}

	sem_destroy(&ready_to_ask);
	pthread_mutex_destroy(&mymutex);
	pthread_mutex_destroy(&reporter_count);
	pthread_cond_destroy(&cond);
	pthread_cond_destroy(&capacity_cond);
	pthread_exit(NULL);
}




