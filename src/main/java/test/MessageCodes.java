package test;

public class MessageCodes {
	// Server -> Client
	public static short ENTITY_CREATED = 0;
	public static byte ENTITY_CREATED_ENTITY_ID = 0;
	public static byte ENTITY_CREATED_OWNER = 1;
	public static byte ENTITY_CREATED_ACTIVE = 2;
	public static byte ENTITY_CREATED_POSITION = 3;

	// Client -> Server
	public static short SET_PATH = 2;
	public static byte SET_PATH_POSITION = 0;

	// Server -> Client
	public static short SET_POSITION = 3;
	public static byte SET_POSITION_ENTITY_ID = 0;
	public static byte SET_POSITION_POSITION = 1;

	// Server -> Client
	public static short ENTITY_ACTIVATED = 4;
	public static byte ENTITY_ACTIVATED_ENTITY_ID = 0;

	// Server -> Client
	public static short ENTITY_DEACTIVATED = 5;
	public static byte ENTITY_DEACTIVATED_ENTITY_ID = 0;
}
