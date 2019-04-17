package de.wolfgangkronberg;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;

public class ImageWithMetadata {

    private final GlobalElements ge;
    private final Image image;
    private final Metadata metadata;

    public ImageWithMetadata(GlobalElements ge, File imageFile) {
        this.ge = ge;
        this.image = new Image(imageFile.toURI().toString());
        this.metadata = getMetadata(imageFile);
    }

    public ImageView getImageView(double width, double height) {
        final int rotate = getRotation();
        ImageView result = new ImageView(image);
        double zoom = ge.getImageZoom();
        if (zoom < 0) {
            result.setFitWidth(width);
            result.setFitHeight(height);
            ge.setEffectiveImageZoom(calculateZoom(rotate, width, height));
        } else {
            result.setScaleX(zoom);
            result.setScaleY(zoom);
            ge.setEffectiveImageZoom(zoom);
        }
        result.setRotate(rotate);
        result.setPreserveRatio(true);
        return result;
    }

    private double calculateZoom(int rotate, double width, double height) {
        boolean tipped = rotate == 90 || rotate == 270;
        double scale1 = height / (tipped ? image.getWidth() : image.getHeight());
        double scale2 = width / (tipped ? image.getHeight() : image.getWidth());
        return Math.min(scale1, scale2);
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
