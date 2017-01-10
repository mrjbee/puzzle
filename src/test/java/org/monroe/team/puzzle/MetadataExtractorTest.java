package org.monroe.team.puzzle;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class MetadataExtractorTest {

    @Test
    public void galaxyNote2CameraPhotoDataTest() throws ImageProcessingException, IOException {

        InputStream imagePath = getClass().getClassLoader().getResourceAsStream("IMG_20160522_120724.jpg");
        Metadata metadata = ImageMetadataReader.readMetadata(imagePath);
        // obtain the Exif directory
        ExifSubIFDDirectory directory
                = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        // query the tag's value
        Date date
                = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

        Assert.assertEquals("Sun May 22 15:07:24 EEST 2016", date.toString());
    }


}
