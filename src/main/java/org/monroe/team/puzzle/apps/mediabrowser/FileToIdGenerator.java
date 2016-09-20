package org.monroe.team.puzzle.apps.mediabrowser;

import com.google.common.hash.Hashing;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.Charset;

@Component
public class FileToIdGenerator {
    public long toId(String filePath){
        long fileHash = Hashing.md5().hashString(filePath, Charset.defaultCharset()).asLong();
        return fileHash;
    }
}
