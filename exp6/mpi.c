#include <mpi.h>
#include <stdio.h>

int main(int argc, char *argv[]) {
    int rank, size, number;

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);   // Process ID
    MPI_Comm_size(MPI_COMM_WORLD, &size);   // Total processes

    if (rank == 0) {
        number = 100;

        printf("Manager Process %d sending data to all workers\n", rank);

        for (int i = 1; i < size; i++) {
            MPI_Send(&number, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
        }
    }
    else {
        MPI_Recv(&number, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

        printf("Worker Process %d received number %d\n", rank, number);
    }

    MPI_Finalize();
    return 0;
}