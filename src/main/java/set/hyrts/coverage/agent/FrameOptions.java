package set.hyrts.coverage.agent;

public class FrameOptions {
    private static final int JAVA_7 = 51;

    public static int pickFlags(byte[] bs) {
        return needsFrames(bs) ? 2 : 1;
    }

    public static boolean needsFrames(byte[] bs) {
        short majorVersion = (short) ((bs[6] & 255) << 8 | bs[7] & 255);
        return majorVersion >= 51;
    }
}
