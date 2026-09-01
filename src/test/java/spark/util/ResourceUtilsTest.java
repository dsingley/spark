package spark.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import spark.utils.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ResourceUtilsTest {

    @Test
    public void testGetFile_whenURLProtocolIsNotFile_thenThrowFileNotFoundException() throws MalformedURLException {
        URL url = new URL("http://example.com/");

        assertThatThrownBy(() -> ResourceUtils.getFile(url, "My File Path"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("My File Path cannot be resolved to absolute file path " +
                                     "because it does not reside in the file system: http://example.com/");
    }

    @Test
    public void testGetFile_whenURLProtocolIsFile_thenReturnFileObject() throws
                                                                         MalformedURLException,
                                                                         FileNotFoundException,
                                                                         URISyntaxException {
        //given
        URL url = new URL("file://public/file.txt");
        File file = ResourceUtils.getFile(url, "Some description");

        //then
        assertThat(new File(ResourceUtils.toURI(url).getSchemeSpecificPart())).isEqualTo(file);
    }

}
