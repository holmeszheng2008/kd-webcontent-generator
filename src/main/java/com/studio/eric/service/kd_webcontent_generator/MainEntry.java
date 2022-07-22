package com.studio.eric.service.kd_webcontent_generator;

import com.studio.eric.service.generator.Generator;
import com.studio.eric.service.generator.StaticContentConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainEntry {

    public static void main(String[] args) {
        long startMillis = System.currentTimeMillis();
        Generator generator = new Generator(new StaticContentConsumer());
        generator.generate();
        long endMillis = System.currentTimeMillis();
        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        log.info("Content Successfully Generated! Took {} ms.", endMillis - startMillis);
    }

}
