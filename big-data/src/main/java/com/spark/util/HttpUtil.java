package com.spark.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class HttpUtil {
    private static final String UFOP_URL = "https://nais.gov.ua/m/ediniy-derjavniy-reestr-yuridichnih-osib-fizichnih-osib-pidpriemtsiv-ta-gromadskih-formuvan";

    public static ArchiveUrl getUFOPDownloadUrl() {
        try {
            final Document document = Jsoup.connect(UFOP_URL).get();
            final String relativeUrl = document.getElementsByAttributeValueContaining("href", "files/general").get(0).attr("href");
            final String downloadUrl = new URL(new URL(UFOP_URL), relativeUrl).toString().replaceAll("\\.\\./", "");
            final String date = document.getElementsContainingText("дату актуальності даних у наборі даних")
                    .last()
                    .children()
                    .stream()
                    .map(Element::text)
                    .collect(Collectors.joining(""));
            return new ArchiveUrl(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy")), downloadUrl);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to connect to page: " + UFOP_URL, ex);

        }
    }

    public static class ArchiveUrl {
        private final LocalDate date;
        private final String url;

        public ArchiveUrl(LocalDate date, String url) {
            this.date = date;
            this.url = url;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getUrl() {
            return url;
        }
    }
}
