package com.example.doctorappointmentservice.service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Turns an untrusted upload into a safe, small avatar. The file type is decided by
 * magic bytes (never the client's Content-Type or filename), pixel dimensions are
 * checked before decoding (decompression-bomb guard), and the result is always
 * re-encoded as a fresh 512x512 JPEG, which drops EXIF/GPS metadata and anything
 * appended to or embedded in the original file.
 */
final class ProfileImageProcessor {

    static final int OUTPUT_SIZE = 512;
    private static final int MIN_DIMENSION = 64;
    private static final long MAX_PIXELS = 12_000_000L;
    private static final float JPEG_QUALITY = 0.85f;

    private ProfileImageProcessor() {
    }

    /** @throws IllegalArgumentException with a user-facing message if the upload isn't an acceptable image */
    static byte[] toAvatarJpeg(byte[] input) {
        String format = sniffFormat(input);
        if (format == null) {
            throw new IllegalArgumentException("Only JPEG or PNG images are supported.");
        }

        BufferedImage source = decode(input, format);

        int side = Math.min(source.getWidth(), source.getHeight());
        BufferedImage square = source.getSubimage(
                (source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);

        BufferedImage scaled = downscale(square, Math.min(side, OUTPUT_SIZE));
        return encodeJpeg(scaled);
    }

    private static String sniffFormat(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        return null;
    }

    private static BufferedImage decode(byte[] input, String format) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format);
            if (in == null || !readers.hasNext()) {
                throw new IllegalArgumentException("Couldn't read that image.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < MIN_DIMENSION || height < MIN_DIMENSION) {
                    throw new IllegalArgumentException("Image is too small (minimum " + MIN_DIMENSION + "x" + MIN_DIMENSION + " pixels).");
                }
                if ((long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException("Image resolution is too large.");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException("Couldn't read that image.");
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof IllegalArgumentException iae) {
                throw iae;
            }
            // Corrupt data, CMYK JPEGs and other unsupported encodings land here.
            throw new IllegalArgumentException("Couldn't read that image. Try a standard JPEG or PNG.");
        }
    }

    /** Halves repeatedly before the final resize so large photos don't alias. */
    private static BufferedImage downscale(BufferedImage image, int target) {
        BufferedImage current = image;
        while (current.getWidth() / 2 >= target) {
            current = draw(current, current.getWidth() / 2);
        }
        return draw(current, target);
    }

    private static BufferedImage draw(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE); // flatten PNG transparency onto white
            g.fillRect(0, 0, size, size);
            g.drawImage(src, 0, 0, size, size, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    private static byte[] encodeJpeg(BufferedImage image) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG encoder available");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ImageOutputStream out = ImageIO.createImageOutputStream(bytes)) {
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), param);
            out.flush();
            return bytes.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to encode image", ex);
        } finally {
            writer.dispose();
        }
    }
}
