/**
 * Small sample file used to test the Java Code Quality Checker.
 */
public class TestSample {

    /** Demonstrates some code-quality warnings. */
    public void badMethod() {
        int count = 5;
        String message = "test";
        System.out.println(count);
        System.out.println(message);

        boolean isReady = true;
        if (isReady == true) {
            System.out.println("This line demonstrates a redundant boolean comparison.");
        }
    }

    /** A longer method for testing method-length detection. */
    public void longerMethod() {
        int firstValue = 1;
        int secondValue = 2;
        int thirdValue = 3;
        int fourthValue = 4;
        int fifthValue = 5;
        int sixthValue = 6;
        int seventhValue = 7;
        int eighthValue = 8;
        int ninthValue = 9;
        int tenthValue = 10;
        int eleventhValue = 11;
        int twelfthValue = 12;
        int thirteenthValue = 13;
        int fourteenthValue = 14;
        int fifteenthValue = 15;
        int sixteenthValue = 16;
        int seventeenthValue = 17;
        int eighteenthValue = 18;
        int nineteenthValue = 19;
        int twentiethValue = 20;
        int twentyFirstValue = 21;
        int twentySecondValue = 22;
        int twentyThirdValue = 23;
        int twentyFourthValue = 24;
        int twentyFifthValue = 25;
        int twentySixthValue = 26;
        int twentySeventhValue = 27;
        int twentyEighthValue = 28;
        int twentyNinthValue = 29;
        int thirtiethValue = 30;
        int thirtyFirstValue = 31;
        int thirtySecondValue = 32;
        System.out.println("Finished long method.");
    }
}
