package rsvp.objects;

public abstract class SenderTemplate extends RSVPObject{

	public abstract void encode();

	public abstract void decode(byte[] bytes, int offset);

}
