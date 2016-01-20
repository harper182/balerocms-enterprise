/**
 * Balero CMS Project: Proyecto 100% Mexicano de código libre.
 * Página Oficial: http://www.balerocms.com
 *
 * @author      Anibal Gomez <anibalgomez@icloud.com>
 * @copyright   Copyright (C) 2015 Neblina Software. Derechos reservados.
 * @license     Licencia BSD; vea LICENSE.txt
 */

package com.neblina.balero.util;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@Configuration
public class AssetPipeline {

    private static final Logger log = LogManager.getLogger(AssetPipeline.class.getName());

    public AssetPipeline() {
        log.debug("Running Balero CMS Resource Compiler...");
    }

    public void compress(String file) {
        String userDir = System.getProperty("user.dir");
        String html = "", sCurrentLine;
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            while ((sCurrentLine = input.readLine()) != null) {
                html += sCurrentLine;
            }
            HtmlCompressor compressor = new HtmlCompressor();
            String compressedHtml = compressor.compress(html);
            log.debug("Compiling Resource... " + file);
            String newFile = file.replace("templates/", "templates/dist/");
            //log.debug("Output String: " + newFile);
            String directory = newFile.substring(0, newFile.lastIndexOf("/"));
            //log.debug("Directoriy: " + directory);
            File newDir = new File(directory);
            File createFile = new File(newFile);
            if(!newDir.exists()) {
                newDir.mkdirs();
            }
            if(!createFile.exists()) {
                createFile.createNewFile();
            }
            FileWriter output = new FileWriter(newFile);
            BufferedWriter bw = new BufferedWriter(output);
            bw.write(compressedHtml);
            bw.close();
        } catch (Exception e) {
            System.out.println("Asset Pipeline Error: " + e.getMessage());
        }
    }

    public String multiPlatformResourcesPath(String file) {
        String resource = System.getProperty("user.dir") +
                "/src/main/resources/" + file;
        return resource.replace("\\", "/");
    }

    public ArrayList<String> getHtmlResourceFileList(String directory) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        Files.walk(Paths.get(multiPlatformResourcesPath(directory))).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String file = filePath.getFileName().toString();
                String ext = file.substring(file.lastIndexOf("."));
                if(ext.equals(".html")) {
                    list.add(filePath.toString());
                }
            }
        });
        return list;
    }

}
