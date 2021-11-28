package demo;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import set.hyrts.coverage.junit.FTracerJUnitUtils;
import set.hyrts.utils.Properties;

import java.util.Date;

import static demo.RunDemo.instrumentAll;

//自定义Listener类，此处的JUnit用例为单一测试类，所以与Suite相关的测试事件不需要覆写
public class MyListener extends RunListener {
    private Class currentTestClass = null;

    @Override
    public void testRunStarted(Description description) throws Exception {
        RunDemo.init();
        Properties.NEW_DIR = "./diff_old";
        RunDemo.writeBack = false;
        //为了初始化Tracer
        instrumentAll();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        if (currentTestClass != null) {
            System.out.println("record" + currentTestClass.getName());
            FTracerJUnitUtils.dumpCoverage(currentTestClass);
        }
    }

    @Override
    public void testStarted(Description description) throws Exception {
        //每个测试类方法开始时都会被调用，为了以测试类为单位记录测试依赖，所以引入了一个currentTestClass变量
        if (currentTestClass != null && currentTestClass != description.getClass()) {
            System.out.println("record" + currentTestClass.getName());
            FTracerJUnitUtils.dumpCoverage(currentTestClass);
        }
        currentTestClass = description.getTestClass();
    }

}
