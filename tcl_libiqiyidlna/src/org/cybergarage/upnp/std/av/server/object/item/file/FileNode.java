package org.cybergarage.upnp.std.av.server.object.item.file;

import java.io.File;
import java.io.InputStream;

public interface FileNode
{

	public void setFile(File file);

	public File getFile();

	public long getFileTimeStamp();

	public boolean equals(File file);

	////////////////////////////////////////////////
	// Abstract methods
	////////////////////////////////////////////////

	public byte[] getContent();

	public long getContentLength();

	public InputStream getContentInputStream();

}
