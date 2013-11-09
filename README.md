While you are code for the program challenges, you are expected to write the program which reads from the System.in and System.out. 
This makes it difficult to test the program for lot of input combinations. This is a very important step to get your program validated
against an online judge. More the program validated with test data, more chances of getting accepted from the judge. This is what my inital
stab at writing a framework which would allow me to write code which could be tested against.

Following are some of the fixtures which could be used at different scenarios
1. com.furry.robot.test.FileValidatingTestFixture helps to test if the test data is stored as files. This can read those test files and pass
the data to the program and validate the output from the program with the output file