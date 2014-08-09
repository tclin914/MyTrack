package com.demo.android.mytrack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.util.Log;

public class SerializerClass{
	/**
	 * 
	 */
	public byte[] serializeObject(Object o) { 
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	 
	    try {
	    	ObjectOutputStream out = new ObjectOutputStream(bos); 
	    	out.writeObject(o); 
	       
	 
	    	// Get the bytes of the serialized object 
	    	byte[] buf = bos.toByteArray();
	    	out.close();
	    	bos.close();
	    	return buf; 
	    } catch(IOException ioe) { 
	    	Log.e("serializeObject", "error", ioe);
	    	return null; 
	    } 
	  } 
	public Object deserializeObject(byte[] b) { 
	    try { 
	      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b)); 
	      Object object = in.readObject(); 
	      in.close(); 
	 
	      return object; 
	    } catch(ClassNotFoundException cnfe) { 
	      Log.e("deserializeObject", "class not found error", cnfe); 
	 
	      return null; 
	    } catch(IOException ioe) { 
//	      Log.e("deserializeObject", "io error", ioe); 
	      Log.e("deserializeObject", ioe.toString()); 
	 
	      return null; 
	    } 
	  } 

}
