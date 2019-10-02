package it.pkg.advices;

import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.SoutOutput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class testAdviceTest{

    @Test
    public void testWebsphereInterceptor() throws NoSuchMethodException{
            //PowerMockito.mockStatic(<<classToIntercept>>.class);
            //WebApp webApp=mock(<<classToIntercept>>.class);

            EntryDefinition handleRequestEntry=new EntryDefinition(testAdvice.class);

            Method method=Whitebox.getMethod(WebApp.class,"<<interceptingMethod>>");

            //test before method
         //   testAdvice.before();

            //test after method
          //  testAdvice.after();
        }
}