package com.dyn.robot.programs;

import java.io.IOException;
import java.io.OutputStream;

public abstract interface IWritableMount
  extends IMount
{
  public abstract void makeDirectory(String paramString)
    throws IOException;
  
  public abstract void delete(String paramString)
    throws IOException;
  
  public abstract OutputStream openForWrite(String paramString)
    throws IOException;
  
  public abstract OutputStream openForAppend(String paramString)
    throws IOException;
  
  public abstract long getRemainingSpace()
    throws IOException;
}
