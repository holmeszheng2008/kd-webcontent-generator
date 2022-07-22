package com.studio.eric.service.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.eric.service.config.KdConfig;
import com.studio.eric.service.model.Description;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Generator {
    private final KdConfig kdConfig;
    private final StaticContentConsumer staticContentConsumer;
    private final ObjectMapper objectMapper;

    public Generator(StaticContentConsumer staticContentConsumer) {
        kdConfig = KdConfig.getInstance();
        this.staticContentConsumer = staticContentConsumer;
        staticContentConsumer.consume();
        objectMapper = new ObjectMapper();
    }

    public void generate() {
        cleanup();
        Map<String, List<File>> postingDateToBulletinIdDirMap = staticContentConsumer.getPostingDateToBulletinIdDirMap();

        for (Entry<String, List<File>> entry : postingDateToBulletinIdDirMap.entrySet()) {
            String postingDate = entry.getKey();
            List<File> bulletinIdFileArray = entry.getValue();
            if (bulletinIdFileArray.isEmpty()) {
                continue;
            }
            String bulletinPageItems = "";
            String bulletinIdFirst = bulletinIdFileArray.get(0).getName();
            String bulletinIdLast = bulletinIdFileArray.get(bulletinIdFileArray.size() - 1).getName();
            log.info("=============================Start generating bulletinId htmls on posting date {}=============================", postingDate);
            for (int i = 0; i < bulletinIdFileArray.size(); i++) {
                File bulletinIdFile = bulletinIdFileArray.get(i);

                String bulletinId = bulletinIdFile.getName();
                String bulletinIdPre = null;
                String bulletinIdNext = null;

                if (i == 0) {
                    bulletinIdPre = bulletinIdFirst;
                } else {
                    bulletinIdPre = bulletinIdFileArray.get(i - 1).getName();
                }

                if (i == bulletinIdFileArray.size() - 1) {
                    bulletinIdNext = bulletinIdLast;
                } else {
                    bulletinIdNext = bulletinIdFileArray.get(i + 1).getName();;
                }

                String descriptionFileStr = bulletinIdFile.getAbsolutePath() + "\\description.txt";
                Description description = null;
                try {
                    description = objectMapper.readValue(new File(descriptionFileStr), Description.class);
                } catch (Exception e) {
                    log.error("Description file {} not found, skip this report...", descriptionFileStr);
                }
                createBulletinIdFile(postingDate, description, bulletinId, bulletinIdFirst, bulletinIdPre, bulletinIdNext, bulletinIdLast);
                bulletinPageItems = bulletinPageItems + fillBulletinPageItem(description, bulletinId) + '\n';
            }
            log.info("=============================Finish generating bulletinId htmls on posting date {}=============================", postingDate);

            log.info("=============================Start generating bulletin page htmls on posting date {}=============================", postingDate);
            createBulletinPageFile(postingDate, bulletinPageItems);
            log.info("=============================Finish generating bulletin page htmls on posting date {}=============================", postingDate);
        }
        
        log.info("=============================Start generating bulletin html =============================");
        createBulletinFile(postingDateToBulletinIdDirMap.keySet());
        log.info("=============================Finish generating bulletin html =============================");
    }

    private String fillBulletinPageItem(Description description, String bulletinId) {
        return kdConfig.getBulletin_page_item_template().replace(KdConfig.PlaceHolderNames.PROJECT_NAME, description.getProject_name())
                .replace(KdConfig.PlaceHolderNames.BUILD_OFFICE, description.getBuild_office())
                .replace(KdConfig.PlaceHolderNames.OBSERVATION_PERIOD, description.getObservation_period())
                .replace(KdConfig.PlaceHolderNames.BULLETIN_ID, bulletinId);
        
    }

    private void createBulletinFile(Set<String> keySet) {
        String bulletinFileStr = kdConfig.getBulletinFolder() + "/bulletin.html";
        File bulletinFile = new File(bulletinFileStr);
        String bulletin_items = "";
        for (String postingDate : keySet) {
            String bulletin_item = kdConfig.getBulletin_item_template().replace(KdConfig.PlaceHolderNames.BULLETIN_PAGE, postingDate)
                    .replace(KdConfig.PlaceHolderNames.BULLETIN_PAGE_PARSED, parsePostingDateToSeason(postingDate));
            bulletin_items = bulletin_items + bulletin_item + '\n';
        }

        String content = kdConfig.getBulletin_template().replace(KdConfig.PlaceHolderNames.BULLETIN_ITEMS, bulletin_items);
        try (Writer fw = new BufferedWriter(new FileWriter(bulletinFile))) {
            log.info("Writing to bulletin file {} ", bulletinFile);
            fw.write(content);
            log.info("Finish Writing to bulletin file {} ", bulletinFile);
        } catch (IOException e) {
            log.error("{} can't be created", bulletinFile);
        }
    }

    private String parsePostingDate(String original) {
        if (original.length() != 8) {
            return original;
        }
        return original.substring(0, 4) + "年" + original.substring(4, 6) + "月" + original.substring(6, 8) + "日";
    }

    private String parsePostingDateToSeason(String original) {
        if (original.length() != 8) {
            return original;
        }
        int year = Integer.valueOf(original.substring(0, 4));
        int month = Integer.valueOf(original.substring(4, 6));
        int season = 0;
        if (month / 3 == 0) {
            year--;
            season = 4;
        } else {
            season = month / 3;
        }

        return year + "年" + "第" + season + "季度";
    }

    private void createBulletinPageFile(String postingDate, String bulletinPageItems) {
        String bulletinPageFolderStr = kdConfig.getBulletinPageFolder();
        String bulletinPageFileStr = bulletinPageFolderStr + "/" + postingDate + ".html";
        File bulletinPageFile = new File(bulletinPageFileStr);
        String content = kdConfig.getBulletin_page_template().replace(KdConfig.PlaceHolderNames.BULLETIN_PAGE_ITEMS, bulletinPageItems);
        try (Writer fw = new BufferedWriter(new FileWriter(bulletinPageFile))) {
            log.info("Writing to bulletinId file {} ", bulletinPageFile);
            fw.write(content);
            log.info("Finish Writing to bulletinId file {} ", bulletinPageFile);
        } catch (IOException e) {
            log.error("{} can't be created", bulletinPageFile);
        }
    }

    private void createBulletinIdFile(String postingDate, Description description, String bulletinId, String bulletinIdFirst, String bulletinIdPre, String bulletinIdNext, String bulletinIdLast) {
        String bulletinIdFileStr = kdConfig.getBulletinIdFolder() + "/" + bulletinId + ".html";
        File bulletinIdFile = new File(bulletinIdFileStr);
        String bulletinIdTemplateStr = kdConfig.getBulletin_id_template();
        String content = bulletinIdTemplateStr.replace(KdConfig.PlaceHolderNames.PROJECT_NAME, description.getProject_name())
                .replace(KdConfig.PlaceHolderNames.BUILD_OFFICE, description.getProject_name())
                .replace(KdConfig.PlaceHolderNames.OBSERVATION_PERIOD, description.getObservation_period())
                .replace(KdConfig.PlaceHolderNames.OBSERVATION_OFFICE, description.getObservation_office())
                .replace(KdConfig.PlaceHolderNames.OBSERVATION_STAFF, description.getObservation_staff())
                .replace(KdConfig.PlaceHolderNames.FILING_DATE, description.getFiling_date())
                .replace(KdConfig.PlaceHolderNames.BULLETIN_PAGE, postingDate)
                .replace(KdConfig.PlaceHolderNames.BULLETIN_ID, bulletinId)
                .replace(KdConfig.PlaceHolderNames.BULLETIN_ID_FIRST, bulletinIdFirst)
                .replace(KdConfig.PlaceHolderNames.BULLETIN_ID_PRE, bulletinIdPre)
                .replace(KdConfig.PlaceHolderNames.BULLETIN_ID_NEXT, bulletinIdNext)
                .replace(KdConfig.PlaceHolderNames.BULLETIN_ID_LAST, bulletinIdLast);

        try (Writer fw = new BufferedWriter(new FileWriter(bulletinIdFile))) {
            log.info("Writing to bulletinId file {} ", bulletinIdFileStr);
            fw.write(content);
            log.info("Finish Writing to bulletinId file {} ", bulletinIdFileStr);
        } catch (IOException e) {
            log.error("{} can't be created", bulletinIdFileStr);
        }
    }

    private void createBulletinPageFile(String postingDate, List<File> bulletinIdFileArray) {
        String bulletinPageItems = "";
        for (File bulletinIdFile : bulletinIdFileArray) {
            String bulletinId = bulletinIdFile.getName();

        }
    }

    private void cleanup() {
        String bulletinFolder = kdConfig.getBulletinFolder();
        File bulletinFoderDir = new File(bulletinFolder);
        if (!bulletinFoderDir.exists()) {
            try {
                Files.createDirectories(bulletinFoderDir.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Bulletin output directory can't be created");
            }
        }

        for (File item : bulletinFoderDir.listFiles()) {
            if (!FileUtils.deleteQuietly(item)) {
                throw new RuntimeException(item.getName() + " can't be deleted");
            }
        }
        try {
            Files.createDirectories(new File(kdConfig.getBulletinIdFolder()).toPath());
            Files.createDirectories(new File(kdConfig.getBulletinPageFolder()).toPath());
        } catch (Exception e) {
            throw new RuntimeException("Bulletin output sub directory can't be created");
        }

    }

}
