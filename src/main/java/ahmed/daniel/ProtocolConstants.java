package ahmed.daniel;

public final class ProtocolConstants {
    private ProtocolConstants(){}

    // Basisheader:
    // TYP
    public static final int TYPE_SIZE_IN_BYTE = 1;
    public static final byte TYPE_INDEX = 0;
    public static final byte TYPE_VERBINDUNGSPAKET = 0;
    public static final byte TYPE_ROUTINGPAKET = 1;
    public static final byte TYPE_MESSAGEPAKET = 2;


    // TTL
    public static final int TTL_SIZE_IN_BYTE = 1;
    public static final byte TTL_INDEX = 1;
    public static final byte TTL = 64;

    // Zielnetzname and Quellnetzname
    public static final int DESTINATION_NETWORK_NAME_SIZE_IN_BYTE = 3;
    public static final String DESTINATION_NETWORK_NAME_NOT_SET = "ZZZ";
    public static final int SOURCE_NETWORK_NAME_SIZE_IN_BYTE = 3;

    //Lower is inclusive Higher is exclusive
    public static final int DESTINATION_NETWORK_NAME_LOWER = TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE;
    public static final int DESTINATION_NETWORK_NAME_HIGHER = DESTINATION_NETWORK_NAME_LOWER + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE;

    public static final int SOURCE_NETWORK_NAME_LOWER = TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE;
    public static final int SOURCE_NETWORK_NAME_HIGHER = SOURCE_NETWORK_NAME_LOWER + SOURCE_NETWORK_NAME_SIZE_IN_BYTE;


    // Total Basisheader size
    public static final int BASISHEADER_SIZE_IN_BYTE =
            TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE + SOURCE_NETWORK_NAME_SIZE_IN_BYTE;

    // Verbindungspaket:
    // Name
    public static final byte NAME_SIZE_IN_BYTE = 3;

    // Nachrichtenpaket:
    public static final int MAX_CHARACTERS_PER_MESSAGE = 320;
    public static final int MAX_MESSAGE_LENGTH_IN_BYTES = BASISHEADER_SIZE_IN_BYTE + MAX_CHARACTERS_PER_MESSAGE;

    // Routingeintragspaket
    public static final int ROUTING_DESTINATION_SIZE_IN_BYTE = 3;

    public static final int ROUTING_NEXT_HOP_SIZE_IN_BYTE = 3;

    public static final int ROUTING_HOPCOUNT_SIZE_IN_BYTE = 1;

    // Total size
    public static final int ROUTING_ENTRY_SIZE_IN_BYTE = ROUTING_DESTINATION_SIZE_IN_BYTE + ROUTING_NEXT_HOP_SIZE_IN_BYTE
            + ROUTING_HOPCOUNT_SIZE_IN_BYTE;

    // Routingpaket:
    public static final int ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE = 1;
    public static final int ROUTING_PAKET_SIZE_IN_BYTE = BASISHEADER_SIZE_IN_BYTE + ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE;

    public static final byte ROUTING_MAX_HOPCOUNT = Byte.MAX_VALUE;
}
