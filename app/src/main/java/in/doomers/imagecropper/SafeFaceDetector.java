package in.doomers.imagecropper;

import android.graphics.ImageFormat;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SafeFaceDetector extends Detector<Face> {
    private static final String TAG = "SafeFaceDetector";
    private Detector<Face> mDelegate;

    /**
     * Creates a safe face detector to wrap and protect an underlying face detector from images that
     * trigger the face detector bug.
     */
    public SafeFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    @Override
    public void release() {
        mDelegate.release();
    }

    /**
     * Determines whether the supplied image may cause a problem with the underlying face detector.
     * If it does, padding is added to the image in order to avoid the issue.
     */
    @Override
    public SparseArray<Face> detect(Frame frame) {
        final int kMinDimension = 147;
        final int kDimensionLower = 640;
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        if (height > (2 * kDimensionLower)) {
            // The image will be scaled down before detection is run.  Check to make sure that this
            // won't result in the width going below the minimum
            double multiple = (double) height / (double) kDimensionLower;
            double lowerWidth = Math.floor((double) width / multiple);
            if (lowerWidth < kMinDimension) {
                // The width would have gone below the minimum when downsampling, so apply padding
                // to the right to keep the width large enough.
                int newWidth = (int) Math.ceil(kMinDimension * multiple);
                frame = padFrameRight(frame, newWidth);
            }
        } else if (width > (2 * kDimensionLower)) {
            // The image will be scaled down before detection is run.  Check to make sure that this
            // won't result in the height going below the minimum
            double multiple = (double) width / (double) kDimensionLower;
            double lowerHeight = Math.floor((double) height / multiple);
            if (lowerHeight < kMinDimension) {
                int newHeight = (int) Math.ceil(kMinDimension * multiple);
                frame = padFrameBottom(frame, newHeight);
            }
        } else if (width < kMinDimension) {
            frame = padFrameRight(frame, kMinDimension);
        }

        return mDelegate.detect(frame);
    }

    @Override
    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    @Override
    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }

    /**
     * Creates a new frame based on the original frame, with additional width on the right to
     * increase the size to avoid the bug in the underlying face detector.
     */
    private Frame padFrameRight(Frame originalFrame, int newWidth) {
        Frame.Metadata metadata = originalFrame.getMetadata();
        int width = metadata.getWidth();
        int height = metadata.getHeight();

        Log.i(TAG, "Padded image from: " + width + "x" + height + " to " + newWidth + "x" + height);

        ByteBuffer origBuffer = originalFrame.getGrayscaleImageData();
        int origOffset = origBuffer.arrayOffset();
        byte[] origBytes = origBuffer.array();

        // This can be changed to just .allocate in the future, when Frame supports non-direct
        // byte buffers.
        ByteBuffer paddedBuffer = ByteBuffer.allocateDirect(newWidth * height);
        int paddedOffset = paddedBuffer.arrayOffset();
        byte[] paddedBytes = paddedBuffer.array();
        Arrays.fill(paddedBytes, (byte) 0);

        for (int y = 0; y < height; ++y) {
            int origStride = origOffset + y * width;
            int paddedStride = paddedOffset + y * newWidth;
            System.arraycopy(origBytes, origStride, paddedBytes, paddedStride, width);
        }

        return new Frame.Builder()
                .setImageData(paddedBuffer, newWidth, height, ImageFormat.NV21)
                .setId(metadata.getId())
                .setRotation(metadata.getRotation())
                .setTimestampMillis(metadata.getTimestampMillis())
                .build();
    }

    /**
     * Creates a new frame based on the original frame, with additional height on the bottom to
     * increase the size to avoid the bug in the underlying face detector.
     */
    private Frame padFrameBottom(Frame originalFrame, int newHeight) {
        Frame.Metadata metadata = originalFrame.getMetadata();
        int width = metadata.getWidth();
        int height = metadata.getHeight();

        Log.i(TAG, "Padded image from: " + width + "x" + height + " to " + width + "x" + newHeight);

        ByteBuffer origBuffer = originalFrame.getGrayscaleImageData();
        int origOffset = origBuffer.arrayOffset();
        byte[] origBytes = origBuffer.array();

        // This can be changed to just .allocate in the future, when Frame supports non-direct
        // byte buffers.
        ByteBuffer paddedBuffer = ByteBuffer.allocateDirect(width * newHeight);
        int paddedOffset = paddedBuffer.arrayOffset();
        byte[] paddedBytes = paddedBuffer.array();
        Arrays.fill(paddedBytes, (byte) 0);

        // Copy the image content from the original, without bothering to fill in the padded bottom
        // part.
        for (int y = 0; y < height; ++y) {
            int origStride = origOffset + y * width;
            int paddedStride = paddedOffset + y * width;
            System.arraycopy(origBytes, origStride, paddedBytes, paddedStride, width);
        }

        return new Frame.Builder()
                .setImageData(paddedBuffer, width, newHeight, ImageFormat.NV21)
                .setId(metadata.getId())
                .setRotation(metadata.getRotation())
                .setTimestampMillis(metadata.getTimestampMillis())
                .build();
    }
}


