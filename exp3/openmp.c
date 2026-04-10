#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <unistd.h>

typedef struct {
    int id;
    int duration;
} Task;

void execute_task(Task t) {
    int thread_id = omp_get_thread_num();
    printf("[Thread %d] Starting Task %d (takes %ds)...\n", thread_id, t.id, t.duration);
    sleep(t.duration);
    printf("[Thread %d] Completed Task %d\n", thread_id, t.id);
}

int main() {
    int num_tasks = 10;
    Task tasks[num_tasks];

    for (int i = 0; i < num_tasks; i++) {
        tasks[i].id = i + 1;
        tasks[i].duration = (rand() % 3) + 1;
    }

    printf("Starting Scheduler with %d threads...\n", omp_get_max_threads());

    #pragma omp parallel
    {
        #pragma omp single
        {
            for (int i = 0; i < num_tasks; i++) {
                #pragma omp task firstprivate(i)
                {
                    execute_task(tasks[i]);
                }
            }
        }
    }

    printf("All tasks completed.\n");
    return 0;
}
