package de.wolfgangkronberg;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;

import java.io.File;
import java.io.IOException;

@Data
public class ImageWithMetadata {

    private Image image;
    private Metadata metadata;

    @SuppressWarnings("unused")  // Lombok
    public ImageWithMetadata() {
    }

    public ImageWithMetadata(File imageFile) {
        this.image = new Image(imageFile.toURI().toString());
        this.metadata = getMetadata(imageFile);
    }

    public ImageView getImageView(double width, double height) {
        final int rotate = getRotation();
        ImageView result = new ImageView(image);
        result.setFitWidth(width);
        result.setFitHeight(height);
        result.setRotate(rotate);
        result.setPreserveRatio(true);
        return result;
    }

    /**
     * Gets this images rotation from metadata, in degrees counter-clockwise
     * @return rotation as set in metadata
     */
    private int getRotation() {
        if (metadata == null) {
            return 0;
        }
        ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (directory == null) {
            return 0;
        }
        Integer orientation = directory.getInteger(274);
        if (orientation == null) {
            return 0;
        }
        switch (orientation) {
            case 8:
                return 270;
            case 3:
                return 180;
            case 6:
                return 90;
            default:
                return 0;
        }
    }

    private static Metadata getMetadata(File imageFile) {
        try {
            return ImageMetadataReader.readMetadata(imageFile);
        } catch (ImageProcessingException | IOException e) {
            return null;
        }
    }

    public boolean isValid() {
        return image.getHeight() > 0;
    }

}
