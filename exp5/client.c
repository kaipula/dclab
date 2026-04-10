#include "task_scheduler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void scheduler_prog_1(char *host, int id, char *cmd) {
    CLIENT *clnt;
    int *result;
    task_data task;

    clnt = clnt_create(host, SCHEDULER_PROG, SCHEDULER_VERS, "udp");

    if (clnt == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    task.task_id = id;
    strncpy(task.command, cmd, 255);
    task.command[255] = '\0';

    result = submit_task_1(&task, clnt);

    if (result == NULL) {
        clnt_perror(clnt, "call failed");
    } else {
        printf("Task returned status: %d\n", *result);
    }

    clnt_destroy(clnt);
}

int main(int argc, char *argv[]) {
    if (argc < 4) {
        printf("usage: %s server_host task_id command\n", argv[0]);
        exit(1);
    }

    scheduler_prog_1(argv[1], atoi(argv[2]), argv[3]);

    return 0;
}
