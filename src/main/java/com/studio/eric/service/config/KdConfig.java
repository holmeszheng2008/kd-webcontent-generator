package com.studio.eric.service.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KdConfig {
    private String workingDirectory;
    private String bulletinFolder;
    private String bulletinIdFolder;
    private String bulletinPageFolder;
    private String bulletin_id_template;
    private String bulletin_item_template;
    private String bulletin_page_item_template;
    private String bulletin_page_template;
    private String bulletin_template;

    private static KdConfig instance = new KdConfig();

    private KdConfig() {
        init();
    }

    private void init() {
        workingDirectory = System.getProperty("workingDir");
        bulletinFolder = System.getProperty("outputDir");
        bulletinIdFolder = bulletinFolder + "/content";
        bulletinPageFolder = bulletinFolder + "/page";
        try (InputStream is1 = KdConfig.class.getClassLoader().getResourceAsStream("bulletin_id_template.txt");
                InputStream is2 = KdConfig.class.getClassLoader().getResourceAsStream("bulletin_item_template.txt");
                InputStream is3 = KdConfig.class.getClassLoader().getResourceAsStream("bulletin_page_item_template.txt");
                InputStream is4 = KdConfig.class.getClassLoader().getResourceAsStream("bulletin_page_template.txt");
                InputStream is5 = KdConfig.class.getClassLoader().getResourceAsStream("bulletin_template.txt");
        ) {
            bulletin_id_template = parseStreamAsString(is1);
            bulletin_item_template = parseStreamAsString(is2);
            bulletin_page_item_template = parseStreamAsString(is3);
            bulletin_page_template = parseStreamAsString(is4);
            bulletin_template = parseStreamAsString(is5);
        } catch (Exception e) {
            throw new RuntimeException("Template consumption failed");
        }
    }
    
    private String parseStreamAsString(InputStream is) {
        String result = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));

        return result;
    }

    public static KdConfig getInstance() {
        return instance;
    }

    public static class PlaceHolderNames {
        public static String PROJECT_NAME = "%project_name%";
        public static String BUILD_OFFICE = "%build_office%";
        public static String OBSERVATION_PERIOD = "%observation_period%";
        public static String OBSERVATION_OFFICE = "%observation_office%";
        public static String OBSERVATION_STAFF = "%observation_staff%";
        public static String FILING_DATE = "%filing_date%";


        public static String BULLETIN_PAGE = "%bulletin_page%";
        public static String BULLETIN_ID = "%bulletin_id%";
        public static String BULLETIN_ID_FIRST = "%bulletin_id_first%";
        public static String BULLETIN_ID_PRE = "%bulletin_id_pre%";
        public static String BULLETIN_ID_NEXT = "%bulletin_id_next%";
        public static String BULLETIN_ID_LAST = "%bulletin_id_last%";

        public static String BULLETIN_PAGE_ITEMS = "%bulletin_page_items%";


        public static String BULLETIN_ITEMS = "%bulletin_items%";
        public static String BULLETIN_PAGE_PARSED = "%bulletin_page_parsed%";
        public static String BULLETIN_PAGE_SEASON_PARSED = "%bulletin_page_season_parsed%";

    }
}
