///////////////////////////////////////////////////////////////////////////////
// File:       Assert.java
// Type:       java application file
// Author(s):  Dale Skrien
// Project:    CPU Sim 3.0
// Date:       September, 1999
//
// Description:
//
//   The Assert.FailureException class is a RuntimeException that is thrown by
//   the Assert class' That() method.
//
//   The Assert class contains a static method for checking assertions and,
//   if it fails, causes a runtime error to occur.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.util;


///////////////////////////////////////////////////////////////////////////////
// Assert class
//
// This class is used for debugging your code.  The Assert method
// checks the truth of assertions and, if they fail, throws an
// Assert.FailureException.

public class Assert
{
    ///////////////////////////////////////////////////////////////////////////////
    // AssertionFailureException class
    //
    // This class is used for debugging your code.  You include
    // assertions as to the state of the code and the assertion throws
    // a FailureException if your code is not in that
    // state.
    public static class AssertionFailureException
            extends RuntimeException
    {
    }


    // constructor
    // it is private so that no instances of this class will be created
    private Assert()
    {
    }

    //   if the expr is false, That throws an Assert.FailureException and
    //   prints the message to System.out.
    public static void That(boolean expr, String message)
    {
        if (!expr) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            System.out.println(message);
            throw new AssertionFailureException();
        }
    }

}  //end class Assert

