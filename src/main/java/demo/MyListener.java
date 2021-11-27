package demo;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import set.hyrts.coverage.junit.FTracerJUnitUtils;
import set.hyrts.utils.Properties;

import java.util.Date;

import static demo.RunDemo.instrumentAll;

//自定义Listener类，此处的JUnit用例为单一测试类，所以与Suite相关的测试事件不需要覆写
public class MyListener extends RunListener
{
    private long startTime;
    private long endTime;
    private Class currentTestClass =null;

    @Override
    public void testRunStarted(Description description) throws Exception
    {
        RunDemo.init();
        Properties.NEW_DIR = "./diff_old";
        RunDemo.writeBack=false;
        instrumentAll();
        startTime = new Date().getTime();
        System.out.println("Test Run Started!");
        System.out.println("The Test Class is " + description.getClassName() + ". Number of Test Case is " + description.testCount());
        System.out.println("===================================================================================");
    }

    @Override
    public void testRunFinished(Result result) throws Exception
    {
        endTime = new Date().getTime();
        System.out.println("Test Run Finished!");
        System.out.println("Number of Test Case Executed is " + result.getRunCount());
        System.out.println("Elipsed Time of this Test Run is " + (endTime - startTime) / 1000);
        System.out.println("===================================================================================");
        if(currentTestClass !=null){
            System.out.println("record"+currentTestClass.getName());
            FTracerJUnitUtils.dumpCoverage(currentTestClass);
        }
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        System.out.println("Test Method Named " + description.getMethodName() + " Started!");
        if(currentTestClass !=null && currentTestClass !=description.getClass()){
            System.out.println("record"+currentTestClass.getName());
            FTracerJUnitUtils.dumpCoverage(currentTestClass);
        }
        currentTestClass=description.getTestClass();
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        System.out.println("Test Method Named " + description.getMethodName() + " Ended!");
        System.out.println("===================================================================================");
    }

    @Override
    public void testFailure(Failure failure) throws Exception
    {

        System.out.println("Test Method Named " + failure.getDescription().getMethodName() + " Failed!");
        System.out.println("Failure Cause is : " + failure.getException());
    }

    @Override
    public void testAssumptionFailure(Failure failure)
    {
        System.out.println("Test Method Named " + failure.getDescription().getMethodName() + " Failed for Assumption!");
    }

    @Override
    public void testIgnored(Description description) throws Exception
    {
        super.testIgnored(description);
    }
}
