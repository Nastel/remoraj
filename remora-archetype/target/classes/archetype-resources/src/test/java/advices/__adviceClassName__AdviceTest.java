#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.advices;

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
public class ${adviceClassName}AdviceTest{

    @Test
    public void test${adviceClassName}Interceptor() throws NoSuchMethodException{
            //PowerMockito.mockStatic(<<classToIntercept>>.class);
            //WebApp webApp=mock(<<classToIntercept>>.class);

            EntryDefinition handleRequestEntry=new EntryDefinition(${adviceClassName}Advice.class);

            Method method=Whitebox.getMethod(Object.class,"<<interceptingMethod>>");

            //test before method
         //   ${adviceClassName}Advice.before();

            //test after method
          //  ${adviceClassName}Advice.after();
        }
}