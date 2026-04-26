struct task_data {
    int task_id;
    char command[256];
};

program SCHEDULER_PROG {
    version SCHEDULER_VERS {
        int SUBMIT_TASK(task_data) = 1;
    } = 1;
} = 0x20000001;
