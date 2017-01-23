package org.tenwell.identity.core.util;

import java.util.UUID;

public class UUIDUtil {
	public static String generateUUID(){
		String uuid;
		uuid = UUID.randomUUID().toString();
		return uuid;
	}
}
