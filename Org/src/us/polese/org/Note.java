package us.polese.org;

public class Note {
	private short semitone; // 0-95, 255: 0 = lowest note, 95 = highest note, 255 = no change
	private short position;
	private short length;
	private short volume; // 0-255: 200 = default, 255 = no change
	private short pan; // 0-12, 255: 0 = left pan, 6 = center, 12 = right pan, 255 = no change
}
