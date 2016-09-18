package org.monroe.team.puzzle.core.fs.config;

import org.monroe.team.puzzle.core.log.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Component
public class FolderPropertiesProvider {

    public Properties forFolder(File folder) {
        if (!folder.isDirectory()) throw new IllegalStateException("Is not a folder = "+folder.getAbsolutePath());
        Properties answer = new Properties();
        mergeProperties(answer, folder);
        return answer;
    }

    private void mergeProperties(final Properties mergeResult, final File folder) {

        if (folder.getParentFile() != null){
            mergeProperties(mergeResult, folder.getParentFile());
        }

        File file = new File(folder,".puzzleconf");
        if (file.exists()){
            if (file.canRead()){
                Properties properties = new Properties();
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    properties.load(fileInputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (fileInputStream != null){
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            Logs.core.warn(e, "Couldnot close stream for file = {}", file.getAbsolutePath());
                        }
                    }
                }
                for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
                    mergeResult.put(objectObjectEntry.getKey(), objectObjectEntry.getValue());
                }
            }
        }
    }
}
