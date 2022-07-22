package com.studio.eric.service.generator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.studio.eric.service.config.KdConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticContentConsumer {
    private KdConfig kdConfig;
    private String bulletinFolderStr;

    @Getter
    private Map<String, List<File>> postingDateToBulletinIdDirMap = new TreeMap<>();

    @SneakyThrows
    public StaticContentConsumer() {
        kdConfig = KdConfig.getInstance();
        bulletinFolderStr = kdConfig.getWorkingDirectory() + "/static/bulletin";
        Files.createDirectories(Paths.get(bulletinFolderStr));
    }

    public void consume() {
        File bulletinFolder = new File(bulletinFolderStr);
        File[] postingDateDirs = bulletinFolder.listFiles(File::isDirectory);
        for (File postingDateDir : postingDateDirs) {
            List<File> bulletinIdArray = new ArrayList<>();
            postingDateToBulletinIdDirMap.put(postingDateDir.getName(), bulletinIdArray);
            File[] bulletinIdDirs = postingDateDir.listFiles(File::isDirectory);
            for(File bulletinIdDir : bulletinIdDirs) {
                bulletinIdArray.add(bulletinIdDir);
            }
            Collections.sort(bulletinIdArray, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    String name1 = o1.getName();
                    String name2 = o2.getName();
                    if (name1.length() == name2.length()) {
                        return name1.compareTo(name2);
                    }

                    return name1.length() - name2.length();
                }
            });
        }
    }
}

