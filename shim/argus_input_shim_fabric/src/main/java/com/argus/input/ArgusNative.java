package com.argus.input;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ArgusNative {
    // The native library is loaded by your Android launcher process.
    // The mod only calls a native method exposed by that library.

    /** Returns 1 if an event was dequeued and written into outBuf, else 0. */
    public static native int nativeDequeue(long engineHandle, ByteBuffer outBuf);

    /** Allocate a direct buffer big enough for the C struct (64 bytes). */
    public static ByteBuffer newEventBuffer() {
        return ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
    }
}