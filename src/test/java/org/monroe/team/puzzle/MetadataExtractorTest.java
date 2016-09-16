package org.monroe.team.puzzle;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.googlecode.mp4parser.util.Path;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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


    @Test
    public void yiActionCameraVideoDataTest() throws ImageProcessingException, IOException, URISyntaxException {
        IsoFile isoFile = new IsoFile("src/test/resources/YDXJ0191.mp4");
        Object nam = Path.getPath(isoFile, "/moov");
        Date date = ((MovieBox) nam).getMovieHeaderBox().getCreationTime();
        isoFile.close();
        Assert.assertEquals("Sat Sep 10 10:16:22 EEST 2016", date.toString());
    }

    @Test
    public void galaxyNote2VideoDataTest() throws ImageProcessingException, IOException, URISyntaxException {
        IsoFile isoFile = new IsoFile("src/test/resources/VID_20160915_001549.mp4");
        Object nam = Path.getPath(isoFile, "/moov");//"/moov[0]/udta[0]/meta[0]/ilst/©nam");
        Date date = ((MovieBox) nam).getMovieHeaderBox().getCreationTime();
        isoFile.close();
        Assert.assertEquals("Thu Sep 15 00:16:07 EEST 2016", date.toString());
    }
}
