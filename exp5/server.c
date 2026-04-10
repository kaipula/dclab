#include "task_scheduler.h"
#include <stdio.h>
#include <stdlib.h>

int *submit_task_1_svc(task_data *argp, struct svc_req *rqstp) {
    static int result;

    printf("Server received Task ID: %d\n", argp->task_id);
    printf("Executing command: %s\n", argp->command);

    // Simulate execution
    result = system(argp->command);

    return &result;
}

