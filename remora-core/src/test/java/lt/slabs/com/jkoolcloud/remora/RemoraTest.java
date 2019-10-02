package lt.slabs.com.jkoolcloud.remora;

import com.google.common.io.Files;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.core.output.SoutOutput;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * This class intended to test loading premain and it's options.
 */
public class RemoraTest {
    public static void main(String[] args) throws Throwable {
        System.setProperty("probe.output", SoutOutput.class.getName());
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println(
                "\n\tClass.loader=" + Remora.class.getClassLoader()
                        + "\n\tJava.version=" + System.getProperty("java.version")
                        + "\n\tJava.vendor=" + System.getProperty("java.vendor")
                        + "\n\tJava.home=" + System.getProperty("java.home")
                        + "\n\tJava.heap=" + Runtime.getRuntime().maxMemory()
                        + "\n\tOS.name=" + System.getProperty("os.name")
                        + "\n\tOS.version=" + System.getProperty("os.version")
                        + "\n\tOS.arch=" + System.getProperty("os.arch")
                        + "\n\tOS.cpus=" + Runtime.getRuntime().availableProcessors());

        new JustATest().instrumentedMethod("http://www.google.com", "Argument");

        Thread.sleep(3000);
    }


    @Test
    public void findJarsTest() throws IOException {
        File tempDir = Files.createTempDir();
        java.nio.file.Files.createTempFile(Paths.get(tempDir.getAbsolutePath()), "temp1", ".jar");
        java.nio.file.Files.createTempFile(Paths.get(tempDir.getAbsolutePath()), "temp2", ".jar");

        URL[] jars = Remora.findJars(tempDir.getAbsolutePath());
        assertEquals(2, jars.length);

        org.apache.commons.io.FileUtils.deleteDirectory(tempDir);

    }

}
