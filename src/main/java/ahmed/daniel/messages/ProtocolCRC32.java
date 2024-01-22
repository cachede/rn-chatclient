package ahmed.daniel.messages;

import ahmed.daniel.ProtocolConstants;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * ProtocolCRC32 is for calculating and handling CRC32 checksums in the communication protocol.
 */
public abstract class ProtocolCRC32 {
    private ProtocolCRC32(){};
    /**
     * Calculates the CRC32 checksum for the given byte array
     *
     * @param bytes The input byte array for which the CRC32 checksum needs to be calculated
     * @return The calculated CRC32 checksum
     */
    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    /**
     * Converts a long checksum value into a byte array.
     *
     * @param checksum The long value representing the checksum
     * @return The byte array representation of the checksum
     */
    public static byte[] getChecksumBytes(long checksum){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(checksum);
        byte[] checksumByte = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
        System.arraycopy(buffer.array(), 4, checksumByte, 0, ProtocolConstants.CHECKSUM_CRC32_SIZE);
        return checksumByte;
    }

    /**
     * Converts a byte array to a long value.
     *
     * @param bytes The byte array to be converted to a long
     * @return The long value represented by the byte array
     */
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * Checks if the given CRC32 checksum is valid by comparing it with the calculated checksum
     * based on the provided header, payload, and expected checksum bytes.
     *
     * @param basisheader The basisheader bytes used in the checksum calculation
     * @param payload The payload bytes used in the checksum calculation
     * @param expectedChecsumCRC32Bytes The expected CRC32 checksum bytes to be validated
     * @return True if the checksum is invalid, false otherwise
     */
    public static boolean checkSumIsInvalid(byte[] basisheader, byte[] payload, byte[] expectedChecsumCRC32Bytes) {
        byte[] fillBytes = {0, 0, 0, 0};
        byte[] expectedChecksumCRC32WithFillBytes = new byte[fillBytes.length + expectedChecsumCRC32Bytes.length];

        System.arraycopy(fillBytes, 0, expectedChecksumCRC32WithFillBytes, 0, fillBytes.length);
        System.arraycopy(expectedChecsumCRC32Bytes, 0, expectedChecksumCRC32WithFillBytes, fillBytes.length, expectedChecsumCRC32Bytes.length);
        long expectedChecksumCRC32 = ProtocolCRC32.bytesToLong(expectedChecksumCRC32WithFillBytes);

        byte[] valuesForCRC32Calculation = new byte[basisheader.length + payload.length];
        System.arraycopy(basisheader, 0, valuesForCRC32Calculation, 0, basisheader.length);

        System.arraycopy(payload, 0, valuesForCRC32Calculation, basisheader.length, payload.length);
        long currentChecksumCRC32 = ProtocolCRC32.getCRC32Checksum(valuesForCRC32Calculation);

        return expectedChecksumCRC32 != currentChecksumCRC32;
    }
}
