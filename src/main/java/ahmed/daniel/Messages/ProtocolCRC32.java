package ahmed.daniel.Messages;

import ahmed.daniel.ProtocolConstants;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ProtocolCRC32 {
    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static byte[] getChecksumBytes(long checksum){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(checksum);
        byte[] checksumByte = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
        System.arraycopy(buffer.array(), 4, checksumByte, 0, ProtocolConstants.CHECKSUM_CRC32_SIZE);
        return checksumByte;
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }
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
