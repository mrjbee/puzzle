package org.monroe.team.puzzle.core.fs.config;

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
                try {
                    properties.load(new FileInputStream(file));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
                    mergeResult.put(objectObjectEntry.getKey(), objectObjectEntry.getValue());
                }
            }
        }
    }
}
