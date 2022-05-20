package cn.dema.chunk;

import java.util.ArrayList;

public class XmlStruct {
	
	public byte[] byteSrc;
	
	public byte[] magicNumber;
	
	public byte[] fileSize;
	
	public StringChunk stringChunk;
	
	public ResourceChunk resChunk;

	public ArrayList<Chunk> tagChunkList = new ArrayList<Chunk>();
	
	public void clear(){
		magicNumber = null;
		fileSize = null;
		stringChunk = null;
		resChunk = null;
		tagChunkList.clear();
	}

}
