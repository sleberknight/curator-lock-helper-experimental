package org.kiwiproject.curator.example;

public class AllExamples {

    public static void main(String[] args) {
        UseLockSuccessfulExample.main(args);
        UseLockTimeOutExample.main(args);
        UseLockErroneousLockExample.main(args);
        UseLockActionFailureExample.main(args);

        WithLockSuccessfulExample.main(args);
        WithLockTimeOutExample.main(args);
        WithLockErroneousLockExample.main(args);
        WithLockActionFailureExample.main(args);
    }
}
