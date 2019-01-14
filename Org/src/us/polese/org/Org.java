package us.polese.org;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

public class Org {
	private String filename; // Name of the file (with the extension appended to it).
	private int wait;
	private int steps;
	private int beats;
	private int loopStart;
	private int loopEnd;
	/*
	 * The instrument arrays are formatted to have 16 slots, with indexes 0-7 being
	 * the melody tracks, and 8-15 being the percussion tracks.
	 */
	private int instrumentPitch[];
	private int instrumentWave[];
	private int instrumentPi[];
	private int instrumentNotes[];

	// Creates a new org with the default settings.
	Org() {
		filename = "NewData.org";
		wait = 0x80; // Decimal 128
		steps = 0x04;
		beats = 0x04;
		loopStart = 0x00;
		loopEnd = 0x0FF0; // Decimal 4080
		instrumentPitch = new int[16];
		Arrays.fill(instrumentPitch, 0x03E8); // Decimal 1000

		instrumentWave = new int[16];
		// Default melody instrument waves.
		instrumentWave[0] = 0x00;
		instrumentWave[1] = 0x0B; // Decimal 11
		instrumentWave[2] = 0x16; // Decimal 22
		instrumentWave[3] = 0x21; // Decimal 33
		instrumentWave[4] = 0x2C; // Decimal 44
		instrumentWave[5] = 0x37; // Decimal 55
		instrumentWave[6] = 0x42; // Decimal 66
		instrumentWave[7] = 0x4D; // Decimal 77
		// Default percussion instrument waves.
		instrumentWave[8] = 0x00;
		instrumentWave[9] = 0x02;
		instrumentWave[10] = 0x05;
		instrumentWave[11] = 0x06;
		instrumentWave[12] = 0x04;
		instrumentWave[13] = 0x08;
		instrumentWave[14] = 0x00;
		instrumentWave[15] = 0x00;

		instrumentPi = new int[16];
		Arrays.fill(instrumentPi, 0x00);
		instrumentNotes = new int[16];
		Arrays.fill(instrumentNotes, 0x00);
	}

	
	Org(String filename) {
		RandomAccessFile myFile = null;
		FileChannel myChannel = null;
		ByteBuffer myBuffer = null;
		ArrayList<Integer> fileContents = new ArrayList<Integer>();
		int bytesRead = 0;
		
		try {
			myFile = new RandomAccessFile(filename, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		myChannel = myFile.getChannel();
		myBuffer = ByteBuffer.allocate(48);
		
		try {
			bytesRead = myChannel.read(myBuffer);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		while (bytesRead != -1) {
			myBuffer.flip();
			
			while (myBuffer.hasRemaining()) {
				int i = ((int) myBuffer.get()) & 0xFF;
				String s = Integer.toHexString(i);
				System.out.print(s + " ");
				fileContents.add(i);
			}
			
			myBuffer.clear();
			try {
				bytesRead = myChannel.read(myBuffer);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		try {
			myFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/* Old method; not necessary anymore
	// Reads an existing org file and loads the settings into the program.
	Org(String filename) {
		FileReader in = null;
		char fileContents[]; 
		int length = 0;
		this.filename = filename;

		
		
		// This method of reading the file is probably not the most efficient way.
		try {
			in = new FileReader(filename);
			
			// Read the file in to get the length
			while (in.read() != -1)
				length++;
			in.close();
			
			// Set the character array's length to how long the file is
			fileContents = new char[length];
			
			// Read the file into the character array
			in = new FileReader(filename);
			in.read(fileContents);
			in.close();
			
			// Need to find a way to convert from ascii to int properly...
			
			System.out.println((char)8364);
			// wait = Integer.parseInt("" + fileContents[6]);
			System.out.println(wait + ", " + fileContents[6] + (byte)fileContents[6]);
		} catch (IOException e) {
			System.out.println("An error occurred while reading the file.");
			System.exit(0);
		} finally {
			System.out.println(filename + " has been read successfully.");
		}
	}
	*/

	// Converts an integer decimal number to a little endian hex number as a string
	// with the specific number of bytes.
	private String toLittleEndian(int val, int bytes) {
		String strVal = Integer.toHexString(val);
		String strValLittleEndian = "";

		// If the amount of digits is not even, add a zero at the beginning to make it
		// even.
		if (strVal.length() % 2 == 1)
			strVal = "0" + strVal;

		// Add an appropriate amount of zeros to the beginning to make it the specified
		// number of bytes.
		while (strVal.length() / 2 < bytes)
			strVal = "00" + strVal;

		// Changes the hex string to little endian by swapping sets of two digits
		// around.
		for (int i = strVal.length() - 2; i >= 0; i -= 2)
			strValLittleEndian += strVal.substring(i, i + 2);

		return strValLittleEndian.toUpperCase();
	}

	// Converts a string of hex characters (little endian) into its ascii
	// equivalent.
	public String hexToAscii(String hex) {
		byte[] s = DatatypeConverter.parseHexBinary(hex);
		return new String(s);
	}

	// Saves the org to a file
	public void writeOrg() {
		FileWriter out = null;
		String filetext = "4F72672D3032"; // Start the file contents with the org file's header

		// Continue to store the remaining header information in the proper order.
		filetext += toLittleEndian(wait, 2);
		filetext += toLittleEndian(steps, 1);
		filetext += toLittleEndian(beats, 1);
		filetext += toLittleEndian(loopStart, 4);
		filetext += toLittleEndian(loopEnd, 4);

		// Header information for the 16 instruments
		for (int i = 0; i < instrumentPitch.length; i++) {
			filetext += toLittleEndian(instrumentPitch[i], 2);
			filetext += toLittleEndian(instrumentWave[i], 1);
			filetext += toLittleEndian(instrumentPi[i], 1);
			filetext += toLittleEndian(instrumentNotes[i], 2);
		}

		// Converts the hex string into an ascii string that will be stored in the file.
		filetext = hexToAscii(filetext);

		// Write the information to the file.
		try {
			out = new FileWriter(filename);
			out.write(filetext);
			out.close();
		} catch (IOException e) {
			System.out.println("An error occurred while writing the file.");
		} finally {
			System.out.println(filename + " has been written successfully.");
		}
	}
}