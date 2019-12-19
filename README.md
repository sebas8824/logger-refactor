# JobLogger refactor

This is a part of the given test that refactor the code and adds unit tests for the main class. 
This was made using maven, java8 and other dependencies.

# Code review and alternative solution to the requirement:
  - Originally the methods were static, which means that in order to use the JobLogger, it does not need to be initialized. Main reason why the JobLogger constructor was removed.
  - The JobLogger class was decomposed in certain functions and data in order to organize better the solution without violating the Single Responsibility Principle.
  - The former JobLogger class had several parameters which made the signature of the methods longer, main reason why there was created a model package. This package uses a Log class that summarizes the contents of a log entry, which allows the entry to be marked as a message, or an error or a warning, plus its message content.
  - The SetUpDBConnection class was created in order to detach the Database connection inside the former JobLogger.
  - Also the class PropertyLoader is a helper class that obtains the properties from a file name, without having to repeat the same operation over and over.
  - A constant package includes all the reused constants and the enums to point out each possible state for the logging.

There were errors in the logic versus the requirement said in the document as follows:
  - The if statements were too redundant and did not validate the preference of logging errors or errors and warnings.
  - The messageText treatment in the original definition was unnecessary inside the class and the validations could have been shortened by using the .isEmpty() String method. This was one of the reasons why the Log class was created, this included the .trim() method.
  - The database logging part was persisting data that did not said anything useful because it was originally storing a boolean value and a number represented by a String. This was modified in order to set up as primary key a said number where 1 was for logging errors and 2 was for logging errors and warnings, and each respective message.
  - Originally the String l, was for the logging of the message but at the last if statement, this was replaced and was not used. Now, that String was renamed to fullLogEntry and was defined by using a series of if-else statements, not individual if.
  - In order to make the unit tests and allow the class to manage an state, it was needed a variable to control each output of the events. This state variable is an enum that describes the situation of the JobLogger when no exception was thrown.
  - For the unit tests, the code coverage threshold using clover is superior than 80%
  - The logMessage method signature was modified in order to receive a log entry, the desired locations to register the logs in a Set and two booleans that makes the method know if this has to log an error and a warning according to the description given in the document.
  - In order to make the module more less dependant as happened when using the dbParams object for the configuration such as the database access or the logging routes, it was required to add two .properties files that include those properties.
