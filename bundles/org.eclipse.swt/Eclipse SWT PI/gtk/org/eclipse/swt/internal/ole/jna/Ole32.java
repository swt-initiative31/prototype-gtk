package org.eclipse.swt.internal.ole.jna;

import org.eclipse.swt.internal.ole.win32.*;

import com.sun.jna.*;

public interface Ole32 extends Library{

	Ole32 INSTANCE = Native.load("ole32", Ole32.class);

	int IIDFromString(Pointer lpsz, GUID lpiid);
}
