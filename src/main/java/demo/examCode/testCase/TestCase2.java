package demo.examCode.testCase;

import demo.examCode.src.son;
import demo.examCode.test;

public class TestCase2 implements test {
    @Override
    public void runTest() {
        son s=new son();
        s.sayHi();
    }
}
