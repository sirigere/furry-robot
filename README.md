when coding for a programming challenge, it is expected to write a program which could read from the System.in and System.out. 
This makes it difficult to test the program for multiple input combinations (test data). however, this is very important step to get your 
program accepted against an online judge. More the program validated against the test data, more chances of getting accepted from the judge. 
This project tries to come up with a framework which could allow writing unit test code against such a program.

Following are some of the fixtures which could be used at different scenarios
1. com.furry.robot.test.FileValidatingTestFixture helps to test if the test data is stored as files. This can read those test files and pass
the data to the program and validate the output from the program with the output file