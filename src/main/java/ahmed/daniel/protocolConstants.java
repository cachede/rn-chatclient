package ahmed.daniel;

public final class protocolConstants {
    private protocolConstants(){}

    // Basisheader:
    // TYP
    public static final int TYPE_SIZE_IN_BYTE = 1;
    public static final byte TYPE_VERBINDUNGSPAKET = 0;
    public static final byte TYPE_ROUTINGPAKET = 1;
    public static final byte TYPE_MESSAGEPAKET = 2;


    // TTL
    public static final int TTL_SIZE_IN_BYTE = 1;
    public static final byte TTL = 64;

    // Zielnetzname and Quellnetzname
    public static final int DESTINATION_NETWORK_NAME_SIZE_IN_BYTE = 3;
    public static final int SOURCE_NETWORK_NAME_SIZE_IN_BYTE = 3;

    // Total Basisheader size
    public static final int BASISHEADER_SIZE_IN_BYTE =
            TYPE_SIZE_IN_BYTE + TTL_SIZE_IN_BYTE + DESTINATION_NETWORK_NAME_SIZE_IN_BYTE + SOURCE_NETWORK_NAME_SIZE_IN_BYTE;

    // Verbindungspaket:
    // Name
    public static final byte NAME_SIZE_IN_BYTE = 3;
    public static final byte PORT_SIZE_IN_BYTE = 2;

    // Nachrichtenpaket:
    public static final int MAX_CHARACTERS_PER_MESSAGE = 320;
    public static final int MAX_MESSAGE_LENGTH_IN_BYTES = BASISHEADER_SIZE_IN_BYTE + MAX_CHARACTERS_PER_MESSAGE;

    // Routingeintragspaket
    public static final int ROUTING_SOURCE_SIZE_IN_BYTE = 3;
    public static final int ROUTING_DESTINATION_SIZE_IN_BYTE = 3;
    // Port-size like above
    public static final int ROUTING_HOPCOUNT_SIZE_IN_BYTE = 1;

    // Total size
    public static final int ROUTING_ENTRY_SIZE_IN_BYTE = ROUTING_SOURCE_SIZE_IN_BYTE
            + ROUTING_DESTINATION_SIZE_IN_BYTE + PORT_SIZE_IN_BYTE + ROUTING_HOPCOUNT_SIZE_IN_BYTE;

    // Routingpaket:
    public static final int ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE = 1;
    public static final int ROUTING_PAKET_SIZE_IN_BYTE = BASISHEADER_SIZE_IN_BYTE + ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE;
}
