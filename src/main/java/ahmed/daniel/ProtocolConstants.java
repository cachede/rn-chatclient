package ahmed.daniel;

/**
 * Contains all Protocol-specific constants which are used when creating a Message or receiving a message.
 */
public final class ProtocolConstants {

    private ProtocolConstants(){}

    // Basisheader:
    // TYP
    /**
     * Size in bytes for the type field in the basis header.
     */
    public static final int TYPE_SIZE_IN_BYTE = 1;

    /**
     * Index of the type field in the basis header.
     */
    public static final byte TYPE_INDEX = 0;

    /**
     * Type value for Verbindungspaket in the basis header.
     */
    public static final byte TYPE_VERBINDUNGSPAKET = 0;

    /**
     * Type value for Routingpaket in the basis header.
     */
    public static final byte TYPE_ROUTINGPAKET = 1;

    /**
     * Type value for Nachrichtenpaket in the basis header.
     */
    public static final byte TYPE_MESSAGEPAKET = 2;

    // TTL
    /**
     * Size in bytes for the time-to-live (TTL) field in the basis header.
     */
    public static final int TTL_SIZE_IN_BYTE = 1;

    /**
     * Index of the TTL field in the basis header.
     */
    public static final byte TTL_INDEX = 1;

    /**
     * Default TTL value.
     */
    public static final byte TTL = 64;

    // Zielnetzname and Quellnetzname
    /**
     * Size in bytes for the destination network name field in the basis header.
     */
    public static final int DESTINATION_NETWORK_NAME_SIZE_IN_BYTE = 3;

    /**
     * Default value for an unset destination network name.
     */
    public static final String DESTINATION_NETWORK_NAME_NOT_SET = "zzz";

    /**
     * Size in bytes for the source network name field in the basis header.
     */
    public static final int SOURCE_NETWORK_NAME_SIZE_IN_BYTE = 3;

    /**
     * Lower bound for the destination network name field in the basis header.
     */
    public static final int DESTINATION_NETWORK_NAME_LOWER = TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE;
    /**
     * Upper bound for the destination network name field in the basis header.
     */
    public static final int DESTINATION_NETWORK_NAME_HIGHER = DESTINATION_NETWORK_NAME_LOWER + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE;

    /**
     * Lower bound for the source network name field in the basis header.
     */
    public static final int SOURCE_NETWORK_NAME_LOWER = TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE;

    /**
     * Upper bound for the source network name field in the basis header.
     */
    public static final int SOURCE_NETWORK_NAME_HIGHER = SOURCE_NETWORK_NAME_LOWER + SOURCE_NETWORK_NAME_SIZE_IN_BYTE;

    // Total Basisheader size
    /**
     * Total size in bytes for the basis header.
     */
    public static final int BASISHEADER_SIZE_IN_BYTE =
            TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE + SOURCE_NETWORK_NAME_SIZE_IN_BYTE;

    // Nachrichtenpaket:
    /**
     * Maximum characters per message.
     */
    public static final int MAX_CHARACTERS_PER_MESSAGE = 320;

    /**
     * Size in bytes for the length field in the communication message.
     */
    public static final int COMMUNICATION_MESSAGE_LENGTH_IN_BYTE = 1;

    /**
     * Index of the length field in the communication message.
     */
    public static final int COMMUNICATION_MESSAGE_LENGTH_INDEX = 0;

    // Routingeintragspaket
    /**
     * Size in bytes for the destination field in the routing entry.
     */
    public static final int ROUTING_DESTINATION_SIZE_IN_BYTE = 3;

    /**
     * Size in bytes for the next hop field in the routing entry.
     */
    public static final int ROUTING_NEXT_HOP_SIZE_IN_BYTE = 3;

    /**
     * Size in bytes for the hop count field in the routing entry.
     */
    public static final int ROUTING_HOPCOUNT_SIZE_IN_BYTE = 1;

    // Total size
    /**
     * Total size in bytes for the routing entry.
     */
    public static final int ROUTING_ENTRY_SIZE_IN_BYTE = ROUTING_DESTINATION_SIZE_IN_BYTE + ROUTING_NEXT_HOP_SIZE_IN_BYTE
            + ROUTING_HOPCOUNT_SIZE_IN_BYTE;

    // Routingpaket:
    /**
     * Index of the amount of packets field in the routing packet.
     */
    public static final int ROUTING_AMOUNT_OF_PACKETS_INDEX = 0;

    /**
     * Size in bytes for the amount of packets field in the routing packet.
     */
    public static final int ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE = 1;

    /**
     * Index of the destination field in the routing packet.
     */
    public static final int ROUTING_INDEX_OF_DESTINATION = 1;

    /**
     * Index of the next hop field in the routing packet.
     */
    public static final int ROUTING_INDEX_OF_NEXTHOP = ROUTING_INDEX_OF_DESTINATION + ROUTING_DESTINATION_SIZE_IN_BYTE;

    /**
     * Index of the hop count field in the routing packet.
     */
    public static final int ROUTING_INDEX_OF_HOP_COUNT = ROUTING_INDEX_OF_NEXTHOP + ROUTING_NEXT_HOP_SIZE_IN_BYTE;

    /**
     * Maximum hop count value in the routing packet.
     */
    public static final byte ROUTING_MAX_HOPCOUNT = 16;

    /**
     * Value indicating that the destination is unreachable in the routing packet.
     */
    public static final byte ROUTING_DESTINATION_UNREACHABLE = 16;

    //CRC
    /**
     * Size in bytes for the CRC32 checksum field.
     */
    public static final byte CHECKSUM_CRC32_SIZE = 4;
}
