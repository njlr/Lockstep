package io.njlr.lockstep.network;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Charsets;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.bytes.BytesReader;

/**
 * A collection of utility functions that do not belong anywhere in particular. 
 *
 */
public final class Utils {

	private Utils() {
		
		super();
	}
	
	/**
	 * Encodes a <code>String</code> as <code>Bytes</code> using UTF-8. 
	 * 
	 * @param message The message to encode
	 * @return The encoded message
	 */
	public static Bytes encode(final String message) {
		
		return new BytesBuilder()
				.appendInt(message.length())
				.append(Charsets.UTF_8.encode(message).array())
				.toBytes();
	}
	
	/**
	 * Decodes a <code>Bytes</code> representation of a <code>String</code> using UTF-8. 
	 * 
	 * @param data The encoded message
	 * @return The decoded message
	 */
	public static String decode(final Bytes data) {
		
		final BytesReader reader = data.read();
		
		final int length = reader.readInt();
		final Bytes text = reader.readRemaining().sub(0, length);
		
		return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(text.array())).toString();
	}
}
