package com.dyn.robot.programs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract interface IMount
{
  public abstract boolean exists(String paramString)
    throws IOException;
  
  public abstract boolean isDirectory(String paramString)
    throws IOException;
  
  public abstract void list(String paramString, List<String> paramList)
    throws IOException;
  
  public abstract long getSize(String paramString)
    throws IOException;
  
  public abstract InputStream openForRead(String paramString)
    throws IOException;
}
