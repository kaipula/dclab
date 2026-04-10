#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#define WORK_TAG 1    // Tag for sending tasks
#define DIE_TAG 2     // Tag to tell workers to stop
void manager(int num_tasks, int num_procs) {
    int task_id = 0;
    int result;
    MPI_Status status;
    // 1. Initial distribution: Give every worker one task to start
    for (int i = 1; i < num_procs; i++) {
        if (task_id < num_tasks) {
            MPI_Send(&task_id, 1, MPI_INT, i, WORK_TAG, MPI_COMM_WORLD);
            task_id++;
        }
    }
    // 2. Dynamic distribution: As workers finish, give them new tasks
    while (task_id < num_tasks) {
        MPI_Recv(&result, 1, MPI_INT, MPI_ANY_SOURCE, WORK_TAG, MPI_COMM_WORLD, &status);
        int worker_id = status.MPI_SOURCE;
        printf("Manager: Received finished task from Worker %d. Sending Task %d.\n", worker_id, task_id);
        MPI_Send(&task_id, 1, MPI_INT, worker_id, WORK_TAG, MPI_COMM_WORLD);
        task_id++;
    }
    // 3. Termination: Tell all workers to stop
    for (int i = 1; i < num_procs; i++) {
        MPI_Recv(&result, 1, MPI_INT, MPI_ANY_SOURCE, WORK_TAG, MPI_COMM_WORLD, &status);
        MPI_Send(NULL, 0, MPI_INT, status.MPI_SOURCE, DIE_TAG, MPI_COMM_WORLD);
    }
    printf("Manager: All tasks completed. Shutting down.\n");
}
void worker(int rank) {
    int task_id;
    MPI_Status status;
    while (1) {
        // Wait for a message from the Manager
        MPI_Recv(&task_id, 1, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
        if (status.MPI_TAG == DIE_TAG) {
            printf("Worker %d: Received DIE signal. Exiting.\n", rank);
            break;
        }
        // Simulate work
        printf("Worker %d: Processing Task %d...\n", rank, task_id);
        sleep(1); // Task takes 1 second
        // Send a "Finished" signal back to Manager
        MPI_Send(&task_id, 1, MPI_INT, 0, WORK_TAG, MPI_COMM_WORLD);
    }
}
int main(int argc, char** argv) {
    int rank, size;
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    if (size < 2) {
        if (rank == 0) printf("This scheduler requires at least 2 processes.\n");
    } else {
        if (rank == 0) {
            manager(10, size); // Distribute 10 tasks
        } else {
            worker(rank);
        }
    }
    MPI_Finalize();
    return 0;
}
