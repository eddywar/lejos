package lejos.nxt;

/**
 * Abstraction for the local NXT Device.
 * Supports methods that are non specific to any particular sub-system.
 * 
 * @author Lawrie Griffiths
 *
 */
public class NXT {	
	
	/**
	 * Terminate the application.
	 */	  
	public static native void exit(int code);
	
	/**
	 * Shutdown the brick
	 */
	public static native void shutDown();
		
	/**
	 * Boot into firmware update mode.
	 */
	public static native void boot();
	
	 /**
	  * Diagnostic tool (for firmware developers only)
	  */
	 public static native int diagn(int code, int param);
	 
	 /**
	  * Get the number of times a Java program (including the menu)
	  * has executed since the brick was switched on
	  * 
	  * @return the count
	  */
	 public static native int getProgramExecutionsCount();
	 
	 /**
	  * Get the leJOS NXJ firmware major version
	  * 
	  * @return the major version number
	  */
	 public static native int getFirmwareMajorVersion();

	 /**
	  * Get the leJOS NXJ firmware minor version
	  * 
	  * @return the minor version number
	  */
	 public static native int getFirmwareMinorVersion();

	 /**
	  * Get the leJOS NXJ firmware revision number
	  * 
	  * @return the revision number
	  */
	 public static native int getFirmwareRevision();
     
     /**
      * Return the number of flash pages available to user programs.
      * Normally these pages are used to hold the leJOS file system.
      * 
      * @return The number of user pages.
      */
     public static native int getUserPages();
}
