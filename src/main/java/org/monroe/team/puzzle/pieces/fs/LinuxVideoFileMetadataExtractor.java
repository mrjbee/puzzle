package org.monroe.team.puzzle.pieces.fs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class LinuxVideoFileMetadataExtractor {

    private final static String KEY_CREATION_DATE = "Media Create Date".toLowerCase();
    private final static String KEY_IMAGE_WIDTH = "Source Image Width".toLowerCase();
    private final static String KEY_IMAGE_HEIGHT = "Source Image Height".toLowerCase();

    //ffmpeg -i VID_20160915_001549.mp4 -ss 00:00:05 -vframes 1 output2.jpg

    @Autowired
    FileHasher fileHasher;

    @Value("${video.ext.tool.tmp.folder}")
    String tmpFolder;

    private final String exifDataToolCommand = "exiftool '{in.file}' >> '{out.file}'";

    public int[] imageSize(File file){
        Map<String, String> metadataMap = getMetadataMap(file);
        String width = metadataMap.get(KEY_IMAGE_WIDTH.toLowerCase());
        String height = metadataMap.get(KEY_IMAGE_HEIGHT.toLowerCase());
        return new int[]{Integer.parseInt(width), Integer.parseInt(height)};
    }


    public Date creationDate(File file){
        Map<String, String> metadataMap = getMetadataMap(file);
        String creationDateString = metadataMap.get(KEY_CREATION_DATE.toLowerCase());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        try {
            return formatter.parse(creationDateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getMetadataMap(final File file) {
        File metadataFolder = getMetadataFolder(file);
        File exifDataFile = new File(metadataFolder,"metadata.exif");
        if (!exifDataFile.exists()) {
            generateMetadaFile(file, exifDataFile);
        }
        return exifFileToMap(exifDataFile);
    }

    private Map<String, String> exifFileToMap(final File exifDataFile) {
        Map<String, String> metadataMap = new HashMap<>();
        try {
            List<String> strings = Files.readAllLines(exifDataFile.toPath());
            for (String string : strings) {
                int colonIndex = string.indexOf(":");
                if (colonIndex > 0){
                    metadataMap.put(
                            string.substring(0, colonIndex).trim().toLowerCase(),
                            string.substring(colonIndex + 1).trim()
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return metadataMap;
    }

    private void generateMetadaFile(final File file, final File exifDataFile) {
        //TODO: Per resource monitor required here
        String cmd = exifDataToolCommand
                .replace("{in.file}", file.getAbsolutePath())
                .replace("{out.file}", exifDataFile.getAbsolutePath());
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh",
                    "-c",
                    cmd});
            process.waitFor(10, TimeUnit.SECONDS);
            int exitStatus = process.exitValue();
            if (exitStatus != 0 && exifDataFile.exists()){
                exifDataFile.delete();
            }
            if (exitStatus != 0){
                throw new RuntimeException("Could not generate metadata file. Cmd ends with = "+exitStatus +" For cmd = "+cmd);
            }
            if (!exifDataFile.exists()){
                throw new RuntimeException("No metadata file produced. Cmd ends with = "+exitStatus +" For cmd = "+cmd);
            }
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException("Could not execute = "+cmd, e);
        }
    }

    private File getMetadataFolder(final File file) {
        long hash = fileHasher.hash(file.getAbsolutePath());
        File fileMetadataFolder = new File(new File(tmpFolder), Long.toString(hash));
        if (!fileMetadataFolder.exists() && !fileMetadataFolder.mkdirs()){
            throw new IllegalStateException("Couldnt create folder = "+fileMetadataFolder);
        }
        return fileMetadataFolder;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(final String tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public File getVideoThumbnail(final File file) {
        File metadataFolder = getMetadataFolder(file);
        File thumbnailFile = new File(metadataFolder,"thumbnail.jpg");
        if (!thumbnailFile.exists()) {
            //TODO: Per resource monitor required here
            String cmd = "ffmpeg -i '" + file.getAbsolutePath() + "' -ss 00:00:01 -vframes 1 '" + thumbnailFile.getAbsolutePath() + "'";
            try {
                Process process = Runtime.getRuntime().exec(new String[]{
                        "/bin/sh",
                        "-c",
                        cmd});
                process.waitFor(10, TimeUnit.SECONDS);
                int exitStatus = process.exitValue();
                if (exitStatus != 0 && thumbnailFile.exists()){
                    thumbnailFile.delete();
                }
                if (exitStatus != 0){
                    throw new RuntimeException("Could not generate thumbnail file. Cmd ends with = "+exitStatus +" For cmd = "+cmd);
                }
                if (!thumbnailFile.exists()){
                    throw new RuntimeException("No thumbnail file produced. Cmd ends with = "+exitStatus +" For cmd = "+cmd);
                }
            } catch (IOException|InterruptedException e) {
                throw new RuntimeException("Could not execute = "+cmd, e);
            }
        }
        return thumbnailFile;
    }
}
