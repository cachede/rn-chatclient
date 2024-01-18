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
}
