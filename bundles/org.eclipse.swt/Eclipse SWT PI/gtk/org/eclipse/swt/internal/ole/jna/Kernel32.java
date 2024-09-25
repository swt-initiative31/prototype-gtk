package org.eclipse.swt.internal.ole.jna;

import com.sun.jna.*;
import com.sun.jna.win32.*;

public interface Kernel32 extends StdCallLibrary{

	Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

    void MoveMemory(Pointer Destination, Pointer Source, int Length);
}
