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
    public static boolean checkSumIsCorrect(byte[] basisheader, byte[] payload, byte[] expectedChecsumCRC32Bytes) {
        byte[] tmp = new byte[4 + expectedChecsumCRC32Bytes.length];
        byte[] fillBytes = {0, 0, 0, 0};
        System.arraycopy(fillBytes, 0, tmp, 0, 4);
        System.arraycopy(expectedChecsumCRC32Bytes, 0, tmp, 4, expectedChecsumCRC32Bytes.length);
        long expectedChecksumCRC32 = ProtocolCRC32.bytesToLong(tmp);

        byte[] valuesForCRC32Calculation = new byte[basisheader.length + payload.length];
        System.arraycopy(basisheader, 0, valuesForCRC32Calculation, 0, basisheader.length);

        System.arraycopy(payload, 0, valuesForCRC32Calculation, basisheader.length, payload.length);
        long currentChecksumCRC32 = ProtocolCRC32.getCRC32Checksum(valuesForCRC32Calculation);

        return expectedChecksumCRC32 == currentChecksumCRC32;
    }

}
