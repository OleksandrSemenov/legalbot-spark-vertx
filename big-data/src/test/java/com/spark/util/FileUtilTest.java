package com.spark.util;

import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Taras Zubrei
 */
public class FileUtilTest {
    private final List<String> paths = new ArrayList<>();

    @Test
    public void unzipDownloadedFile() {
        String path = "/tmp/legalbot/archives/ufop";
        String extractPath = "/tmp/legalbot/archives/ufop/20180508";
        final String url = "https://nais.gov.ua/files/general/2018/05/08/20180508160018-10.zip";

        final String zipFile = FileUtil.downloadFile(url, path);
        paths.add(zipFile);
        assertThat(Files.isRegularFile(Paths.get(zipFile)), is(true));
        FileUtil.unzip(zipFile);

        assertThat(Files.isDirectory(Paths.get(extractPath)), is(true));
        assertThat(Files.isRegularFile(Paths.get(extractPath, "15.1-EX_XML_EDR_UO.xml")), is(true));
        assertThat(Files.isRegularFile(Paths.get(extractPath, "15.2-EX_XML_EDR_FOP.xml")), is(true));
        Stream.of(
                Paths.get(extractPath, "15.1-EX_XML_EDR_UO.xml"),
                Paths.get(extractPath, "15.2-EX_XML_EDR_FOP.xml")
        ).map(Path::toString).forEach(paths::add);
    }

    @After
    public void cleanUp() throws Exception {
        for (String path : paths)
            Files.deleteIfExists(Paths.get(path));
        paths.clear();
    }
}
